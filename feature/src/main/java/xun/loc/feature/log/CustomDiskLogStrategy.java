package xun.loc.feature.log;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orhanobut.logger.LogStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CustomDiskLogStrategy extends Handler implements LogStrategy {

    private String dirPath;
    private String filePrefix;
    private long maxSize;

    public CustomDiskLogStrategy(@NonNull Looper looper, @Nullable String dirPath, @Nullable String filePrefix, @Nullable Long maxSize) {
        super(looper);
        this.filePrefix = filePrefix != null ? filePrefix : "log";
        this.maxSize = maxSize != null ? maxSize : 500 * 1024;
        post(new DirSetup(dirPath));
    }

    @Override
    public void handleMessage(Message msg) {

        String[] content = (String[]) msg.obj;
        String tag = content[0], message = content[1];
        File logFile = getLogFile();
        FileWriter writer = null;
        try {
            writer = new FileWriter(logFile, true);

            writer.append(message);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e1) { /* fail silently */ }
            }
        }
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {
        obtainMessage(priority, new String[]{tag, message}).sendToTarget();
    }

    private File getLogFile() {
        int count = 0;
        File logFile;
        do {
            logFile = new File(dirPath + File.separatorChar + String.format("%s_%s.csv", this.filePrefix, count++));
        } while (logFile.isFile() && logFile.length() >= maxSize);
        return logFile;
    }

    class DirSetup implements Runnable {

        String dirPath;

        DirSetup(@Nullable String dirPath) {
            this.dirPath = dirPath;
        }

        @Override
        public void run() {
            File dir = null;
            if (!TextUtils.isEmpty(dirPath)) {
                dir = new File(dirPath);
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    dir = null;
                }
            }
            if (dir == null) {
                dir = new File(Environment.getExternalStorageDirectory(), "Log");
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    dir = Environment.getExternalStorageDirectory();
                }
            }
            CustomDiskLogStrategy.this.dirPath = dir.getAbsolutePath();
        }
    }
}
