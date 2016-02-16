package me.imli.newme.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Em on 2015/12/10.
 */
public class Joke extends BaseModel {

    /** ID */
    @SerializedName("id")
    public String id;
    /** 专题ID */
    @SerializedName("zt")
    public String zId;
    /** 发布人ID */
    @SerializedName("member_id")
    public String mID;
    /** 标题 */
    @SerializedName("title")
    public String title;
    /** 专题标题 */
    @SerializedName("zt_title")
    public String zTitle;
    /** 发布人名称 */
    @SerializedName("member_byname")
    public String name;
    /** 发布人头像 */
    @SerializedName("member_logo")
    public String avatar;
    /** 评论数 */
    @SerializedName("comment_nums")
    public String cNum;
    /** 顶数 */
    @SerializedName("up")
    public String upNum;
    /** 点击/查看数 */
    @SerializedName("click_sum")
    public String clickNum;
    /** 发布时间 */
    @SerializedName("add_time")
    public String time;

    //============ 预留字段
    public String content;
    //	public String html;
    public String video;
    //============ 预留字段

    @SerializedName("pic")
    public List<Picture> images;

    public static class Picture {
        /** 图片地址 */
        public String address;
        public int width;
        public int height;
        public int type;
    }

    public static class JokeData {
        /** 数据 */
        public List<Joke> data;
        /** 页面 */
        public int page;
        public JokeData() {
            this.page = 1;
            this.data = new ArrayList<Joke>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Joke joke = (Joke) o;

        if (id != null ? !id.equals(joke.id) : joke.id != null) return false;
        if (zId != null ? !zId.equals(joke.zId) : joke.zId != null) return false;
        return !(title != null ? !title.equals(joke.title) : joke.title != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (zId != null ? zId.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
