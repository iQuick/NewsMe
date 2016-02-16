package me.imli.newme.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.imli.newme.model.Channel;
import me.imli.newme.ui.fragment.NewsChannelFragment;

/**
 * Created by Em on 2015/11/27.
 */
public class ChannelPagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private List<NewsChannelFragment> mFragments = new ArrayList<>();

    public ChannelPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
    }

    private void initFragment(List<Channel> channels) {
        mFragments.clear();
        notifyDataSetChanged();
        for (Channel c : channels) {
            if (c.show) {
                mFragments.add(NewsChannelFragment.newInstance(c.channelId, c.name));
            }
        }
    }

    public void onChange(List<Channel> channels) {
        initFragment(channels);
        notifyDataSetChanged();
    }

    @Override
    public NewsChannelFragment getItem(int position) {
//        Channel channel = Channel.getChannelList().get(position);
//        return ChannelFragment.newInstance(getContext(), channel.channelId, channel.name);
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getName();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public int getItemPosition(Object object) {
        if (object.getClass().getName().equals(NewsChannelFragment.class.getName())) {
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == ((Fragment) obj).getView();
    }

    public Context getContext() {
        return mContext;
    }
}
