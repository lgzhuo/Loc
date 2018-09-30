package xun.loc.feature.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import xun.loc.feature.Utils;

@Entity(tableName = "track")
public class Track {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    @ColumnInfo(name = "start_time")
    private Date startTime;
    @ColumnInfo(name = "stop_time")
    private Date stopTime;

    private long interval;

    @ColumnInfo(name = "alarm_enabled")
    private boolean alarmEnabled;
    @ColumnInfo(name = "alarm_interval")
    private long alarmInterval;

    private boolean correct;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public boolean isAlarmEnabled() {
        return alarmEnabled;
    }

    public void setAlarmEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }

    public long getAlarmInterval() {
        return alarmInterval;
    }

    public void setAlarmInterval(long alarmInterval) {
        this.alarmInterval = alarmInterval;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    /* compute property */
    public boolean isStopped() {
        return this.stopTime != null;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", stopTime=" + stopTime +
                ", interval=" + interval +
                ", alarmEnabled=" + alarmEnabled +
                ", alarmInterval=" + alarmInterval +
                ", correct=" + correct +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return interval == track.interval &&
                alarmEnabled == track.alarmEnabled &&
                alarmInterval == track.alarmInterval &&
                correct == track.correct &&
                Utils.equals(id, track.id) &&
                Utils.equals(startTime, track.startTime) &&
                Utils.equals(stopTime, track.stopTime);
    }
}
