package xun.loc.feature;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.orhanobut.logger.Logger;

import java.util.Date;

import xun.loc.feature.db.AppDataBase;
import xun.loc.feature.db.dao.TrackDao;
import xun.loc.feature.db.entrity.Location;
import xun.loc.feature.db.entrity.Track;

public class TrackService extends Service {

    private static final String ACTION_START = "xun.loc.feature.TRACT_START";
    private static final String ACTION_STOP = "xun.loc.feature.TRACT_STOP";
    private static final String ACTION_CONTINUE = "xun.loc.feature.CONTINUE";
    private static final String ACTION_WAKE = "xun.loc.feature.WAKE";

    private static final String EXTRA_TRACK_ID = "xun.loc.feature.TRACK_ID";

    private static final int MS_TRACK_START = 0;
    private static final int MS_TRACK_CONTINUE = 1;
    private static final int MS_LOCATION = 2;
    private static final int MS_TRACK_STOP = 3;
    private static final int MS_NOOP = 4;
    private static final int MS_WAKE = 5;

    private static final String TRACK_CHANNEL_ID = "track";

    private static PowerManager.WakeLock wakeLock;

    private AMapLocationClient locClient;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int appState = intent.getIntExtra(Constants.APP_STATE.EXTRA_STATE, -1);
            onAppState(appState);
        }
    };
    private boolean isForeground = false;
    private boolean isChannelCreated = false;
    private AMapLocationListener locListener;
    private AppDataBase db;
    private Looper handlerLooper;
    private Handler handler;

    private boolean wakeWating = false;
    private PendingIntent alarmIntent;

    private Track currentTrack;
    private boolean locationOriginal = false;
    private Handler.Callback handlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MS_TRACK_START:
                    handleTrackStart(msg.arg1, msg.arg2);
                    return true;
                case MS_TRACK_CONTINUE:
                    handleTrackContinue(msg.arg1, msg.arg2);
                    return true;
                case MS_LOCATION:
                    AMapLocation location = (AMapLocation) msg.obj;
                    handleLocation(location);
                    return true;
                case MS_TRACK_STOP:
                    Integer trackId = (Integer) msg.obj;
                    handleTrackStop(msg.arg1, msg.arg2, trackId);
                    return true;
                case MS_NOOP:
                    handleNoop(msg.arg1, msg.arg2);
                    return true;
                case MS_WAKE:
                    handleWake(msg.arg1, msg.arg2);
                    return true;
            }
            return false;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /* init app state */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.APP_STATE.BROADCAST_ACTION);
        registerReceiver(receiver, filter);
        onAppState(((LocApplication) getApplication()).getAppState());

        /* init Loc message handler */
        HandlerThread thread = new HandlerThread("track-service");
        thread.start();

        handlerLooper = thread.getLooper();
        handler = new Handler(handlerLooper, handlerCallback);
        db = AppDataBase.getInstance(this);

        Logger.i("track service created");
    }

    @Override
    public void onDestroy() {
        if (locClient != null) {
            locClient.unRegisterLocationListener(locListener);
            locClient.onDestroy();
            locClient = null;
        }
        unregisterReceiver(receiver);
        if (Build.VERSION.SDK_INT > 18) {
            handlerLooper.quitSafely();
        } else {
            handlerLooper.quit();
        }
        if (wakeLock != null) {
            wakeLock.release();
        }
        Logger.i("track service stopped");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent == null) {
            Logger.i("track service 重启");
            handler.removeMessages(MS_TRACK_CONTINUE);
            handler.obtainMessage(MS_TRACK_CONTINUE, flags, startId).sendToTarget();
        } else if (TextUtils.equals(intent.getAction(), ACTION_CONTINUE)) {
            handler.removeMessages(MS_TRACK_CONTINUE);
            handler.obtainMessage(MS_TRACK_CONTINUE, flags, startId).sendToTarget();
        } else if (TextUtils.equals(intent.getAction(), ACTION_START)) {
            handler.removeMessages(MS_TRACK_START);
            handler.obtainMessage(MS_TRACK_START, flags, startId).sendToTarget();
        } else if (TextUtils.equals(intent.getAction(), ACTION_STOP)) {
            Message message = handler.obtainMessage(MS_TRACK_STOP, flags, startId);
            if (intent.hasExtra(EXTRA_TRACK_ID)) {
                message.obj = intent.getIntExtra(EXTRA_TRACK_ID, -1);
            }
            message.sendToTarget();
        } else if (TextUtils.equals(intent.getAction(), ACTION_WAKE)) {
            handler.removeMessages(MS_WAKE);
            handler.obtainMessage(MS_WAKE, flags, startId).sendToTarget();
        } else {
            handler.obtainMessage(MS_NOOP, flags, startId).sendToTarget();
        }
        return START_STICKY;
    }

    private void onAppState(int appState) {
        switch (appState) {
            case Constants.APP_STATE.FOREGROUND:
                if (isForeground) {
                    stopForeground(true);
                    isForeground = false;
                }
                break;
            case Constants.APP_STATE.BACKGROUND:
                if (!isForeground) {
                    startForeground(10086, buildNotification());
                    isForeground = true;
                }
                break;
        }
    }

    private Notification buildNotification() {
        Notification.Builder builder;
        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (isChannelCreated) {
                NotificationChannel notificationChannel = new NotificationChannel(TRACK_CHANNEL_ID,
                        "轨迹获取", NotificationManager.IMPORTANCE_DEFAULT);
                try {
                    notificationManager.createNotificationChannel(notificationChannel);
                    isChannelCreated = true;
                } catch (Exception ignore) {
                }
            }
            if (isChannelCreated) {
                builder = new Notification.Builder(this, TRACK_CHANNEL_ID);
            } else {
                builder = new Notification.Builder(this);
            }
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setSmallIcon(xun.loc.R.mipmap.ic_launcher)
                .setContentTitle(getString(xun.loc.R.string.app_name))
                .setContentText("正在获取位置")
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }

    private void setupTrack(@NonNull Track track) {
        cleanTrack();

        currentTrack = track;
        locationOriginal = true;

        /* init location client */
        if (locClient == null) {
            locClient = new AMapLocationClient(this);
            locClient.setLocationListener(locListener = new LocationListener());
        }

        AMapLocationClientOption locOption = new AMapLocationClientOption();
        locOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        locOption.setInterval(track.getInterval());

        locClient.setLocationOption(locOption);
        locClient.startLocation();

        /* setup alarm */
        if (track.isAlarmEnabled()) {
            int alarmInterval = track.getAlarmInterval() * 60000;
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + alarmInterval,
                        alarmInterval, alarmIntent = TrackAlarmReceiver.pendingWake(this));
            }
        }
    }

    private void cleanTrack() {
        currentTrack = null;
        if (locClient != null) {
            locClient.stopLocation();
        }
        if (alarmIntent != null) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                am.cancel(alarmIntent);
            }
        }
        cleanWakeLock();
    }

    private void cleanWakeLock() {
        wakeWating = false;
        if (wakeLock != null && handler.hasMessages(MS_WAKE)) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private boolean isTracking() {
        return currentTrack != null && !currentTrack.isStopped();
    }

    /* handle methods, run on handlerLooper */

    private void handleTrackStop(int flags, int startId, @Nullable Integer trackId) {
        Track targetTrack;
        if (trackId != null) {
            if (currentTrack != null && currentTrack.getId().intValue() == trackId.intValue()) {
                targetTrack = currentTrack;
            } else {
                targetTrack = db.trackDao().findById(trackId);
            }
        } else if (currentTrack != null) {
            targetTrack = currentTrack;
        } else {
            targetTrack = db.trackDao().latestSync();
        }
        if (targetTrack != null && !targetTrack.isStopped()) {
            targetTrack.setStopTime(new Date());
            db.trackDao().update(targetTrack);
            Logger.i("关闭轨迹->%d", targetTrack.getId());
        }
        if (targetTrack == currentTrack) {
            cleanTrack();
        }
        if (!isTracking()) {
            stopSelf(startId);
        }
    }

    private void handleTrackStart(int flags, int startId) {
        final Track nextTrack = new Track();
        nextTrack.setStartTime(new Date());
        /* build track on preference */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        nextTrack.setAlarmEnabled(preferences.getBoolean("loc_alarm_enabled", true));
        nextTrack.setAlarmInterval(preferences.getInt("loc_alarm_interval", 5));
        nextTrack.setInterval(preferences.getLong("loc_interval", 10));

        final TrackDao trackDao = db.trackDao();
        final Track preTrack = currentTrack;
        db.runInTransaction(new Runnable() {
            @Override
            public void run() {
                if (preTrack != null) {
                    preTrack.setStopTime(new Date());
                    trackDao.update(preTrack);
                }
                trackDao.insertAndAssign(nextTrack);
            }
        });
        setupTrack(nextTrack);
        if (preTrack != null) {
            Logger.i("关闭轨迹->", preTrack.getId());
        }
        Logger.i("开启新轨迹->%s", currentTrack);
    }

    private void handleTrackContinue(int flags, int startId) {
        if (isTracking()) return;
        Track latestTrack = db.trackDao().latestSync();
        if (latestTrack == null || latestTrack.isStopped()) {
            Logger.e("没有持续中的轨迹，无法延续，退出");
            stopSelf(startId);
        } else {
            setupTrack(latestTrack);
            Logger.e("延续轨迹->%d", currentTrack.getId());
        }
    }

    private void handleLocation(AMapLocation al) {
        if (!isTracking()) return;
        Location location = new Location(
                currentTrack.getId(),
                al.getLatitude(),
                al.getLongitude(),
                al.getTime(),
                al.getSpeed(),
                al.getBearing(),
                locationOriginal,
                wakeWating
        );
        locationOriginal = false;
        db.locationDao().insert(location);
        Logger.i("loc->%s", location.toString());
        cleanWakeLock();
    }

    private void handleNoop(int flags, int startId) {
        if (!isTracking()) {
            stopSelf(startId);
        }
    }

    private void handleWake(int flags, int startId) {
        handleTrackContinue(flags, startId);
        if (isTracking()) {
            wakeWating = true;
            locClient.startLocation();
        }
    }

    /* static action */

    public static void start(Context context) {
        Intent it = new Intent(context, TrackService.class);
        it.setAction(ACTION_START);
        context.startService(it);
    }

    public static void stop(Context context, @Nullable Integer trackId) {
        Intent it = new Intent(context, TrackService.class);
        it.setAction(ACTION_STOP);
        if (trackId != null) {
            it.putExtra(EXTRA_TRACK_ID, trackId.intValue());
        }
        context.startService(it);
    }

    public static void restart(Context context) {
        Intent it = new Intent(context, TrackService.class);
        it.setAction(ACTION_CONTINUE);
        context.startService(it);
    }

    public static void wake(Context context) {
        acquireWakeLockNow(context);

        Intent it = new Intent(context, TrackService.class);
        it.setAction(ACTION_WAKE);
        context.startService(it);
    }

    private static void acquireWakeLockNow(Context context) {
        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            if (powerManager == null) return;
            wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    TrackService.class.getSimpleName());
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire(30000);
        }
    }

    /* inner class */
    class LocationListener implements AMapLocationListener {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            handler.obtainMessage(MS_LOCATION, aMapLocation).sendToTarget();
        }
    }
}
