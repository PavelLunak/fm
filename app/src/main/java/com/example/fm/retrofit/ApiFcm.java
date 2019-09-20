package com.example.fm.retrofit;

import com.example.fm.retrofit.requests.ResponseToFcm;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiFcm {

    @Headers({"Authorization: key=AAAA8QnanpM:APA91bFCQBcx9bsqtkKLoLut1s6ljUXuWXHUQ3EK5id70CgDQAWWLIcjHFKKr9bHH7yukANefSVP4AV8-_Iyswlm7fv7Rc5Xx3ApDStEVY4tuR0_j_oXz0eirjNKojGTL0yhBIC-OHay",
            "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendResponse(@Body ResponseToFcm responseToFcm);
}
