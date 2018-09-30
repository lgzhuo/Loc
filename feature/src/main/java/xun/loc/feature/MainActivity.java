package xun.loc.feature;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import xun.loc.feature.db.entity.Location;
import xun.loc.feature.db.entity.Track;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    static final String[] needPermissions;

    static {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
        if (Build.VERSION.SDK_INT >= 16) {
            permissions = Arrays.copyOf(permissions, permissions.length + 1);
            permissions[permissions.length - 1] = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        needPermissions = permissions;
    }

    private static final int PERMISSION_REQUEST_START_TRACK = 100;

    private TextureMapView mapView;
    private MainActivityModel model;
    private SelectionTracker<Long> selectionTracker;
    private LongSparseArray<TrackOverlay> trackOverlayArray = new LongSparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMap().setMyLocationEnabled(true);
        mapView.getMap().setMyLocationStyle(
                new MyLocationStyle()
                        .interval(2000)
                        .myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
        );
        mapView.getMap().addPolyline(new PolylineOptions());

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
//        SwitchCompat showAll = findViewById(R.id.show_all);
//        showAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                model.showAll();
//            }
//        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
//        new PagerSnapHelper().attachToRecyclerView(recyclerView);

        /* item divider */
        Drawable dividerDrawable = ActivityCompat.getDrawable(this, R.drawable.track_list_divider);
        if (dividerDrawable != null) {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
            dividerItemDecoration.setDrawable(dividerDrawable);
            recyclerView.addItemDecoration(dividerItemDecoration);
        }

        final TrackPagedListAdapter adapter = new TrackPagedListAdapter();
        adapter.setDelegate(new TrackPagedListAdapter.TrackItemDelegate() {
            @Override
            public void onTrackForward(Track track, int position) {
                TrackActivity.start(MainActivity.this, track.getId());
            }
        });
        recyclerView.setAdapter(adapter);

        /* recycler view selection */
        selectionTracker = new SelectionTracker.Builder<>(
                "track-selection",
                recyclerView,
                new StableIdKeyProvider(recyclerView),
                new TrackItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage()
        ).withGestureTooltypes(MotionEvent.TOOL_TYPE_FINGER).build();
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onItemStateChanged(@NonNull Long key, boolean selected) {
                Logger.d("item selection state changed on key:%d -> %b", key, selected);
            }

            @Override
            public void onSelectionChanged() {
                Logger.d("item selection changed, %s", selectionTracker.getSelection());
                model.setTrackSelection(selectionTracker.getSelection());
            }
        });
        adapter.setSelectionTracker(selectionTracker);

        model = ViewModelProviders.of(this).get(MainActivityModel.class);
        model.trackStarted.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean trackStarted) {
                fab.setEnabled(trackStarted != null);
                if (trackStarted == null) {
                    fab.setImageDrawable(null);
                } else if (trackStarted) {
                    fab.setImageResource(R.drawable.ic_block);
                } else {
                    fab.setImageResource(R.drawable.ic_track_changes);
                }
            }
        });
        model.allTracks.observe(this, new Observer<PagedList<Track>>() {
            @Override
            public void onChanged(@Nullable PagedList<Track> tracks) {
                adapter.submitList(tracks);
            }
        });

        model.locations.observe(this, new Observer<List<Location>>() {
            @Override
            public void onChanged(@Nullable List<Location> locations) {
                MainActivity.this.handleLocations(locations);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        TextView navigationSubTitle = navigationView.getHeaderView(0).findViewById(R.id.sub_title);
        navigationSubTitle.setText(getString(R.string.nav_header_subtitle, Utils.getVersion(this)));
        navigationView.setNavigationItemSelectedListener(this);

        /* restart track service to avoid error state after been killed */
        TrackService.restart(this);

        /* setup preferences default */
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        selectionTracker.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mapView.onSaveInstanceState(savedInstanceState);
        selectionTracker.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent it = new Intent(this, SettingsActivity.class);
            startActivity(it);
        } else if (id == R.id.nav_log) {
            Intent it = new Intent(this, LogActivity.class);
            startActivity(it);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_START_TRACK && Utils.checkGrantResult(permissions, grantResults).length == 0) {
            TrackService.start(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            Boolean trackStarted = model.trackStarted.getValue();
            if (trackStarted == null) {
            } else if (trackStarted) {
                TrackService.stop(this, null);
            } else {
                String[] deniedPermissions = Utils.checkSelfPermission(this, needPermissions);
                if (deniedPermissions.length > 0) {
                    String[] showRationalePermissions = Utils.shouldShowRequestPermissionRationale(this, deniedPermissions);
                    if (showRationalePermissions.length > 0) {
                        StringBuilder message = new StringBuilder("定位缺少以下权限。\n ");
                        for (String permission : deniedPermissions) {
                            message.append(permissionDescription(permission)).append("、");
                        }
                        message.setCharAt(message.length() - 1, '\n');
                        message.append("请前往设置中开启相关权限");
                        new AlertDialog.Builder(this)
                                .setTitle("提示")
                                .setMessage(message)
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        startActivity(intent);
                                    }
                                })
                                .show();
                    } else {
                        ActivityCompat.requestPermissions(this, deniedPermissions, PERMISSION_REQUEST_START_TRACK);
                    }
                } else {
                    TrackService.start(this);
                }
            }
        }
    }

    /* track polyline */
    private void handleLocations(@Nullable List<Location> locations) {
        if (locations == null) {
            for (int i = 0; i < trackOverlayArray.size(); i++) {
                trackOverlayArray.valueAt(i).remove();
            }
            trackOverlayArray.clear();
        } else {
            LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

            LongSparseArray<TrackOverlay> nextTrackOverlayArray = new LongSparseArray<>();
            List<Integer> originIndexList = new ArrayList<>();
            for (int i = 0; i < locations.size(); i++) {
                if (locations.get(i).isOriginal()) {
                    originIndexList.add(i);
                }
            }
            originIndexList.add(locations.size());
            int from = 0;
            for (int to : originIndexList) {
                if (from == to) continue;
                List<Location> locationPatch = locations.subList(from, to);
                Long id = locationPatch.get(0).getId();
                TrackOverlay overlay = trackOverlayArray.get(id);
                if (overlay == null) {
                    overlay = new TrackOverlay(mapView.getMap());
                }
                overlay.setLocations(locationPatch);
                nextTrackOverlayArray.put(id, overlay);
                LatLngBounds bounds = overlay.bounds();
                if (bounds != null) {
                    boundsBuilder.include(bounds.southwest);
                    boundsBuilder.include(bounds.northeast);
                }
                from = to;
            }
            for (int i = 0; i < trackOverlayArray.size(); i++) {
                if (!nextTrackOverlayArray.containsKey(trackOverlayArray.keyAt(i))) {
                    trackOverlayArray.valueAt(i).remove();
                }
            }
            trackOverlayArray = nextTrackOverlayArray;

            mapView.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 40));
        }
    }

    static String permissionDescription(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "网络定位";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "GPS定位";
            case Manifest.permission.READ_PHONE_STATE:
                return "读取手机当前的状态";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "写入缓存数据到扩展存储卡";

        }
        return null;
    }
}
