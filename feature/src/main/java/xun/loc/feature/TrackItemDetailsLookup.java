package xun.loc.feature;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.orhanobut.logger.Logger;

import androidx.recyclerview.selection.ItemDetailsLookup;

public class TrackItemDetailsLookup extends ItemDetailsLookup<Long> {

    private RecyclerView mRecView;

    public TrackItemDetailsLookup(RecyclerView recyclerView) {
        this.mRecView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(final @NonNull MotionEvent e) {
        ItemDetails<Long> itemDetails = null;
        View view = mRecView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = mRecView.getChildViewHolder(view);
            if (holder instanceof TrackViewHolder) {
                itemDetails = ((TrackViewHolder) holder).getItemDetails();
//                return ((TrackViewHolder) holder).getItemDetails();
            }
        }
        Logger.d("x:%f y:%f -> %s", e.getX(), e.getY(), itemDetails);
        return itemDetails;
    }
}
