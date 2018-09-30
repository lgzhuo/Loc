package xun.loc.feature;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import xun.loc.feature.db.entity.Location;

public class TrackActivity extends AppCompatActivity {

    private static final String EXTRA_TRACK_ID = "xun.loc.feature.TRACK_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        final LocationPagedListAdapter adapter = new LocationPagedListAdapter();
        recyclerView.setAdapter(adapter);

        TrackActivityModel model = ViewModelProviders.of(this).get(TrackActivityModel.class);
        model.trackId.setValue(getTrackId());
        model.locations.observe(this, new Observer<PagedList<Location>>() {
            @Override
            public void onChanged(@Nullable PagedList<Location> locations) {
                adapter.submitList(locations);
            }
        });
    }

    private Long getTrackId() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_TRACK_ID)) {
            return intent.getLongExtra(EXTRA_TRACK_ID, -1);
        } else {
            return null;
        }
    }

    public static void start(Context context, Long trackId) {
        Intent it = new Intent(context, TrackActivity.class);
        it.putExtra(EXTRA_TRACK_ID, trackId);
        context.startActivity(it);
    }
}
