package me.imli.newme.ui.fragment;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.FragmentMeiziBinding;
import me.imli.newme.model.Image;
import me.imli.newme.ui.adapter.MeiziAdapter;
import me.imli.newme.ui.base.BaseChanneFragment;
import me.imli.newme.utils.SexUtil;

/**
 *
 * MeiziFragment 福利
 * 当在设置里开启福利选项时，福利页面会打开
 * 本 Fragment 会展示与选择的性别相反的福利
 *
 * 女：帅哥  男：美女
 *
 * Created by Em on 2015/12/9.
 */
public class MeiziFragment extends BaseChanneFragment<FragmentMeiziBinding> {

    /**
     * TAG
     */
    public static final String TAG = "MeiziFragment";

    private ObservableList<Image> data;

    private static List<Image> boys = new ArrayList<>();
    private static List<Image> girls = new ArrayList<>();
    static {
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/91b9ee7d0c2c89fbf127af4810aab6fbf3cb90b8", 640, 960));
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/fc73cc4d4ec26d3f2b87d82b761aa9a2ffcdfd28", 600, 800));
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/5e003ed1114b5d6a174cda0d667ad00c4bd84d12", 960, 638));
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/129220540014217c3a94fcfc8b34c100a122a4d0", 638, 1024));
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/353c26fe682c1006b7a2c76f2e7c403fdc3f5d49", 1280, 850));
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/c4997735fe48e78cf7448c78f8924506ef06beef", 610, 889));
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/8aacefee70efbf3348e9e8b3c17450c96dd8b36d", 640, 960));
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/f8623cc679c38cf2f4c9b47b8e7b377de7122d87", 640, 960));
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/a1ce0bdce6073bb297556080fe400e6eb0acfbb8", 866, 609));
        girls.add(new Image("http://7xls0x.com1.z0.glb.clouddn.com/5af3928acc0692fb3ae3a2857614e56b6b5a0673", 960, 639));

        boys.add(new Image("http://imgcache.mysodao.com/img1/M07/9B/92/CgAPDE37UP6XrwT2AALy6zclxH0629_700x0x1.JPG", 686, 915));
        boys.add(new Image("http://imgsrc.baidu.com/forum/pic/item/8671ba20daddc6b72f73b3e3.jpg", 725, 544));
        boys.add(new Image("http://d.hiphotos.baidu.com/image/pic/item/58ee3d6d55fbb2fbe80cebc04a4a20a44623dc39.jpg", 1200, 1693));
        boys.add(new Image("http://img5.duitang.com/uploads/item/201501/13/20150113131306_XUzEV.thumb.700_0.jpeg", 700, 879));
        boys.add(new Image("http://cdn.duitang.com/uploads/item/201511/08/20151108084550_C4WFH.jpeg", 474, 669));
        boys.add(new Image("http://d.hiphotos.baidu.com/image/pic/item/10dfa9ec8a136327597b1ceb948fa0ec09fac750.jpg", 440, 553));
        boys.add(new Image("http://pic8.nipic.com/20100708/479029_195227717865_2.jpg", 1024, 683));
        boys.add(new Image("http://www.cnwnews.com/uploads/allimg/080723/0001350.jpg", 471, 600));
        boys.add(new Image("http://upload.ldnews.cn/2015/0326/1427355500573.jpg", 274, 400));
        boys.add(new Image("http://img2.imgtn.bdimg.com/it/u=1701623750,3927924046&fm=11&gp=0.jpg", 434, 523));
    }

    // 性别
    private int sex = -1;

    public static MeiziFragment newInstance() {
        MeiziFragment fragment = new MeiziFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int inflateLayout() {
        return R.layout.fragment_meizi;
    }

    @Override
    protected void initialization() {
        this.initData();
        this.initView();
        this.initObservable();
    }

    private void initData() {
        sex = SexUtil.getSex(getActivity());
        data = new ObservableArrayList<>();
        if (sex == 0) {
            data.addAll(boys);
        } else if (sex == 1) {
            data.addAll(girls);
        }
    }

    private void initView() {
        MeiziAdapter adapter = new MeiziAdapter(getActivity(), data, Glide.with(getActivity()));
        getBinding().content.setAdapter(adapter);
    }

    private void initObservable() {
    }

    @Override
    protected void createApi(ImApp app) {
    }


    @Override
    protected void onStopRefresh() {
//        getBinding().refresher.setRefreshing(false);
    }
}
