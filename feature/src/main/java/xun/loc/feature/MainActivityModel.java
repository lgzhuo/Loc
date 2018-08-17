package xun.loc.feature;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import java.util.List;

import xun.loc.feature.db.AppDataBase;
import xun.loc.feature.db.entrity.Location;
import xun.loc.feature.db.entrity.Track;

public class MainActivityModel extends AndroidViewModel {

    public final LiveData<Boolean> trackStarted;
    public final LiveData<List<Location>> locations;
    public final LiveData<PagedList<Track>> allTracks;

    private MutableLiveData<Boolean> showAll;

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

        showAll = new MutableLiveData<>();
        showAll.setValue(false);

        locations = Transformations.switchMap(showAll, new Function<Boolean, LiveData<List<Location>>>() {
            @Override
            public LiveData<List<Location>> apply(Boolean input) {
                return input ? db.locationDao().all() : db.locationDao().findByLatestTrack();
            }
        });

        allTracks = new LivePagedListBuilder<>(db.trackDao().allPagedDesc(), new PagedList.Config.Builder()
                .setPageSize(5)
                .setEnablePlaceholders(true)
                .build()).build();
    }
}
