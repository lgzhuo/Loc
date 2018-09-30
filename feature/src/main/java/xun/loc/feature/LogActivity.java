package xun.loc.feature;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import xun.loc.feature.widget.ItemClickHelper;

public class LogActivity extends AppCompatActivity {

    private FileObserver fileObserver;
    private FileFilter filter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(".csv");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final File dir = new File(getExternalFilesDir(null), "Log");

        RecyclerView rcv = findViewById(R.id.recycler_view);
        rcv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rcv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        final LogAdapter adapter = new LogAdapter(dir.listFiles(filter));
        rcv.setAdapter(adapter);

        ItemClickHelper.attach(rcv).on(new ItemClickHelper.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View itemView) {
                File file = adapter.getItem(position);
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setDataAndType(Uri.fromFile(file), "text/csv");
                it.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(it);
            }
        });

        fileObserver = new FileObserver(dir.getAbsolutePath(), FileObserver.CREATE | FileObserver.DELETE) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                adapter.setFiles(dir.listFiles(filter));
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fileObserver.stopWatching();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView lastModified;
        private TextView size;
        private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            size = itemView.findViewById(R.id.size);
            lastModified = itemView.findViewById(R.id.last_modified);
        }

        void bind(File file) {
            name.setText(file.getName());
            lastModified.setText(String.format(Locale.getDefault(), "最后修改时间:%s", format.format(new Date(file.lastModified()))));
            size.setText(String.format(Locale.getDefault(), "(%s)", Utils.formatSize(file.length())));
        }
    }

    static class LogAdapter extends RecyclerView.Adapter<LogViewHolder> {

        File[] files;

        LogAdapter(File[] files) {
            this.files = files;
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.log_item, viewGroup, false);
            return new LogViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder logViewHolder, int i) {
            logViewHolder.bind(files[i]);

        }

        @Override
        public int getItemCount() {
            return files == null ? 0 : files.length;
        }

        public File getItem(int i) {
            return files[i];
        }

        public void setFiles(File[] files) {
            this.files = files;
            notifyDataSetChanged();
        }
    }
}
