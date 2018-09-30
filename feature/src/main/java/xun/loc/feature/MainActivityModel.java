package xun.loc.feature;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.recyclerview.selection.MutableSelection;
import androidx.recyclerview.selection.Selection;
import xun.loc.feature.db.AppDataBase;
import xun.loc.feature.db.entity.Location;
import xun.loc.feature.db.entity.Track;

public class MainActivityModel extends AndroidViewModel {

    private static final int MS_SET_TRACK_SELECTION = 1;

    public final LiveData<Boolean> trackStarted;
    public final LiveData<List<Location>> locations;
    public final LiveData<PagedList<Track>> allTracks;

    private MutableLiveData<Boolean> showAll;
    private MutableLiveData<List<Long>> selectedTrackIds = new MutableLiveData<>();
    private MutableSelection<Long> trackSelection = new MutableSelection<>();

    private Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MS_SET_TRACK_SELECTION:
                    selectedTrackIds.setValue((List<Long>) msg.obj);
                    return true;
            }
            return false;
        }
    });

    public MainActivityModel(@NonNull Application application) {
        super(application);

        final AppDataBase db = AppDataBase.getInstance(application);
        LiveData<Track> latestTrack = db.trackDao().latest();
        trackStarted = Transformations.map(latestTrack, new Function<Track, Boolean>() {
            @Override
            public Boolean apply(Track input) {
                return input != null && !input.isStopped();
            }
        });

        locations = Transformations.switchMap(selectedTrackIds, new Function<List<Long>, LiveData<List<Location>>>() {
            @Override
            public LiveData<List<Location>> apply(List<Long> input) {
                return db.locationDao().findByTrackIds(input);
            }
        });

        allTracks = new LivePagedListBuilder<>(db.trackDao().allPagedDesc(), new PagedList.Config.Builder()
                .setPageSize(5)
                .setEnablePlaceholders(true)
                .build()).build();
    }

    public void showAll() {

    }

    public void setTrackSelection(Selection<Long> selection) {
        trackSelection.copyFrom(selection);
        List<Long> ids = new ArrayList<>();
        for (Iterator<Long> ite = trackSelection.iterator(); ite.hasNext(); ) {
            ids.add(ite.next());
        }
        handler.obtainMessage(MS_SET_TRACK_SELECTION, ids).sendToTarget();
    }
}
