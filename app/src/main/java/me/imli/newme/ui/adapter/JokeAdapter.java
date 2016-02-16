package me.imli.newme.ui.adapter;

import android.content.Context;
import android.databinding.ObservableList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import me.imli.newme.R;
import me.imli.newme.api.ApiConst;
import me.imli.newme.databinding.ItemJokeBinding;
import me.imli.newme.model.Image;
import me.imli.newme.model.Joke;
import ooo.oxo.library.databinding.support.widget.BindingRecyclerView;

/**
 * Created by Em on 2015/12/10.
 */
public class JokeAdapter extends BindingRecyclerView.ListAdapter<Joke, JokeAdapter.ViewHolder> {

    private final RequestManager mRequestManager;

    public JokeAdapter(Context context, ObservableList<Joke> data, RequestManager manager) {
        super(context, data);
        this.mRequestManager = manager;
        this.setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemJokeBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Joke joke = data.get(position);
        holder.binding.setJoke(joke);
        if (joke.content != null && joke.content.length() >= 2) {
            holder.binding.content.setVisibility(View.VISIBLE);
        } else {
            holder.binding.content.setVisibility(View.GONE);
        }
        if (joke.images != null && joke.images.size() >= 0) {
            holder.binding.image.setVisibility(View.VISIBLE);
            mRequestManager.load(ApiConst.JOKE_IMAGE_HEADER + joke.images.get(0).address).placeholder(R.drawable.image_loading).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.image);
        } else {
            holder.binding.image.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(data.get(position).id);
    }

    private OnItemClickListener mListener;
    public void setOnItemClickListener(OnItemClickListener l) {
        this.mListener = l;
    }

    /**
     *
     */
    public interface OnItemClickListener {
        public void onItemClick(JokeAdapter.ViewHolder holder, ImageView iv, Image[] images);
    }

    /**
     *
     */
    public class ViewHolder extends BindingRecyclerView.ViewHolder<ItemJokeBinding> {

        public ViewHolder(ItemJokeBinding binding) {
            super(binding);
            binding.image.setOnClickListener(v -> itemClick(this, (ImageView) v, data.get(getAdapterPosition()).images));
        }

        public void itemClick(JokeAdapter.ViewHolder holder, ImageView iv, List<Joke.Picture> images) {
            if (images != null && images.size() >= 1) {
                Image[] imgs = new Image[1];
                imgs[0] = new Image();
                imgs[0].url = ApiConst.JOKE_IMAGE_HEADER + images.get(0).address;
                if (mListener != null) {
                    mListener.onItemClick(holder, iv, imgs);
                }
            }
        }
    }

}
