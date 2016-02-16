package me.imli.newme.ui;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import me.imli.newme.Const;
import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.ActivityCustomNewsBinding;
import me.imli.newme.helper.recycler.ItemTouchHelperAdapter;
import me.imli.newme.helper.recycler.OnStartDragListener;
import me.imli.newme.helper.recycler.SimpleItemTouchHelperCallback;
import me.imli.newme.model.Channel;
import me.imli.newme.ui.adapter.ChannelCustomAdapter;
import me.imli.newme.ui.base.BaseActivity;
import me.imli.newme.utils.LogUtils;
import me.imli.newme.widget.sb.SwipeBackLayout;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Em on 2015/12/29.
 */
public class CustomNewsActivity extends BaseActivity<ActivityCustomNewsBinding> implements SwipeBackLayout.OnSwipeBackListener, OnStartDragListener, ChannelCustomAdapter.OnChangeListener {

    /**
     * TAG
     */
    private static final String TAG = "CustomNewsActivity";

    private ItemTouchHelper mItemTouchHelper;
    private ChannelCustomAdapter adapter;
    private boolean isUpdate;

    @Override
    protected int inflateLayout() {
        return R.layout.activity_custom_news;
    }

    @Override
    protected void initialization() {
        this.initSwipeBack();
        this.initActionBar();
        this.initData();
        this.initRecycle();
        this.updateView();
    }

    private void initSwipeBack() {
        getBinding().swipe.setOnSwipeBackListener(this);
    }

    private void initActionBar() {
        setSupportActionBar(getBinding().toolbar);
        getBinding().toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());
    }

    private void initData() {
        isUpdate = false;
    }

    private void initRecycle() {
        adapter = new ChannelCustomAdapter(CustomNewsActivity.this);
        adapter.setOnStartDragListener(CustomNewsActivity.this);
        adapter.setOnChangeListener(CustomNewsActivity.this);
        getBinding().content.setAdapter(adapter);
        getBinding().content.setHasFixedSize(true);

        ItemTouchHelper.Callback callback = simpleItemTouch(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(getBinding().content);
    }

    @Override
    protected void createApi(ImApp app) {
    }

    private void updateView() {
        getApp().getDB().getList(Const.DB_NEWS_CHANNEL, Channel.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(channels -> {
                    adapter.onChange(channels);
                }, e -> LogUtils.e(TAG, "update channel error", e));
    }

    private SimpleItemTouchHelperCallback simpleItemTouch(ItemTouchHelperAdapter adapter) {
        return new SimpleItemTouchHelperCallback(adapter) {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                final int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }
        };
    }

    /**
     *
     */
    private void setUpdate() {
        getApp().getDB()
                .putList(Const.DB_NEWS_CHANNEL, adapter.getData(), Channel.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(avoid -> {
                            LogUtils.d(TAG, "save channels success");
                            setResult(Activity.RESULT_OK);
                        }, e -> LogUtils.e(TAG, getString(R.string.error), e), () -> super.supportFinishAfterTransition()
                );
    }

    @Override
    public void onBack() {
        onBackPressed();
        overridePendingTransition(0, R.anim.push_f_right_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void supportFinishAfterTransition() {
        if (isUpdate) {
            setUpdate();
        } else {
            super.supportFinishAfterTransition();
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onChange() {
        isUpdate = true;
    }
}
