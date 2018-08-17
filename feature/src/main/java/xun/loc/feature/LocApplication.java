package xun.loc.feature;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.Logger;

import java.io.File;

import xun.loc.feature.Constants.APP_STATE;
import xun.loc.feature.log.CustomDiskLogStrategy;

public class LocApplication extends Application {

    int startedCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (startedCount++ == 0) {
                    Intent intent = new Intent(APP_STATE.BROADCAST_ACTION);
                    intent.putExtra(APP_STATE.EXTRA_STATE, APP_STATE.FOREGROUND);
                    broadcastManager.sendBroadcast(intent);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (startedCount > 0 && --startedCount == 0) {
                    Intent intent = new Intent(APP_STATE.BROADCAST_ACTION);
                    intent.putExtra(APP_STATE.EXTRA_STATE, APP_STATE.BACKGROUND);
                    broadcastManager.sendBroadcast(intent);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        LogAdapter logcatAdapter = new AndroidLogAdapter();
        Logger.addLogAdapter(logcatAdapter);

        String dirPath = null;
        File dir = getExternalFilesDir(null);
        if (dir != null) {
            dirPath = dir.getAbsolutePath() + File.separatorChar + "Log";
        }

        HandlerThread ht = new HandlerThread("FileLogger");
        ht.start();
        CustomDiskLogStrategy logStrategy = new CustomDiskLogStrategy(ht.getLooper(), dirPath, null, null);
        LogAdapter fileAdapter = new DiskLogAdapter(CsvFormatStrategy.newBuilder().logStrategy(logStrategy).build());
        Logger.addLogAdapter(fileAdapter);
    }

    public int getAppState() {
        return startedCount > 0 ? APP_STATE.FOREGROUND : APP_STATE.BACKGROUND;
    }
}
