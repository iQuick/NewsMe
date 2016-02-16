package me.imli.newme.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Em on 2015/12/10.
 */
public class News {

    public long id;
    public String channelId;
    public String channelName;
    public int chinajoy;
    public String desc;
    public String link;
    public String nid;
    public String pubDate;
    public String source;
    public String title;
    public Image[] imageurls;

    public News() {
        id = Long.valueOf(System.nanoTime() +  "0" + (int)(Math.random() * 100));
    }

    /**
     * NewsData
     */
    public static class NewsData {

        public int allNum;
        public int currentPage;
        public int allPages;
        public int maxResult;
        public List<News> contentlist;

        public NewsData() {
            contentlist = new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        News news = (News) o;

        if (channelId != null ? !channelId.equals(news.channelId) : news.channelId != null)
            return false;
        if (nid != null ? !nid.equals(news.nid) : news.nid != null) return false;
        return !(title != null ? !title.equals(news.title) : news.title != null);

    }

    @Override
    public int hashCode() {
        int result = channelId != null ? channelId.hashCode() : 0;
        result = 31 * result + (nid != null ? nid.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

}
