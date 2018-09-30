package xun.loc.feature;

import android.arch.paging.PagedListAdapter;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import xun.loc.feature.databinding.LocationItemBinding;
import xun.loc.feature.db.entity.Location;

public class LocationPagedListAdapter extends PagedListAdapter<Location, LocationPagedListAdapter.LocationViewHolder> {

    private static DiffUtil.ItemCallback<Location> DIFF_CALLBACK = new DiffUtil.ItemCallback<Location>() {
        @Override
        public boolean areItemsTheSame(@NonNull Location l1, @NonNull Location l2) {
            return Utils.equals(l1.getId(), l2.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Location l1, @NonNull Location l2) {
            return Utils.equals(l1, l2);
        }
    };

    public LocationPagedListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new LocationViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder locationViewHolder, int i) {
        locationViewHolder.bind(getItem(i));
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        LocationItemBinding binding;

        LocationViewHolder(@NonNull ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false));
            binding = DataBindingUtil.bind(itemView);
        }

        void bind(@Nullable Location location) {
            binding.setLocation(location);
        }
    }
}
