package me.imli.newme.ui.adapter;

import android.content.Context;
import android.databinding.ObservableList;
import android.view.View;
import android.view.ViewGroup;

import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.sina.sinavideo.sdk.data.VDVideoListInfo;

import me.imli.newme.databinding.ItemVideoBinding;
import me.imli.newme.model.Video;
import me.imli.newme.ui.fragment.VideoFragment;
import ooo.oxo.library.databinding.support.widget.BindingRecyclerView;

/**
 * Created by Em on 2015/12/31.
 */
public class VideoAdapter extends BindingRecyclerView.ListAdapter<Video, VideoAdapter.ViewHolder> {

    private VideoFragment mVideoFragment;

    public VideoAdapter(Context context, VideoFragment fragment, ObservableList<Video> data) {
        super(context, data);
        this.mVideoFragment = fragment;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemVideoBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Video video = data.get(position);
        holder.listener.setData(holder.binding.videoLayout, video.title, video.url, position);
        holder.binding.ivVideoPlayicon.setOnClickListener(holder.listener);
        if (video.isShow) {
            holder.binding.ivVideoPlayicon.setVisibility(View.GONE);
            holder.binding.ivVideoThumb.setVisibility(View.GONE);
        } else {
            holder.binding.ivVideoPlayicon.setVisibility(View.VISIBLE);
            holder.binding.ivVideoThumb.setVisibility(View.VISIBLE);
        }
        // 添加视频 View
        if (video.isShow) {
            View container = getVideoFragment().getContainer();
            if (container == null || holder.binding.videoLayout == null) {
                return;
            }
            if (container.getParent() != null) {
                ((ViewGroup) container.getParent()).removeAllViews();
            }
            holder.binding.videoLayout.addView(container);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public VideoAdapter getAdapter() {
        return this;
    }

    public VideoFragment getVideoFragment() {
        return mVideoFragment;
    }

    /**
     *
     */
    public class ViewHolder extends BindingRecyclerView.ViewHolder<ItemVideoBinding> {

        public OnVideoClickListener listener;

        public ViewHolder(ItemVideoBinding binding) {
            super(binding);
            this.listener = new OnVideoClickListener(getAdapter());
        }
    }

    /**
     * OnVideoClickListener
     */
    public class OnVideoClickListener implements View.OnClickListener {

        private VideoAdapter adapter;

        private String title;
        private String url;
        private ViewGroup parent;
        private int position;

        public OnVideoClickListener(VideoAdapter adapter) {
            this.adapter = adapter;
        }

        public void setData(ViewGroup parent, String title, String url, int position) {
            this.parent = parent;
            this.title = title;
            this.url = url;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            View container = getVideoFragment().getContainer();
            if (container == null || this.parent == null) {
                return;
            }
            if (container.getParent() != null) {
                ((ViewGroup) container.getParent()).removeAllViews();
            }
            this.parent.addView(container);

            VDVideoListInfo infoList = new VDVideoListInfo();
            VDVideoInfo info = new VDVideoInfo();
            info = new VDVideoInfo();
            info.mTitle = title;
            info.mPlayUrl = url;
            infoList.addVideoInfo(info);
            getVideoFragment().itemClick(position, infoList);
            adapter.notifyDataSetChanged();
        }
    }
}
