package com.example.fm.retrofit;

import com.example.fm.retrofit.requests.RequestSendPositionsToDB;
import com.example.fm.retrofit.responses.ResponseAddPosition;
import com.example.fm.retrofit.responses.ResponseSendPositionsIntoDatabase;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiDatabase {

    @POST("checked_positions.php")
    Call<ResponseSendPositionsIntoDatabase> sendCheckedPositions(@Body RequestSendPositionsToDB request);

    /*
    @POST("checked_positions.php")
    @FormUrlEncoded
    Call<ResponseAddPosition> addCheckedPosition(
            @Field("id") String id,             //Pokud není null, jde o editaci
            @Field("nazev") String nazev,
            @Field("popis") String popis,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("proximity_watcher") int proximityWatcher,
            @Field("proximity_radius") int proximityRadius);
    */
}
