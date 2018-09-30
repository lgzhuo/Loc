package xun.loc.feature;

import android.arch.paging.PagedList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import androidx.recyclerview.selection.ItemKeyProvider;
import xun.loc.feature.db.entity.Track;

public class TrackItemKeyProvider extends ItemKeyProvider<Long> {

    private TrackPagedListAdapter adapter;

    public TrackItemKeyProvider(TrackPagedListAdapter adapter) {
        super(SCOPE_MAPPED);
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public Long getKey(int i) {
        if (adapter.hasStableIds()) {
            long id = adapter.getItemId(i);
            if (id != RecyclerView.NO_ID) return id;
        }
        return null;
    }

    @Override
    public int getPosition(@NonNull Long key) {
        PagedList<Track> pagedList = adapter.getCurrentList();
        if (pagedList != null) {
            List<Track> list = pagedList.snapshot();
            for (int i = 0, size = list.size(); i < size; i++) {
                Track track = list.get(i);
                if (track != null && Utils.equals(track.getId(), key)) {
                    return i;
                }
            }
        }
        // cant access the key，may have not yet loaded
        return -1;
    }
}
