package xun.loc.feature;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import xun.loc.feature.db.entrity.Track;

public class TrackViewHolder extends RecyclerView.ViewHolder {

    private static final DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());

    private TextView identifier;
    private TextView timeRange;
    private TextView alarmConfig;
    private TextView dataType;

    public TrackViewHolder(@NonNull ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.track_item, parent, false));
        identifier = itemView.findViewById(R.id.identifier);
        timeRange = itemView.findViewById(R.id.time_range);
        alarmConfig = itemView.findViewById(R.id.alarm_config);
        dataType = itemView.findViewById(R.id.data_type);
    }

    public void bind(@Nullable Track track) {
        if (track == null) {
            identifier.setText(null);
            timeRange.setText(null);
            alarmConfig.setText(null);
            dataType.setText(null);
        } else {
            identifier.setText(String.format(Locale.getDefault(), "%d.", track.getId()));
            SpannableStringBuilder _timeRange = new SpannableStringBuilder();
            _timeRange.append("起 ");
            _timeRange.append(dateFormat.format(track.getStartTime()));
            _timeRange.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

            if (track.getStopTime() != null) {
                _timeRange.append("\n");
                int start = _timeRange.length();
                _timeRange.append("止 ").append(dateFormat.format(track.getStopTime()));
                _timeRange.setSpan(new ForegroundColorSpan(Color.RED), start, start + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            timeRange.setText(_timeRange);
            StringBuilder _alarmConfig = new StringBuilder("alarm ");
            if (track.isAlarmEnabled()) {
                _alarmConfig.append(" 开启, 间隔 ").append(track.getAlarmInterval()).append("分钟");
            } else {
                _alarmConfig.append(" 关闭");
            }
            alarmConfig.setText(_alarmConfig);
        }
    }
}
