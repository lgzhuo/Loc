package xun.loc.feature.db.entrity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "location",
        foreignKeys = {
                @ForeignKey(
                        entity = Track.class,
                        parentColumns = "id",
                        childColumns = "track_id"
                )
        })
public class Location {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "track_id", index = true)
    private int trackId;
    private double latitude;
    private double longitude;
    private long time;
    private float speed;
    private float bearing;

    private boolean original;
    @ColumnInfo(name = "alarm_wake")
    private boolean alarmWake;

    public Location(int trackId, double latitude, double longitude, long time, float speed, float bearing, boolean original, boolean alarmWake) {
        this.trackId = trackId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.speed = speed;
        this.bearing = bearing;
        this.original = original;
        this.alarmWake = alarmWake;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public boolean isOriginal() {
        return original;
    }

    public void setOriginal(boolean original) {
        this.original = original;
    }

    public boolean isAlarmWake() {
        return alarmWake;
    }

    public void setAlarmWake(boolean alarmWake) {
        this.alarmWake = alarmWake;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", trackId=" + trackId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", time=" + time +
                ", speed=" + speed +
                ", bearing=" + bearing +
                ", original=" + original +
                ", alarmWake=" + alarmWake +
                '}';
    }
}
