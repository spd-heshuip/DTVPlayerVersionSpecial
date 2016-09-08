package com.eardatek.player.dtvplayer.network;

import com.eardatek.player.dtvplayer.network.api.EardatekApi;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Administrator on 16-9-2.
 */
public class NetWork {

    private static EardatekApi eardatekApi;

    private static OkHttpClient okHttpClient = new OkHttpClient();

    private static Converter.Factory gsonFactory = GsonConverterFactory.create();
    private static CallAdapter.Factory rxJavaCallAdapterFactory = RxJavaCallAdapterFactory.create();
    private static Converter.Factory scalarsFactory = ScalarsConverterFactory.create();

    public static EardatekApi getEardatekApi(){
        if (eardatekApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl("http://www.eardatek.com/")
                    .addConverterFactory(scalarsFactory)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .build();
            eardatekApi = retrofit.create(EardatekApi.class);
        }

        return eardatekApi;
    }
}
