package com.example.fm.retrofit;

import com.example.fm.objects.Device;
import com.example.fm.retrofit.requests.RequestSendPositionsToDB;
import com.example.fm.retrofit.responses.ResponseNewDevice;
import com.example.fm.retrofit.responses.ResponseSendPositionsIntoDatabase;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiDatabase {

    @POST("checked_positions.php")
    Call<ResponseSendPositionsIntoDatabase> sendCheckedPositions(@Body RequestSendPositionsToDB request);

    @POST("devices.php")
    Call<ResponseNewDevice> addDevice(@Body Device device);
}
