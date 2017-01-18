package com.eardatek.special.player.network.api;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by Administrator on 16-9-2.
 */
public interface EardatekApi {
    @GET("UploadFiles/dtvboxupdate/update.asp")
    Observable<String> getVersionInfo();
}
