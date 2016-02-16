package me.imli.newme.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import me.imli.newme.model.Image;
import me.imli.newme.ui.fragment.ViewerFragment;

/**
 * Created by Em on 2015/12/2.
 */
public class ViewerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private Image[] mImages;

    public ViewerAdapter(FragmentManager fm, Context context, Image[] images) {
        super(fm);
        this.mImages = images;
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Image image = mImages[position];
        return ViewerFragment.newInstance(image, image.url);
    }

    @Override
    public int getCount() {
        return mImages.length;
    }

}
