package me.imli.newme.ui.adapter;

import android.content.Context;
import android.databinding.ObservableList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import me.imli.newme.R;
import me.imli.newme.databinding.ItemNewsBinding;
import me.imli.newme.model.Image;
import me.imli.newme.model.News;
import ooo.oxo.library.databinding.support.widget.BindingRecyclerView;

/**
 * Created by Em on 2015/11/27.
 */
public class NewsAdapter extends BindingRecyclerView.ListAdapter<News, NewsAdapter.ViewHolder> {

    private final RequestManager mRequestManager;

    public NewsAdapter(Context context, ObservableList<News> data, RequestManager requestManager) {
        super(context, data);
        this.mRequestManager = requestManager;
        this.setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemNewsBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        News news = data.get(position);
        holder.binding.setNews(news);
        if (news.imageurls.length >= 1) {
            holder.binding.image.setVisibility(View.VISIBLE);
            mRequestManager.load(news.imageurls[0].url).placeholder(R.drawable.image_loading).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.image);
        } else {
            holder.binding.image.setVisibility(View.GONE);
        }
        // execute the binding immediately to ensure the original size of RatioImageView is set
        // before layout
        holder.binding.executePendingBindings();
    }

//    @Override
//    public int getItemViewType(int position) {
//        NewsData.News news = data.get(position);
//        return Math.round((float) news.meta.width / (float) news.meta.height * 10f);
//    }

    private OnItemClickListener mListener;
    public void setOnItemClickListener(OnItemClickListener l) {
        this.mListener = l;
    }

    /**
     *
     */
    public interface OnItemClickListener {
        public void onItemClick(ViewHolder holder, News news);
        public void onImageClick(ViewHolder holder, ImageView iv, Image[] image, int index);
    }


    @Override
    public long getItemId(int position) {
        return data.get(position).id;
    }

    /**
     * BindingRecyclerView
     */
    public class ViewHolder extends BindingRecyclerView.ViewHolder<ItemNewsBinding> {

        public ViewHolder(ItemNewsBinding binding) {
            super(binding);
            itemView.setOnClickListener(v -> itemClick(data.get(getAdapterPosition())));
            binding.image.setOnClickListener(v -> imageClick((ImageView) v, data.get(getAdapterPosition()).imageurls, 0));
        }

        public void itemClick(News news) {
            if (mListener != null) {
                mListener.onItemClick(this, news);
            }
        }

        public void imageClick(ImageView iv, Image[] images, int index) {
            if (mListener != null && images.length >= 1) {
                mListener.onImageClick(this, iv, images, index);
            }
        }
    }
}
