package xun.loc.feature;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

public class TrackAlarmReceiver extends WakefulBroadcastReceiver {

    private static final String ACTION_WEAK = "xun.loc.feature.weak";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), ACTION_WEAK)) {
            TrackService.wake(context);
        }
    }

    static PendingIntent pendingWake(Context context) {
        Intent it = new Intent(context, TrackAlarmReceiver.class);
        it.setAction(ACTION_WEAK);
        return PendingIntent.getBroadcast(context, 100, it, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
