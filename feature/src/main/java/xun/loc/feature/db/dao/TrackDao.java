package xun.loc.feature.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.Update;

import java.util.List;

import xun.loc.feature.db.entity.Track;

@Dao
public abstract class TrackDao {

    private RoomDatabase db;

    TrackDao(RoomDatabase db) {
        this.db = db;
    }

    @Insert(onConflict = OnConflictStrategy.FAIL)
    public abstract void insert(Track track);

    @Update
    public abstract void update(Track track);

    @Query("SELECT * from track order by id desc limit 1")
    public abstract Track latestSync();

    @Query("SELECT * from track order by id desc limit 1")
    public abstract LiveData<Track> latest();

    @Query("SELECT * from track")
    public abstract LiveData<List<Track>> all();

    @Query("SELECT * from track order by id desc")
    public abstract DataSource.Factory<Integer, Track> allPagedDesc();

    @Query("SELECT * from track where id = :trackId")
    public abstract Track findById(int trackId);

    @Query("SELECT id from track")
    public abstract LiveData<List<Long>> allIds();

    public void insertAndAssign(final Track track) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                insert(track);
                Track entry = latestSync();
                track.setId(entry.getId());
            }
        };
        if (db.inTransaction()) {
            runnable.run();
        } else {
            db.runInTransaction(runnable);
        }
    }
}
