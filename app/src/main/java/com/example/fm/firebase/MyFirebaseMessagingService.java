package com.example.fm.firebase;

import android.Manifest;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.fm.MainActivity;
import com.example.fm.R;
import com.example.fm.objects.NewLocation;
import com.example.fm.retrofit.ApiFcm;
import com.example.fm.retrofit.ControllerFcm;
import com.example.fm.retrofit.objects.ResponseToFcmData;
import com.example.fm.retrofit.objects.ResponseToFcmDataLocation;
import com.example.fm.retrofit.requests.ResponseToFcm;
import com.example.fm.services.LocationMonitoringService;
import com.example.fm.utils.AppConstants;
import com.example.fm.utils.AppUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements AppConstants {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest = new LocationRequest();
    private LocationCallback mLocationCallback;

    private boolean mBounded;
    private LocationMonitoringService mServiceNew;

    private BroadcastReceiver locacionReceiver;
    private BroadcastReceiver restartServiceReceiver;
    private BroadcastReceiver serviceDestroyedReceiver;
    private BroadcastReceiver serviceStartetReceiver;
    private BroadcastReceiver serviceOnUnbindReceiver;

    private NewLocation newLocation;

    private int requestType = 0;
    private String tokenForResponse = "";

    boolean serviceIsStarted, gpsIsStarted;
    Intent mServiceIntent;

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            AppUtils.appendLog("MainActivity - ServiceConnection - onServiceDisconnected");
            mBounded = false;
            mServiceNew = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppUtils.appendLog("MainActivity - ServiceConnection - onServiceConnected");

            LocationMonitoringService.LocalBinderNewVersion mLocalBinder = (LocationMonitoringService.LocalBinderNewVersion) service;
            mServiceNew = mLocalBinder.getService();
            mBounded = true;
            if (gpsIsStarted) mServiceNew.startGps();

            ResponseToFcmData responseToFcmData = new ResponseToFcmData(FCM_RESPONSE_SERVICE_START, "Služba je zapnuta.");
            ResponseToFcm response = new ResponseToFcm(tokenForResponse, responseToFcmData);
            sendResponse(response);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        registerLocationReceiver();
        registerRetartServiceReceiver();
        registerServiceDestroyedReceiver();
        registerServiceOnUnbindReceiver();
        registerServiceStartedReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterLocationReceiver();
        unregisterRetartServiceReceiver();
        unregisterServiceDestroyedReceiver();
        unregisterServiceOnUnbindReceiver();
        unregisterServiceStartetReceiver();
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                MainActivity.appPrefs.edit().fcmToken().put(newToken).apply();
                Log.i(TAG, "new token: " + newToken);
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.i(TAG, "MyFirebaseMessagingService - onMessageReceived");

        if (remoteMessage != null) {
            /*
            Log.i(TAG, "MessageType: " + remoteMessage.getMessageType());
            Log.i(TAG, "CollapseKey: " + remoteMessage.getCollapseKey());
            Log.i(TAG, "From: " + remoteMessage.getFrom());
            Log.i(TAG, "MessageId: " + remoteMessage.getMessageId());
            Log.i(TAG, "To: " + remoteMessage.getTo());
            Log.i(TAG, "Priority: " + remoteMessage.getPriority());
            Log.i(TAG, "SentTime: " + DateTimeUtils.getDateTime(remoteMessage.getSentTime()));
            Log.i(TAG, "Ttl: " + remoteMessage.getTtl() / 60);
            */

            Map<String, String> data = remoteMessage.getData();

            if (data != null) {
                if (!data.isEmpty()) {
                    for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                        Log.i(TAG, "key : " + entry.getKey() + ", value : " + entry.getValue());
                    }

                    try {
                        if (data.containsKey(KEY_REQUEST_TYPE)) requestType = Integer.parseInt(data.get(KEY_REQUEST_TYPE));
                    } catch (NumberFormatException e) {
                        Log.i(TAG, "NumberFormatException: " + e.getMessage());
                        return;
                    }

                    if (data.containsKey(KEY_TOKEN_FOR_RESPONSE)) tokenForResponse = data.get(KEY_TOKEN_FOR_RESPONSE);

                    Log.i(TAG, "requestType: " + AppUtils.requestTypeToString(requestType));
                    Log.i(TAG, "tokenForResponse: " + tokenForResponse);

                    ResponseToFcmData responseData;
                    ResponseToFcm responseToFcm;

                    switch (requestType) {
                        case FCM_REQUEST_TYPE_SERVICE_STATUS:
                            responseData = new ResponseToFcmData(serviceIsStarted ? FCM_RESPONSE_SERVICE_STATUS_STARTED : FCM_RESPONSE_SERVICE_STATUS_STOPED, "");
                            responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                            sendResponse(responseToFcm);
                            break;
                        case FCM_REQUEST_TYPE_SERVICE_START:
                            startService();
                            break;
                        case FCM_REQUEST_TYPE_SERVICE_STOP:
                            stopService();
                            break;
                        case FCM_REQUEST_TYPE_GPS_START:
                            startLocationWatcher();
                            responseData = new ResponseToFcmData(FCM_RESPONSE_GPS_START, "Start GPS");
                            responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                            sendResponse(responseToFcm);
                            break;
                        case FCM_REQUEST_TYPE_GPS_STOP:
                            if (mServiceNew != null) mServiceNew.stopGps();
                            responseData = new ResponseToFcmData(FCM_RESPONSE_GPS_STOP, "Stop GPS");
                            responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                            sendResponse(responseToFcm);
                            break;
                    }
                } else {
                    Log.i(TAG, "remoteMessage.getData().isEmpty()");
                }
            } else {
                Log.i(TAG, "remoteMessage.getData() == null");
            }
        } else {
            Log.i(TAG, "remoteMessage == null");
        }
    }

    public void sendResponse(ResponseToFcm response) {

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
                    //showNotification(DateTimeUtils.getDateTime(new Date()));

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
                //showNotification("onFailure " + DateTimeUtils.getDateTime(new Date()));
            }
        });
    }

    public void startService() {
        Log.i(TAG, "Start/Stop service from menu - serviceIsStarted == FALSE");

        if (!isMyServiceRunning(LocationMonitoringService.class)) {
            mServiceNew = new LocationMonitoringService(getApplicationContext());
            mServiceIntent = new Intent(this, mServiceNew.getClass());
            startService(mServiceIntent);
        }

        bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
    }

    public void stopService() {
        Log.i(TAG, "Start/Stop service from menu - serviceIsStarted == TRUE");

        gpsIsStarted = false;

        if (mServiceNew != null) mServiceNew.setRequestStopService();

        if (mConnection != null) {
            if (mBounded) {
                try {
                    Log.i(TAG, "Start/Stop service from menu - unbindService(mConnection)...");
                    unbindService(mConnection);
                } catch (IllegalArgumentException e) {
                    Log.i(TAG, "Start/Stop service from menu - unbindService(mConnection)... SELHALO");
                    Log.i(TAG, e.getMessage());
                }
            }
        } else {
            Log.i(TAG, "Start/Stop service from menu - mConnection == null");
        }

        if (mServiceNew != null) {
            mServiceNew.stopSelf();
        } else {
            Log.i(TAG, "Start/Stop service from menu - mServiceNew == NULL -> nemůžu dát požadavek na stop");
        }

        Log.i(TAG, "Start/Stop service from menu - is running : " + isMyServiceRunning(LocationMonitoringService.class));
    }

    public void startLocationWatcher() {
        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {
            if (checkPermissions()) {
                gpsIsStarted = true;

                if (!isMyServiceRunning(LocationMonitoringService.class)) {
                    mServiceNew = new LocationMonitoringService(getApplicationContext());
                    mServiceIntent = new Intent(this, mServiceNew.getClass());
                    startService(mServiceIntent);
                }

                if (!mBounded) {
                    bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
                    //GPS se zapne v onServiceConnected (MainActivity)
                    // po připojení k service díky nastavení gpsIsStarted na TRUE;
                } else {
                    if (mServiceNew != null) mServiceNew.startGps();
                }
            } else {
                AppUtils.appendLog("ERROR - no permissions!");
            }
        } else {
            AppUtils.appendLog("ERROR - no Google play service!");
        }
    }

    private void showNotification(String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_location_black_24dp)
                .setContentTitle("fm")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(74930, builder.build());
    }

    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) return false;
        return true;
    }

    private boolean checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int permissionState1 = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
            int permissionState2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void registerLocationReceiver() {
        Log.i(TAG, "MyFirebaseMessagingService - registerLocationReceiver()");

        locacionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("MyFirebaseMessagingService - LocationReceiver - onReceive()");

                if (intent == null) {
                    AppUtils.appendLog("MyFirebaseMessagingService - LocationReceiver - intent == null");
                    return;
                }

                if (tokenForResponse == null) {
                    AppUtils.appendLog("MyFirebaseMessagingService - LocationReceiver - tokenForResponse == null");
                    return;
                }

                if (tokenForResponse.equals("")) {
                    AppUtils.appendLog("MyFirebaseMessagingService - LocationReceiver - tokenForResponse is empty");
                    return;
                }

                Location location = intent.getParcelableExtra(EXTRA_LOCATION);

                if (location != null) {
                    ResponseToFcmDataLocation responseDataLocation = new ResponseToFcmDataLocation(
                            FCM_RESPONSE_TYPE_LOCATION,
                            "",
                            "" + location.getLatitude(),
                            "" + location.getLongitude(),
                            "" + location.getAccuracy(),
                            "" + location.getTime());

                    ResponseToFcm responseToFcm = new ResponseToFcm(tokenForResponse, responseDataLocation);
                    sendResponse(responseToFcm);
                }
                else AppUtils.appendLog("MyFirebaseMessagingService - LocationReceiver - location == null");
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(locacionReceiver, new IntentFilter(ACTION_LOCATION_BROADCAST));
    }

    private void registerRetartServiceReceiver() {
        Log.i(TAG, "MyFirebaseMessagingService - registerRetartServiceReceiver()");

        restartServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("MyFirebaseMessagingService - RestartServiceReceiver - onReceive()");
                if (gpsIsStarted) startLocationWatcher();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(restartServiceReceiver, new IntentFilter(ACTION_RESTART_SERVICE_BROADCAST));
    }

    private void registerServiceDestroyedReceiver() {
        AppUtils.appendLog("registerServiceDestroyedReceiver()");

        serviceDestroyedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("ServiceDestroyedReceiver - onReceive()");
                mBounded = false;
                mServiceNew = null;
                serviceIsStarted = false;

                ResponseToFcmData responseToFcmData = new ResponseToFcmData(FCM_RESPONSE_SERVICE_STOP, "Služba je vypnuta.");
                ResponseToFcm response = new ResponseToFcm(tokenForResponse, responseToFcmData);
                sendResponse(response);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(serviceDestroyedReceiver, new IntentFilter(ACTION_DESTROY_SERVICE));
    }

    private void registerServiceStartedReceiver() {
        AppUtils.appendLog("registerServiceStartedReceiver()");

        serviceStartetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("ServiceStartetReceiver - onReceive()");
                serviceIsStarted = true;

                ResponseToFcmData responseData = new ResponseToFcmData(FCM_RESPONSE_SERVICE_START, "");
                ResponseToFcm responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                sendResponse(responseToFcm);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(serviceStartetReceiver, new IntentFilter(ACTION_START_SERVICE));
    }

    private void registerServiceOnUnbindReceiver() {
        AppUtils.appendLog("registerServiceOnUnbindReceiver()");

        serviceOnUnbindReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("ServiceStartetReceiver - onReceive()");
                mBounded = false;
                gpsIsStarted = false;
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(serviceOnUnbindReceiver, new IntentFilter(ACTION_ON_UNBIND_SERVICE));
    }

    private void unregisterLocationReceiver() {
        Log.i(TAG, "unregisterLocationReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(locacionReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "unregisterLocationReceiver(): " + e.getMessage());
        }
    }

    private void unregisterRetartServiceReceiver() {
        Log.i(TAG, "unregisterRetartServiceReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(restartServiceReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "unregisterRetartServiceReceiver(): " + e.getMessage());
        }
    }

    private void unregisterServiceDestroyedReceiver() {
        Log.i(TAG, "unregisterServiceDestroyedReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceDestroyedReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "serviceDestroyedReceiver(): " + e.getMessage());
        }
    }

    private void unregisterServiceStartetReceiver() {
        Log.i(TAG, "unregisterServiceStartetReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceStartetReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "serviceStartetReceiver(): " + e.getMessage());
        }
    }

    private void unregisterServiceOnUnbindReceiver() {
        Log.i(TAG, "unregisterServiceOnUnbindReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceOnUnbindReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "serviceOnUnbindReceiver(): " + e.getMessage());
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        Log.i(TAG, "isMyServiceRunning()");
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i(TAG, "MainActivity - isMyServiceRunning() - TRUE");
                return true;
            }
        }
        Log.i(TAG, "MainActivity - isMyServiceRunning() - FALSE");
        return false;
    }
}
