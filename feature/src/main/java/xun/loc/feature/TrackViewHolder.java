package xun.loc.feature;

import android.graphics.Color;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.recyclerview.selection.ItemDetailsLookup;
import xun.loc.feature.db.entity.Track;

public class TrackViewHolder extends RecyclerView.ViewHolder {

    private static final DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());

    private TextView identifier;
    private TextView timeRange;
    private TextView alarmConfig;
    private TextView dataType;
    private View forward;

    private Track track;
    private SelectionDetail selectionDetail;

    public TrackViewHolder(@NonNull ViewGroup parent, final TrackPagedListAdapter.TrackItemDelegate delegate) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.track_item, parent, false));
        identifier = itemView.findViewById(R.id.identifier);
        timeRange = itemView.findViewById(R.id.time_range);
        alarmConfig = itemView.findViewById(R.id.alarm_config);
        dataType = itemView.findViewById(R.id.data_type);

        (forward = itemView.findViewById(R.id.forward)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegate.onTrackForward(track, getAdapterPosition());
                Logger.d("track item forward clicked " + getAdapterPosition());
            }
        });
    }

    public void bind(@Nullable Track track, boolean isSelected) {
        this.track = track;
        itemView.setActivated(isSelected);
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
                _alarmConfig.append(" 开启, 间隔 ").append(String.format(Locale.getDefault(), "%d分钟 %d秒", track.getAlarmInterval() / 60000, (track.getAlarmInterval() / 1000) % 60));
            } else {
                _alarmConfig.append(" 关闭");
            }
            alarmConfig.setText(_alarmConfig);
        }
    }

    public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
//        long id = getItemId();
//        return new SelectionDetail(getAdapterPosition(), id == RecyclerView.NO_ID ? null : id)
//                .exHotspot(childRect(forward));
        if (selectionDetail == null) {
            selectionDetail = new SelectionDetail();
        }
        return selectionDetail;
    }

    private RectF childRect(View view) {
        float offsetX = view.getTranslationX() + itemView.getX() + itemView.getTranslationX();
        float offsetY = view.getTranslationY() + itemView.getY() + itemView.getTranslationY();
        return new RectF((float) view.getLeft() + offsetX,
                (float) view.getTop() + offsetY,
                (float) view.getRight() + offsetX,
                (float) view.getBottom() + offsetY);
    }

    class SelectionDetail extends ItemDetailsLookup.ItemDetails<Long> {
        @Override
        public int getPosition() {
            return getAdapterPosition();
        }

        @Nullable
        @Override
        public Long getSelectionKey() {
            long id = getItemId();
            return id == RecyclerView.NO_ID ? null : id;
        }

        @Override
        public boolean inSelectionHotspot(@NonNull MotionEvent e) {
//            float x = e.getX(), y = e.getY();
//            Logger.d("inSelectionHotspot x:" + x + " y:" + y);
//            return !childRect(forward).contains(x, y);
            return false;
        }
    }

//    class SelectionDetail extends ItemDetailsLookup.ItemDetails<Long> {
//
//        private int position;
//        private Long key;
//        private List<RectF> exHotspotList = new ArrayList<>();
//
//        SelectionDetail(int position, Long key) {
//            this.position = position;
//            this.key = key;
//        }
//
//        SelectionDetail exHotspot(RectF rect) {
//            exHotspotList.add(rect);
//            return this;
//        }
//
//        @Override
//        public int getPosition() {
//            return position;
//        }
//
//        @Nullable
//        @Override
//        public Long getSelectionKey() {
//            return key;
//        }
//
//        @Override
//        public boolean inSelectionHotspot(@NonNull MotionEvent e) {
//            float x = e.getX(), y = e.getY();
//            float rawX = e.getRawX(), rawY = e.getRawY();
//            Logger.d("event x:" + x + " y:" + y);
//            Logger.d("event rawX:" + rawX + " rawY:" + rawY);
//            for (RectF rect : exHotspotList) {
//                if (rect.contains(x, y)) {
//                    return false;
//                }
//            }
//            return true;
//        }
//    }
}
