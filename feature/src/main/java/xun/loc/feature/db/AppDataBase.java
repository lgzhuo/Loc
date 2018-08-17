package xun.loc.feature.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import xun.loc.feature.db.converter.DateConverter;
import xun.loc.feature.db.dao.LocationDao;
import xun.loc.feature.db.dao.TrackDao;
import xun.loc.feature.db.entrity.Location;
import xun.loc.feature.db.entrity.Track;

@Database(entities = {Location.class, Track.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDataBase extends RoomDatabase {

    public static final String DATABASE_NAME = "base-db";

    public abstract LocationDao locationDao();

    public abstract TrackDao trackDao();

    private static AppDataBase dataBase;

    public static AppDataBase getInstance(Context context) {
        if (dataBase == null) {
            synchronized (AppDataBase.class) {
                if (dataBase == null) {
                    dataBase = buildDatabase(context);
                }
            }
        }
        return dataBase;
    }

    private static AppDataBase buildDatabase(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(), AppDataBase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }
}
