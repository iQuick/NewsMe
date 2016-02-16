package me.imli.newme.model;

/**
 * Created by Em on 2016/1/4.
 */
public class Video extends BaseModel {
    public String id;
    public String title;
    public String img;
    public String url;
    public boolean isShow;

    public Video(String title, String img, String url) {
        this("0", title, img, url);
    }

    public Video(String id, String title, String img, String url) {
        this.id = id;
        this.title = title;
        this.img = img;
        this.url = url;
        this.isShow = false;
    }
}
