package com.example.fm.retrofit;

import com.example.fm.retrofit.requests.RequestSendPosition;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiFcm {

    @Headers({"Authorization: key=AAAACyCfeOI:APA91bE4yyogyzWajjm7aPq4fJbd_xS36LXoJO9h6LbA2_KZa4VHbBF3LJhH4M9sRe3X_IAKWcyUU01v1WN7FuVyjUiF77XUms41Bh2O-1pr3VM_C5TOO7V8r_jR8vcXykZT4bRfSiEl",
            "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendLocation(@Body RequestSendPosition requestSendPosition);
}
