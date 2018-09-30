package xun.loc.feature;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.amap.api.maps.model.LatLng;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import xun.loc.feature.db.entity.Location;

public class Utils {
    public static boolean equals(Object a, Object b) {
        if (Build.VERSION.SDK_INT >= 19) {
            return Objects.equals(a, b);
        }
        return (a == b) || (a != null && a.equals(b));
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static String getVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude(), false);
    }

    public static String formatSize(long size) {
        float result = size;
        String suffix = "B";
        if (result > 900) {
            suffix = "kB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "MB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "GB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "TB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "PB";
            result = result / 1024;
        }
        String value;
        if (result < 1) {
            value = String.format(Locale.getDefault(), "%.2f", result);
        } else if (result < 10) {
            value = String.format(Locale.getDefault(), "%.2f", result);
        } else {
            value = String.format(Locale.getDefault(), "%.0f", result);
        }
        return String.format("%1$s %2$s", value, suffix);
    }

    public static String[] checkSelfPermission(Context context, String... permissions) {
        String[] deniedPermissions = new String[permissions.length];
        int length = 0;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions[length++] = permission;
            }
        }
        return Arrays.copyOf(deniedPermissions, length);
    }

    public static String[] shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        String[] showRationalePermissions = new String[permissions.length];
        int length = 0;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationalePermissions[length++] = permission;
            }
        }
        return Arrays.copyOf(showRationalePermissions, length);
    }

    public static String[] checkGrantResult(String[] permissions, int[] grantResults) {
        String[] deniedPermissions = new String[permissions.length];
        int length = 0;
        for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions[length++] = permissions[i];
            }
        }
        return Arrays.copyOf(deniedPermissions, length);
    }
}
