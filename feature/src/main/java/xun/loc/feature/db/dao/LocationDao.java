package xun.loc.feature.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import xun.loc.feature.db.entity.Location;

@Dao
public interface LocationDao {

    @Insert
    void insert(Location location);

    @Query("SELECT * from location where track_id = :trackId")
    List<Location> findByTrackId(long trackId);

    @Query("SELECT * from location")
    LiveData<List<Location>> all();

    @Query("SELECT * from location where track_id = (SELECT max(id) from track)")
    LiveData<List<Location>> findByLatestTrack();

    @Query("SELECT * from location where track_id in (:trackIds)")
    LiveData<List<Location>> findByTrackIds(List<Long> trackIds);

    @Query("SELECT * from location where track_id in (:trackIds)")
    DataSource.Factory<Integer, Location> dataSourceFindByTrackIds(List<Long> trackIds);
}
