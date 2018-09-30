package xun.loc.feature;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import xun.loc.feature.db.entity.Track;

public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.TrackViewHolder> {

    private DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
    private List<Track> mTrackList;

    public void setTrackList(final List<Track> trackList) {
        if (mTrackList == null) {
            mTrackList = trackList;
            notifyItemRangeInserted(0, trackList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mTrackList.size();
                }

                @Override
                public int getNewListSize() {
                    return trackList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    Track newProduct = trackList.get(newItemPosition);
                    Track oldProduct = mTrackList.get(oldItemPosition);
                    return Utils.equals(newProduct.getId(), oldProduct.getId());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Track newProduct = trackList.get(newItemPosition);
                    Track oldProduct = mTrackList.get(oldItemPosition);
                    return Utils.equals(newProduct, oldProduct);
                }
            });
            mTrackList = trackList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.track_item, viewGroup);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder trackViewHolder, int i) {
        Track track = mTrackList.get(i);
        trackViewHolder.identifier.setText(String.format(Locale.SIMPLIFIED_CHINESE, "%d.", track.getId()));
        trackViewHolder.timeRange.setText(String.format(Locale.SIMPLIFIED_CHINESE, "%s", dateFormat.format(track.getStartTime())));
    }

    @Override
    public int getItemCount() {
        return mTrackList == null ? 0 : mTrackList.size();
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        final TextView identifier;
        final TextView timeRange;
        final TextView alarmConfig;
        final TextView dataType;

        TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            identifier = itemView.findViewById(R.id.identifier);
            timeRange = itemView.findViewById(R.id.time_range);
            alarmConfig = itemView.findViewById(R.id.alarm_config);
            dataType = itemView.findViewById(R.id.data_type);
        }
    }
}
