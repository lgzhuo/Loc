package xun.loc.feature;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.orhanobut.logger.Logger;

public class TrackService extends Service {

    private static final String TRACK_CHANNEL_ID = "track";
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
    private AMapLocationListener locListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {

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
        /* init location client */
        locClient = new AMapLocationClient(this);
        AMapLocationClientOption locOption = new AMapLocationClientOption();
        locOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        locClient.setLocationOption(locOption);
        locClient.setLocationListener(locListener);

        /* init app state broadcast receiver */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.APP_STATE.BROADCAST_ACTION);
        registerReceiver(receiver, filter);

        /* init Loc message handler */
        PreferenceManager.getDefaultSharedPreferences(this);

        Logger.i("track service created");
    }

    @Override
    public void onDestroy() {
        locClient.onDestroy();
        locClient = null;
        unregisterReceiver(receiver);
        Logger.i("track service stopped");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!locClient.isStarted()) {
            locClient.startLocation();
            onAppState(((LocApplication) getApplication()).getAppState());
        }
        return START_STICKY;
    }

    private void startAlarm(){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
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
        Notification.Builder builder = null;
        Notification notification = null;
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
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("正在获取位置")
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }
}
