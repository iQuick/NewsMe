package me.imli.newme.ui.fragment;

import android.content.res.Configuration;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sina.sinavideo.sdk.VDVideoExtListeners;
import com.sina.sinavideo.sdk.VDVideoView;
import com.sina.sinavideo.sdk.VDVideoViewController;
import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.sina.sinavideo.sdk.data.VDVideoListInfo;
import com.sina.sinavideo.sdk.utils.VDVideoFullModeController;
import com.sina.sinavideo.sdk.widgets.playlist.VDVideoPlayListContainer;

import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.FragmentVideoBinding;
import me.imli.newme.model.Video;
import me.imli.newme.ui.adapter.VideoAdapter;
import me.imli.newme.ui.base.BaseChanneFragment;
import me.imli.newme.utils.LogUtils;

/**
 * Created by Em on 2015/12/9.
 */
public class VideoFragment extends BaseChanneFragment<FragmentVideoBinding> implements VDVideoExtListeners.OnVDVideoFrameADListener, VDVideoExtListeners.OnVDVideoInsertADListener, VDVideoExtListeners.OnVDVideoPlaylistListener {
    /**
     * TAG
     */
    public static final String TAG = "VideoFragment";

    // Video
    private View videoContainer;
    private VDVideoView videoView;
    private VDVideoPlayListContainer mVDVideoPlayListContainer;

    private ObservableArrayList<Video> observableVideos;

    public static VideoFragment newInstance() {
        VideoFragment fragment = new VideoFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int inflateLayout() {
        return R.layout.fragment_video;
    }

    @Override
    protected void initialization() {
        this.initData();
        this.initVideoView();
        this.initView();
        this.initObservable();
    }

    private void initData() {
        observableVideos = new ObservableArrayList<>();
        for (int i = 0; i < 10; i++) {
            observableVideos.add(createVideos(i));
        }
    }

    private void initView() {
        VideoAdapter adapter = new VideoAdapter(getActivity(), this, observableVideos);
        getBinding().content.setAdapter(adapter);
    }

    private void initVideoView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                videoContainer = LayoutInflater.from(getActivity()).inflate(R.layout.video_layout, null);
                videoView = (VDVideoView) videoContainer.findViewById(R.id.vdvv);
                videoView.setVDVideoViewContainer((ViewGroup) videoView.getParent());
                mVDVideoPlayListContainer = (VDVideoPlayListContainer) videoContainer.findViewById(R.id.playlist1);
                registerListener();
            }
        }, 300);
    }

    private void initObservable() {
    }

    @Override
    protected void createApi(ImApp app) {
    }

    private void registerListener() {
        if (VDVideoViewController.getInstance(getActivity()) != null) {
            VDVideoViewController.getInstance(getActivity()).getExtListener().setFrameADListener(this);
            VDVideoViewController.getInstance(getActivity()).getExtListener().setInsertADListener(this);
            VDVideoViewController.getInstance(getActivity()).getExtListener().setPlaylistListener(this);
        }
    }

    public Video createVideos(int position) {
        String title = "test";
        String img = "http://n.sinaimg.cn/default/20150525/xqHu-avxeafs8051553.jpg";
        String url = "http://v.iask.com/v_play_ipad.php?vid=138116139";
        Video video = new Video(title, img, url);
        return video;
    }

    public View getContainer() {
        return videoContainer;
    }

    public void itemClick(int position, VDVideoListInfo lists) {
        videoView.open(getActivity(), lists);
        videoView.play(0);

        for (Video video : observableVideos) {
            video.isShow = false;
        }
        if (position < observableVideos.size()) {
            observableVideos.get(position).isShow = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (VDVideoViewController.getInstance(getActivity()) != null && VDVideoViewController.getInstance(getActivity()).getVideoInfoNum() > 0) {
            VDVideoViewController.getInstance(getActivity()).onStart();
        }
    }


    public void onResume() {
        super.onResume();
        if (VDVideoViewController.getInstance(getActivity()) != null && VDVideoViewController.getInstance(getActivity()).getVideoInfoNum() > 0) {
            VDVideoViewController.getInstance(getActivity()).onResume();
        }
    }

    public void onPause() {
        super.onPause();
        if (VDVideoViewController.getInstance(getActivity()) != null) {
            VDVideoViewController.getInstance(getActivity()).onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (VDVideoViewController.getInstance(getActivity()) != null) {
            VDVideoViewController.getInstance(getActivity()).onStop();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (VDVideoViewController.getInstance(getActivity()) != null) {
                VDVideoViewController.getInstance(getActivity()).onPause();
            }
        } else {
            if (VDVideoViewController.getInstance(getActivity()) != null && VDVideoViewController.getInstance(getActivity()).getVideoInfoNum() > 0) {
                VDVideoViewController.getInstance(getActivity()).onResume();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (videoView != null) {
                videoView.setIsFullScreen(true);
                LogUtils.e(VDVideoFullModeController.TAG, "onConfigurationChanged---ORIENTATION_LANDSCAPE");
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (videoView != null) {
                videoView.setIsFullScreen(false);
                LogUtils.e(VDVideoFullModeController.TAG, "onConfigurationChanged---ORIENTATION_PORTRAIT");
            }
        }
    }

    public boolean holdGoBack() {
        boolean isLandscape = !VDVideoFullModeController.getInstance().getIsPortrait();
        if (isLandscape) {
            return true;
        }
        return false;
    }

    public boolean onBack(KeyEvent event) {
        if (VDVideoViewController.getInstance(getActivity()) != null && !VDVideoViewController.getInstance(getActivity()).onKeyEvent(event)) {
            return false;
        }
        return true;
    }

    public void onDestroy() {
        videoView.release(false);
        super.onDestroy();
    }

    @Override
    public void onFrameADPrepared(VDVideoInfo info) {
    }

    @Override
    public void onInsertADClick(VDVideoInfo info) {
    }

    @Override
    public void onInsertADStepOutClick(VDVideoInfo info) {
    }

    @Override
    public void onPlaylistClick(VDVideoInfo info, int p) {
        videoView.play(p);
    }

    @Override
    protected void onStopRefresh() {
        getBinding().refresher.setRefreshing(false);
    }
}
