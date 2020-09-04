package com.example.fm.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fm.MainActivity;
import com.example.fm.objects.Device;
import com.example.fm.objects.NewLocation;
import com.example.fm.retrofit.ApiDatabase;
import com.example.fm.retrofit.ApiFcm;
import com.example.fm.retrofit.ControllerDatabase;
import com.example.fm.retrofit.ControllerFcm;
import com.example.fm.retrofit.objects.ResponseToFcmData;
import com.example.fm.retrofit.objects.ResponseToFcmDataLocation;
import com.example.fm.retrofit.requests.ResponseToFcm;
import com.example.fm.retrofit.responses.ResponseNewDevice;

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
                if (!response.isSuccessful()) {
                    try {
                        AppUtils.appendLog("sendResponse() - CHYBA:");
                        AppUtils.appendLog(response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                AppUtils.appendLog("sendResponse() - CHYBA:");
                AppUtils.appendLog(t.getMessage());
            }
        });
    }

    public static void sendMessage(
            Context context,
            String message,
            int batteryPercentages,
            int batteryPlugged,
            String tokenForResponse,
            int actionCode) {

        ResponseToFcmData responseToFcmData = new ResponseToFcmData(
                FCM_RESPONSE_TYPE_MESSAGE,
                PrefsUtils.getPrefsToken(context),
                PrefsUtils.getPrefsDatabaseId(context),
                PrefsUtils.getAndroidId(context),
                message,
                "" + batteryPercentages,
                batteryPlugged,
                actionCode);

        ResponseToFcm response = new ResponseToFcm(tokenForResponse, responseToFcmData);
        sendResponse(response);
    }

    public static void sendResponseLocation(Context context, NewLocation newLocation, int batteryPercentages, int batteryPlugged) {
        ResponseToFcmDataLocation responseDataLocation = new ResponseToFcmDataLocation(
                FCM_RESPONSE_TYPE_LOCATION,
                PrefsUtils.getPrefsToken(context),
                PrefsUtils.getPrefsDatabaseId(context),
                PrefsUtils.getAndroidId(context),
                "",
                "" + batteryPercentages,
                batteryPlugged,
                ACTION_MESSAGE_CODE_NONE,
                "" + newLocation.getLatitude(),
                "" + newLocation.getLongitude(),
                "" + (newLocation.getSpeed() * 3.6),
                "" + newLocation.getAccuracy(),
                "" + newLocation.getDate());

        ResponseToFcm responseToFcm = new ResponseToFcm(MainActivity.tokenForResponse, responseDataLocation);
        FcmManager.sendResponse(responseToFcm);
    }

    public static void registerDevice(final Context context, Device device, String newToken) {
        Log.d(TAG_DB, "FcmManager - registerDevice()");
        ApiDatabase api = ControllerDatabase.getRetrofitInstance().create(ApiDatabase.class);
        final Call<ResponseNewDevice> call = api.addDevice(device);

        call.enqueue(new Callback<ResponseNewDevice>() {
            @Override
            public void onResponse(Call<ResponseNewDevice> call, Response<ResponseNewDevice> response) {

                boolean isRegistered = false;

                if (response.isSuccessful()) {
                    Log.d(TAG_DB, "FcmManager - isSuccessful == TRUE");
                    Log.d(TAG_DB, "CODE: " + response.code());

                    /*
                    try {
                        Log.d(TAG_DB, response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    */

                    if (response.body() != null) {
                        ResponseNewDevice responseNewDevice = response.body();
                        String message = null;

                        if (responseNewDevice.getMessage() != null) {
                            message = responseNewDevice.getMessage();
                        } else {
                            isRegistered = true;
                        }

                        Intent intent = new Intent(ACTION_REGISTRATION);
                        intent.putExtra(EXTRA_REGISTRATION, isRegistered);
                        intent.putExtra(EXTRA_MESSAGE, message);
                        intent.putExtra(EXTRA_DB_DEVICE_ID, responseNewDevice.getNewDeviceId());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                    } else {
                        Intent intent = new Intent(ACTION_REGISTRATION);
                        intent.putExtra(EXTRA_REGISTRATION, false);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                } else {
                    Log.d(TAG_DB, "FcmManager - isSuccessful == FALSE");

                    try {
                        Log.d(TAG_DB, response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(ACTION_REGISTRATION);
                    intent.putExtra(EXTRA_REGISTRATION, false);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<ResponseNewDevice> call, Throwable t) {
                Log.d(TAG_DB, "FcmManager - onFailure()");
                Log.d(TAG_DB, t.getMessage());
                Intent intent = new Intent(ACTION_REGISTRATION);
                intent.putExtra(EXTRA_REGISTRATION, false);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }
}
