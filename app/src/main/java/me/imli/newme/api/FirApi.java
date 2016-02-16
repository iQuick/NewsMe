package me.imli.newme.api;

import me.imli.newme.model.Version;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Em on 2015/12/22.
 */
public interface FirApi {

    @GET(ApiConst.FIR_VERSION + "/" + ApiConst.FIR_ME_ID)
    Observable<Version> latest(@Query("api_token") String token);


}
