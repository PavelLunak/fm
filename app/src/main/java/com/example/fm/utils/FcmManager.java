package com.example.fm.utils;

import android.util.Log;

import com.example.fm.MainActivity;
import com.example.fm.objects.NewLocation;
import com.example.fm.retrofit.ApiFcm;
import com.example.fm.retrofit.ControllerFcm;
import com.example.fm.retrofit.objects.ResponseToFcmData;
import com.example.fm.retrofit.objects.ResponseToFcmDataLocation;
import com.example.fm.retrofit.requests.ResponseToFcm;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FcmManager implements AppConstants {

    public static void sendResponse(ResponseToFcm response) {

        AppUtils.appendLog("MyFirebaseMessagingService - sendResponse()");
        AppUtils.appendLog(response.toString());


        if (response == null) return;

        ApiFcm apiFcm = ControllerFcm.getRetrofitInstance().create(ApiFcm.class);
        Call<ResponseBody> callFcm = apiFcm.sendResponse(response);

        callFcm.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG, "response code: " + response.code());
                if (response.isSuccessful()) {
                    Log.i(TAG, "isSuccessful: TRUE");

                    try {
                        Log.i(TAG, "response: " + response.body().string());
                    } catch (IOException e) {
                        Log.i(TAG, "response: IOException: ");
                        e.printStackTrace();
                    }
                } else {
                    Log.i(TAG, "isSuccessful: FALSE");
                    //showNotification("error " + response.code() + " " + DateTimeUtils.getDateTime(new Date()));

                    try {
                        Log.i(TAG, "response: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.i(TAG, "response: IOException: ");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "onFailure: ");
                Log.i(TAG, t.getMessage());
            }
        });
    }

    public static void sendMessage(String message, int batteryPercentages, String tokenForResponse) {
        ResponseToFcmData responseToFcmData = new ResponseToFcmData(
                FCM_RESPONSE_TYPE_MESSAGE,
                message,
                "" + batteryPercentages);

        ResponseToFcm response = new ResponseToFcm(tokenForResponse, responseToFcmData);
        sendResponse(response);
    }

    public static void sendResponseLocation(NewLocation newLocation, int batteryPercentages) {
        ResponseToFcmDataLocation responseDataLocation = new ResponseToFcmDataLocation(
                FCM_RESPONSE_TYPE_LOCATION,
                "",
                "" + batteryPercentages,
                "" + newLocation.getLatitude(),
                "" + newLocation.getLongitude(),
                "" + (newLocation.getSpeed() * 3.6),
                "" + newLocation.getAccuracy(),
                "" + newLocation.getDate());

        ResponseToFcm responseToFcm = new ResponseToFcm(MainActivity.tokenTest, responseDataLocation);
        FcmManager.sendResponse(responseToFcm);
    }
}
