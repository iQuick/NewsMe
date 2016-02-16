package me.imli.newme.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Em on 2015/11/26.
 */
public class Channel {

    private static final List<Channel> mChannels = new ArrayList<>();
    static {
//        mChannels.add(new Channel("5572a108b3cdc86cf39001cd", "国内焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001ce", "国际焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001cf", "军事焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d0", "财经焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d1", "互联网焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d2", "房产焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d3", "汽车焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d4", "体育焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d5", "娱乐焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d6", "游戏焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d7", "教育焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d8", "女人焦点"));
//        mChannels.add(new Channel("5572a108b3cdc86cf39001d9", "科技焦点"));
//        mChannels.add(new Channel("5572a109b3cdc86cf39001da", "社会焦点"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001db", "国内最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001dc", "台湾最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001dd", "港澳最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001de", "国际最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001df", "军事最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001e0", "财经最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001e1", "理财最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001e2", "宏观经济最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001e3", "互联网最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001e4", "房产最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001e5", "汽车最新"));
        mChannels.add(new Channel("5572a109b3cdc86cf39001e6", "体育最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001e7", "国际足球最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001e8", "国内足球最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001e9", "CBA最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001ea", "综合体育最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001eb", "娱乐最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001ec", "电影最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001ed", "电视最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001ee", "游戏最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001ef", "教育最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001f0", "女人最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001f1", "美容护肤最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001f2", "情感两性最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001f3", "健康养生最新"));
        mChannels.add(new Channel("5572a10ab3cdc86cf39001f4", "科技最新"));
        mChannels.add(new Channel("5572a10bb3cdc86cf39001f5", "数码最新"));
        mChannels.add(new Channel("5572a10bb3cdc86cf39001f6", "电脑最新"));
        mChannels.add(new Channel("5572a10bb3cdc86cf39001f7", "科普最新"));
        mChannels.add(new Channel("5572a10bb3cdc86cf39001f8", "社会最新"));
    }

    /**
     * channelId : 5572a108b3cdc86cf39001cd
     * name : 国内焦点
     */
    public String channelId;
    public String name;
    public boolean show;

    public Channel(String id, String name) {
        this.channelId = id;
        this.name = name;
        this.show = true;
    }

    /**
     * 交换 Channel 位置
     * @param first
     * @param second
     */
    public static void exchangeChannelPosition(int first, int second) {
        Channel channelF = mChannels.get(first);
        Channel channelS = mChannels.get(second);
        mChannels.set(first, channelS);
        mChannels.set(second, channelF);
    }

    /**
     * 设置是否显示
     * @param show
     * @param position
     */
    public static void setChannelShow(boolean show, int position) {
        mChannels.get(position).show = show;
    }

    /**
     * 添加 Channel
     * @param channel
     * @param position
     */
    public static void addChannel(Channel channel, int position) {
        mChannels.add(position, channel);
    }


    /**
     * 添加 Channel
     * @param channel
     */
    public static void addChannel(Channel channel) {
        mChannels.add(channel);
    }

    /**
     * 删除 Channel
     * @param channel
     */
    public static void removeChannel(Channel channel) {
        mChannels.remove(channel);
    }

    /**
     * 删除 Channel
     * @param position
     */
    public static void removeChannel(int position) {
        mChannels.remove(position);
    }

    /**
     * 根据 id 获取 Channel
     * @param id
     * @return
     */
    public static Channel getChannelById(String id) {
        Channel channel = null;
        for (Channel c : mChannels) {
            if (id.equals(c.channelId)) {
                channel = c;
            }
        }
        return channel;
    }

    /**
     * 根据位置获取 Channel
     * @param position
     * @return
     */
    public static Channel getChannelByPosition(int position) {
        return mChannels.get(position);
    }

    /**
     * 获取 Channel 列表
     * 获取 Channel 列表
     * @return
     */
    public static List<Channel> getChannelList() {
        return mChannels;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "channelId='" + channelId + '\'' +
                ", name='" + name + '\'' +
                ", show=" + show +
                '}';
    }
}
