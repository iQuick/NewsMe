package me.imli.newme.ui.adapter;

import android.content.Context;
import android.databinding.ObservableList;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import me.imli.newme.R;
import me.imli.newme.databinding.ItemMeiziBinding;
import me.imli.newme.model.Image;
import ooo.oxo.library.databinding.support.widget.BindingRecyclerView;

/**
 * Created by Em on 2016/1/6.
 */
public class MeiziAdapter extends BindingRecyclerView.ListAdapter<Image, MeiziAdapter.ViewHolder> {

    private final RequestManager mRequestManager;

    public MeiziAdapter(Context context, ObservableList<Image> data, RequestManager manager) {
        super(context, data);
        this.mRequestManager = manager;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMeiziBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Image image = data.get(position);
        holder.binding.setImage(image);
        holder.binding.image.setOriginalSize(image.width, image.height);
        holder.binding.executePendingBindings();
        mRequestManager.load(image.url).placeholder(R.drawable.image_loading).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.image);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     *
     */
    public class ViewHolder extends BindingRecyclerView.ViewHolder<ItemMeiziBinding> {

        public ViewHolder(ItemMeiziBinding binding) {
            super(binding);
        }
    }
}
