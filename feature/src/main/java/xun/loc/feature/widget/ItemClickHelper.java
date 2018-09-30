package xun.loc.feature.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import xun.loc.feature.R;

public class ItemClickHelper implements View.OnClickListener {

    private RecyclerView recyclerView;
    private OnItemClickListener listener;

    private ItemClickHelper(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                view.setOnClickListener(ItemClickHelper.this);
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
            }
        });
        this.recyclerView.setTag(R.id.item_click_helper, this);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(v);
            if (viewHolder != null) {
                listener.onItemClick(viewHolder.getAdapterPosition(), v);
            }
        }
    }

    public void on(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static ItemClickHelper attach(RecyclerView recyclerView) {
        if (recyclerView.getTag(R.id.item_click_helper) instanceof ItemClickHelper) {
            return (ItemClickHelper) recyclerView.getTag(R.id.item_click_helper);
        } else {
            return new ItemClickHelper(recyclerView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View itemView);
    }
}
