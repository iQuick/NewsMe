package me.imli.newme.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.imli.newme.R;
import me.imli.newme.helper.recycler.ItemTouchHelperAdapter;
import me.imli.newme.helper.recycler.ItemTouchHelperViewHolder;
import me.imli.newme.helper.recycler.OnStartDragListener;
import me.imli.newme.model.Channel;

/**
 * Created by Em on 2015/12/29.
 */
public class ChannelCustomAdapter extends RecyclerView.Adapter<ChannelCustomAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private Context context;
    private List<Channel> data = new ArrayList<>();
    private OnStartDragListener mDragStartListener;
    private OnChangeListener mOnchagenListener;

    public ChannelCustomAdapter(Context context) {
        this.context = context;
    }

    public void onChange(List<Channel> channels) {
        data.clear();
        data.addAll(channels);
        notifyDataSetChanged();
    }

    public List<Channel> getData() {
        return data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel_custom, parent, false));
    }

    @Override
    public void onBindViewHolder(ChannelCustomAdapter.ViewHolder holder, int position) {
        Channel channel = data.get(position);
        holder.channel.setText(channel.name);
        holder.channel.setChecked(channel.show);
        // Click
        holder.channel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.get(position).show = !holder.channel.isChecked();
                holder.channel.setChecked(data.get(position).show);
                if (mOnchagenListener != null) {
                    mOnchagenListener.onChange();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(data, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        if (mOnchagenListener != null) {
            mOnchagenListener.onChange();
        }
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        if (mOnchagenListener != null) {
            mOnchagenListener.onChange();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        mDragStartListener = listener;
    }

    public void setOnChangeListener(OnChangeListener listener) {
        mOnchagenListener = listener;
    }

    /**
     *
     */
    public interface OnChangeListener {
        public void onChange();
    }

    /**
     *
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        public CheckedTextView channel;

        public ViewHolder(View itemView) {
            super(itemView);
            channel = (CheckedTextView) itemView.findViewById(R.id.channel);
        }


        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

}
