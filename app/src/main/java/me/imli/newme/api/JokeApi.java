package me.imli.newme.api;

import rx.Observable;

import me.imli.newme.model.Joke;
import retrofit.http.GET;
import retrofit.http.Query;


/**
 * Created by Em on 2015/12/10.
 */
public interface JokeApi {

    @GET(ApiConst.JOKE_JOKE)
    Observable<Joke.JokeData> joke(@Query("a") String a, @Query("p") int page);

}
