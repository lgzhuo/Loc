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

import java.util.Collections;

import xun.loc.feature.db.AppDataBase;
import xun.loc.feature.db.entity.Location;

public class TrackActivityModel extends AndroidViewModel {

    public final LiveData<PagedList<Location>> locations;

    public final MutableLiveData<Long> trackId = new MutableLiveData<>();

    public TrackActivityModel(@NonNull Application application) {
        super(application);
        final AppDataBase db = AppDataBase.getInstance(application);


        final PagedList.Config config = new PagedList.Config.Builder()
                .setPageSize(15)
                .setEnablePlaceholders(true)
                .build();
        locations = Transformations.switchMap(trackId, new Function<Long, LiveData<PagedList<Location>>>() {
            @Override
            public LiveData<PagedList<Location>> apply(Long input) {
                if (input == null) return null;
                return new LivePagedListBuilder<>(db.locationDao().dataSourceFindByTrackIds(Collections.singletonList(input)), config).build();
            }
        });
    }

}
