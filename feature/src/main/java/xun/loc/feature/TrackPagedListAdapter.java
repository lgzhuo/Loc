package xun.loc.feature;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import androidx.recyclerview.selection.SelectionTracker;
import xun.loc.feature.db.entity.Track;

public class TrackPagedListAdapter extends PagedListAdapter<Track, TrackViewHolder> {

    private static DiffUtil.ItemCallback<Track> DIFF_CALLBACK = new DiffUtil.ItemCallback<Track>() {
        @Override
        public boolean areItemsTheSame(@NonNull Track t1, @NonNull Track t2) {
            return Utils.equals(t1.getId(), t2.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Track t1, @NonNull Track t2) {
            return Utils.equals(t1, t2);
        }
    };

    private SelectionTracker<Long> selectionTracker;
    private TrackItemDelegateWrapper delegate = new TrackItemDelegateWrapper();

    public TrackPagedListAdapter() {
        super(DIFF_CALLBACK);
        setHasStableIds(true);
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @Override
    public long getItemId(int position) {
        Track track = getItem(position);
        return track != null ? track.getId() : RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new TrackViewHolder(viewGroup, delegate);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder trackViewHolder, int i) {
        Track track = getItem(i);
        trackViewHolder.bind(track, track != null && selectionTracker.isSelected(track.getId()));
    }

    public void setDelegate(TrackItemDelegate delegate) {
        this.delegate.body = delegate;
    }

    static abstract class TrackItemDelegate {
        public void onTrackForward(Track track, int position) {
        }
    }

    static class TrackItemDelegateWrapper extends TrackItemDelegate {
        TrackItemDelegate body;

        @Override
        public void onTrackForward(Track track, int position) {
            if (body != null) body.onTrackForward(track, position);
        }
    }
}
