package me.imli.newme.ui;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.ActivityFeedbackBinding;
import me.imli.newme.ui.base.BaseActivity;
import me.imli.newme.widget.sb.SwipeBackLayout;

/**
 * Created by Em on 2015/12/23.
 */
public class FeedbackActivity extends BaseActivity<ActivityFeedbackBinding> implements SwipeBackLayout.OnSwipeBackListener {


    @Override
    protected int inflateLayout() {
        return R.layout.activity_feedback;
    }

    @Override
    protected void initialization() {
        this.initSwipeBack();
        this.initActionBar();
    }

    private void initSwipeBack() {
        getBinding().swipe.setOnSwipeBackListener(this);
    }

    private void initActionBar() {
        setSupportActionBar(getBinding().toolbar);
        getBinding().toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());
    }

    @Override
    protected void createApi(ImApp app) {
    }

    private void doSend() {
        Toast.makeText(this, R.string.success_feedback, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                doSend();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBack() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.push_f_right_out);
    }
}
