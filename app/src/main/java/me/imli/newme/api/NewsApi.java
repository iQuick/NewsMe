package me.imli.newme.api;

import me.imli.newme.model.News;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Em on 2015/11/26.
 */
public interface NewsApi {

    /**
     * 根据 ID 请求新闻列表
     * @param id
     * @return
     */
    @Headers("apikey: 2c61a1cd1f64216e92f7da1603697bf7")
    @GET(ApiConst.NEWS)
    Observable<News.NewsData> queryNewsByID(@Query("channelId") String id, @Query("page") int page);

    /**
     * 根据 ChannelName (标题)请求新闻列表
     * @param title
     * @return
     */
    @Headers("apikey: 2c61a1cd1f64216e92f7da1603697bf7")
    @GET(ApiConst.NEWS)
    Observable<News.NewsData> queryNewsByCName(@Query("channelName") String title, @Query("page") int page);

    /**
     * 根据 title (标题)请求新闻列表
     * @param title
     * @return
     */
    @Headers("apikey: 2c61a1cd1f64216e92f7da1603697bf7")
    @GET(ApiConst.NEWS)
    Observable<News.NewsData> queryNewsByTitle(@Query("title") String title, @Query("page") int page);

}
