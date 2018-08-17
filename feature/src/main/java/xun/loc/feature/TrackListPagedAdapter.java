package xun.loc.feature;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.view.ViewGroup;

import xun.loc.feature.db.entrity.Track;

public class TrackListPagedAdapter extends PagedListAdapter<Track, TrackViewHolder> {

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

    public TrackListPagedAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new TrackViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder trackViewHolder, int i) {
        trackViewHolder.bind(getItem(i));
    }
}
