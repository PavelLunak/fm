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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fm.MainActivity;
import com.example.fm.R;
import com.example.fm.objects.DeviceIdentification;
import com.example.fm.retrofit.ApiFcm;
import com.example.fm.retrofit.ControllerFcm;
import com.example.fm.retrofit.objects.ResponseToFcmData;
import com.example.fm.retrofit.objects.ResponseToFcmDataServiceStatus;
import com.example.fm.retrofit.objects.ResponseToFcmDataSettings;
import com.example.fm.retrofit.requests.ResponseToFcm;
import com.example.fm.services.LocationMonitoringService;
import com.example.fm.utils.AppConstants;
import com.example.fm.utils.AppUtils;
import com.example.fm.utils.FcmManager;
import com.example.fm.utils.PrefsUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements AppConstants {

    private int batteryPercentages;
    private int batteryPlugged;

    private boolean mBounded;
    private LocationMonitoringService mServiceNew;

    private BroadcastReceiver restartServiceReceiver;
    private BroadcastReceiver serviceDestroyedReceiver;
    private BroadcastReceiver serviceStartetReceiver;
    private BroadcastReceiver serviceOnUnbindReceiver;

    private int requestType = 0;
    private String tokenForResponse = "";

    boolean serviceIsStarted,
            isRequestLocation,
            isRequestStartGps,
            isRequestStopGps,
            isRequestKillService;

    Intent mServiceIntent;

    MediaPlayer m;

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            AppUtils.appendLog("MyFirebaseMessagingService - ServiceConnection - onServiceDisconnected");
            mBounded = false;
            mServiceNew = null;
            sendServiceStatus();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppUtils.appendLog("MyFirebaseMessagingService - ServiceConnection - onServiceConnected()");

            updateBatteryStatus();

            LocationMonitoringService.LocalBinderNewVersion mLocalBinder = (LocationMonitoringService.LocalBinderNewVersion) service;
            mServiceNew = mLocalBinder.getService();
            mBounded = true;

            if (isRequestStartGps) {
                AppUtils.appendLog("MyFirebaseMessagingService - onServiceConnected() : isRequestStartGps");
                if (mServiceNew != null) mServiceNew.startGps();
                else Log.d(TAG, "mServiceNew == null");
                isRequestStartGps = false;
                sendServiceStatus();
            } else if (isRequestStopGps) {
                AppUtils.appendLog("MyFirebaseMessagingService - onServiceConnected() : isRequestStopGps");
                if (mServiceNew != null) mServiceNew.stopGps();
                else Log.d(TAG, "mServiceNew == null");
                isRequestStopGps = false;
                sendServiceStatus();
            } else if (isRequestLocation) {
                AppUtils.appendLog("MyFirebaseMessagingService - onServiceConnected() : isRequestLocation");
                if (mServiceNew != null) mServiceNew.getActualLocation(REQUIRED_NUMBER_OF_LOCATIONS);
                else Log.d(TAG, "mServiceNew == null");
                isRequestLocation = false;
            } else if (isRequestKillService) {
                AppUtils.appendLog("MyFirebaseMessagingService - onServiceConnected() : isRequestKillService");
                mServiceNew.setRequestStopService();
                killService();
                isRequestKillService = false;
                sendServiceStatus();
            } else {
                sendServiceStatus();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.appendLog("MyFirebaseMessagingService - onCreate()");
        registerRetartServiceReceiver();
        registerServiceDestroyedReceiver();
        registerServiceOnUnbindReceiver();
        registerServiceStartedReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppUtils.appendLog("MyFirebaseMessagingService - onDestroy()");
        unregisterRetartServiceReceiver();
        unregisterServiceDestroyedReceiver();
        unregisterServiceOnUnbindReceiver();
        unregisterServiceStartetReceiver();

        try {
            AppUtils.appendLog("MyFirebaseMessagingService - onDestroy() : try unbindService(mConnection)");
            unbindService(mConnection);
        } catch (IllegalArgumentException e) {
            AppUtils.appendLog("MyFirebaseMessagingService - onDestroy() : try unbindService(mConnection) - IllegalArgumentException");
            AppUtils.appendLog(e.getMessage());
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        AppUtils.appendLog("MyFirebaseMessagingService - onRebind()");
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        AppUtils.appendLog("MyFirebaseMessagingService - onMessageSent()");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppUtils.appendLog("MyFirebaseMessagingService - onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Log.d(TAG, "MyFirebaseMessagingService - onNewToken()");
        AppUtils.appendLog("MyFirebaseMessagingService - onNewToken()");

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(final InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                PrefsUtils.updatePrefsToken(MyFirebaseMessagingService.this, newToken);
                DeviceIdentification di = AppUtils.getDeviceIdentification(MyFirebaseMessagingService.this);
                PrefsUtils.updateAndroidId(MyFirebaseMessagingService.this, di.getAndroidId());
                PrefsUtils.updateDeviceId(MyFirebaseMessagingService.this, di.getDeviceId());
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        AppUtils.appendLog("MyFirebaseMessagingService - onMessageReceived");
        updateBatteryStatus();

        if (remoteMessage != null) {
            Map<String, String> data = remoteMessage.getData();

            if (data != null) {
                if (!data.isEmpty()) {
                    try {
                        if (data.containsKey(KEY_REQUEST_TYPE)) requestType = Integer.parseInt(data.get(KEY_REQUEST_TYPE));
                    } catch (NumberFormatException e) {
                        return;
                    }

                    if (data.containsKey(KEY_TOKEN_FOR_RESPONSE)) tokenForResponse = data.get(KEY_TOKEN_FOR_RESPONSE);
                    MainActivity.tokenForResponse = tokenForResponse;

                    ResponseToFcmData responseData;
                    ResponseToFcm responseToFcm;

                    switch (requestType) {
                        case FCM_REQUEST_TYPE_CANCEL:
                            break;
                        case FCM_REQUEST_TYPE_SERVICE_STATUS:
                            if (isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - onMessageReceived()")) {
                                if (!mBounded) {
                                    try {
                                        bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    sendServiceStatus();
                                }
                            } else {
                                sendServiceStatus();
                            }
                            break;
                        case FCM_REQUEST_TYPE_SERVICE_START:
                            startService();
                            break;
                        case FCM_REQUEST_TYPE_SERVICE_STOP:
                            stopService();
                            break;
                        case FCM_REQUEST_TYPE_GPS_START:
                            if (!AppUtils.isLocationEnabled(this)) {
                                sendServiceStatus();
                                return;
                            }

                            if (isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - onMessageReceived()")) {
                                if (!mBounded) {
                                    try {
                                        isRequestStartGps = true;
                                        bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
                                    } catch (IllegalArgumentException e) {
                                        Log.d(TAG, "Nepodařilo se připojit ke službě: " + e.getMessage());
                                    }
                                }
                            } else {
                                isRequestStartGps = true;
                                startService();
                            }
                            break;
                        case FCM_REQUEST_TYPE_GPS_STOP:
                            if (!AppUtils.isLocationEnabled(this)) {
                                sendServiceStatus();
                                return;
                            }

                            if (isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - onMessageReceived()")) {
                                if (!mBounded) {
                                    try {
                                        isRequestStopGps = true;
                                        bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
                                    } catch (IllegalArgumentException e) {
                                        Log.d(TAG, "Nepodařilo se připojit ke službě: " + e.getMessage());
                                    }
                                }
                            } else {
                                Log.d(TAG, "Služba neběží, neběží tedy ani GPS - není co vypnout.");
                            }
                            break;
                        case FCM_REQUEST_TYPE_LOCATION:
                            if (!AppUtils.isLocationEnabled(this)) {
                                sendServiceStatus();

                                FcmManager.sendMessage(
                                        MyFirebaseMessagingService.this,
                                        "Na zařízení není povolen přístup k poloze.",
                                        batteryPercentages,
                                        batteryPlugged,
                                        tokenForResponse,
                                        ACTION_MESSAGE_CODE_NONE);
                                return;
                            }

                            if (isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - onMessageReceived()")) {
                                if (!mBounded) {
                                    try {
                                        isRequestLocation = true;
                                        bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
                                    } catch (IllegalArgumentException e) {
                                        Log.d(TAG, "Nepodařilo se připojit ke službě: " + e.getMessage());
                                    }
                                }
                            } else {
                                isRequestLocation = true;
                                startService();
                            }
                            break;
                        case FCM_REQUEST_TYPE_SETTINGS_DATABASE:
                            SharedPreferences sharedpreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();

                            try {
                                if (data.containsKey("savingToDatabaseEnabled"))
                                    editor.putBoolean("savingToDatabaseEnabled", data.get("savingToDatabaseEnabled").toLowerCase().trim().equals("1") ? true : false);
                                if (data.containsKey("autoCheckedPositionSavingInterval"))
                                    editor.putLong("autoCheckedPositionSavingInterval", Long.parseLong(data.get("autoCheckedPositionSavingInterval")));
                                if (data.containsKey("maxCountOfLocationChecked"))
                                    editor.putInt("maxCountOfLocationChecked", Integer.parseInt(data.get("maxCountOfLocationChecked")));
                                if (data.containsKey("timeUnit"))
                                    editor.putInt("timeUnit", Integer.parseInt(data.get("timeUnit")));
                                if (data.containsKey("locationsInterval"))
                                    editor.putLong("locationsInterval", Long.parseLong(data.get("locationsInterval")));
                                if (data.containsKey("locationsIntervalTimeUnit"))
                                    editor.putInt("locationsIntervalTimeUnit", Integer.parseInt(data.get("locationsIntervalTimeUnit")));
                                editor.commit();

                                responseData = new ResponseToFcmData(
                                        FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVED,
                                        PrefsUtils.getPrefsToken(MyFirebaseMessagingService.this),
                                        PrefsUtils.getPrefsDatabaseId(MyFirebaseMessagingService.this),
                                        PrefsUtils.getAndroidId(MyFirebaseMessagingService.this),
                                        "Nové nastavení uloženo.",
                                        "" + batteryPercentages,
                                        batteryPlugged,
                                        ACTION_MESSAGE_CODE_NONE);
                                responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                responseData = new ResponseToFcmData(
                                        FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVE_ERROR,
                                        PrefsUtils.getPrefsToken(MyFirebaseMessagingService.this),
                                        PrefsUtils.getPrefsDatabaseId(MyFirebaseMessagingService.this),
                                        PrefsUtils.getAndroidId(MyFirebaseMessagingService.this),
                                        "Chyba při ukládání nastavení - NumberFormatException",
                                        "" + batteryPercentages,
                                        batteryPlugged,
                                        ACTION_MESSAGE_CODE_NONE);
                                responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                responseData = new ResponseToFcmData(
                                        FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVE_ERROR,
                                        PrefsUtils.getPrefsToken(MyFirebaseMessagingService.this),
                                        PrefsUtils.getPrefsDatabaseId(MyFirebaseMessagingService.this),
                                        PrefsUtils.getAndroidId(MyFirebaseMessagingService.this),
                                        "Chyba při ukládání nastavení - NullPointerException",
                                        "" + batteryPercentages,
                                        batteryPlugged,
                                        ACTION_MESSAGE_CODE_NONE);
                                responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                            }

                            sendResponse(responseToFcm);

                            break;
                        case FCM_REQUEST_TYPE_LOAD_SETTINGS:
                            SharedPreferences sp = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            responseData = new ResponseToFcmDataSettings(
                                    FCM_RESPONSE_TYPE_SETTINGS_LOADED,
                                    PrefsUtils.getPrefsToken(MyFirebaseMessagingService.this),
                                    PrefsUtils.getPrefsDatabaseId(MyFirebaseMessagingService.this),
                                    PrefsUtils.getAndroidId(MyFirebaseMessagingService.this),
                                    "Nastavení načteno.",
                                    "" + batteryPercentages,
                                    batteryPlugged,
                                    sp.getBoolean(KEY_DB_ENABLED, false) ? 1 : 0,
                                    sp.getLong(KEY_SAVE_INTERVAL, -1),
                                    sp.getInt(KEY_MAX_COUNT_LOC_SAVE, -1),
                                    sp.getInt(KEY_TIME_UNIT, TIME_UNIT_SECONDS),
                                    sp.getLong(KEY_LOCATIONS_INTERVAL, -1),
                                    sp.getInt(KEY_LOCATIONS_INTERVAL_TIME_UNIT, TIME_UNIT_SECONDS));

                            FcmManager.sendResponse(new ResponseToFcm(tokenForResponse, responseData));
                            break;
                        case FCM_REQUEST_TYPE_ALARM:
                            playAlarm();
                            break;
                        case FCM_REQUEST_TYPE_CALL:
                            sendCall("+420606289254");
                    }
                } else {
                    Log.d(TAG, "remoteMessage.getData().isEmpty()");
                }
            } else {
                Log.d(TAG, "remoteMessage.getData() == null");
            }
        } else {
            Log.d(TAG, "remoteMessage == null");
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
                Log.d(TAG, "response code: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "isSuccessful: TRUE");

                    try {
                        Log.d(TAG, "response: " + response.body().string());
                    } catch (IOException e) {
                        Log.d(TAG, "response: IOException: ");
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "isSuccessful: FALSE");

                    try {
                        Log.d(TAG, "response: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.d(TAG, "response: IOException: ");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
                Log.d(TAG, t.getMessage());
            }
        });
    }

    public void startService() {
        Log.d(TAG, "MyFirebaseMessagingService - startService()");

        if (!isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - startService()")) {
            mServiceNew = new LocationMonitoringService(getApplicationContext());
            mServiceIntent = new Intent(this, mServiceNew.getClass());
            startService(mServiceIntent);
        }

        if (!mBounded) {
            bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
        }
    }

    public void stopService() {
        Log.d(TAG, "stopService()");

        if (mServiceNew != null) {
            Log.d(TAG, "stopService() - mServiceNew != null");
            mServiceNew.stopGps();
            mServiceNew.setRequestStopService();
        } else {
            Log.d(TAG, "stopService() - mServiceNew == null -> nové připojení k service...");
            isRequestKillService = true;
            bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
        }
    }

    public void killService() {
        Log.d(TAG, "killService()");

        if (mConnection != null) {
            if (mBounded) {
                try {
                    Log.d(TAG, "killService() - unbindService(mConnection)...");
                    unbindService(mConnection);
                    //PrefsUtils.updatePrefsGpsStatus(this, false);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "killService() - unbindService(mConnection)... SELHALO");
                    Log.d(TAG, e.getMessage());
                }
            }
        } else {
            Log.d(TAG, "killService() - mConnection == null");
        }

        if (mServiceNew != null) {
            mServiceNew.stopSelf();
        } else {
            Log.d(TAG, "killService() - mServiceNew == NULL -> nemůžu dát požadavek na stop");
        }

        Log.d(TAG, "killService() - is service running : " + isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - killService()"));
    }

    public void startLocationWatcher() {
        Log.d(TAG, "FirebaseMessagingService - startLocationWatcher()");

        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {
            if (checkPermissions()) {

                /*
                if (PrefsUtils.getPrefsGpsStatus(this)) {
                    Log.d(TAG, "FirebaseMessagingService - startLocationWatcher() : GPS již běží, nebudu znova zapínat...");
                    return;
                }
                */

                if (!isMyServiceRunning(LocationMonitoringService.class, "FirebaseMessagingService - startLocationWatcher()")) {
                    Log.d(TAG, "FirebaseMessagingService - startLocationWatcher() - isMyServiceRunning == FALSE -> go start");
                    mServiceNew = new LocationMonitoringService(getApplicationContext());
                    mServiceIntent = new Intent(this, mServiceNew.getClass());
                    startService(mServiceIntent);
                }

                if (!mBounded) {
                    Log.d(TAG, "FirebaseMessagingService - startLocationWatcher() - mBounded == FALSE -> go bound");
                    if (mServiceNew == null) Log.d(TAG, "mServiceNew == null");
                    if (mConnection == null) Log.d(TAG, "mConnection == null");

                    mServiceIntent = new Intent(this, LocationMonitoringService.class);
                    bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
                    //GPS se zapne v onServiceConnected (MainActivity)
                    // po připojení k service díky nastavení gpsIsStarted na TRUE;
                }

                if (mServiceNew != null) mServiceNew.startGps();
            } else {
                Log.d(TAG, "FirebaseMessagingService - startLocationWatcher() - ERROR - no permissions!");
            }
        } else {
            Log.d(TAG, "FirebaseMessagingService - startLocationWatcher() - ERROR - no Google play service!");
        }
    }

    public void stopLocationWatcher() {
        Log.d(TAG, "FirebaseMessagingService - stopLocationWatcher()");
        //unregisterLocationReceiver();

        if (mServiceNew != null) {
            mServiceNew.stopGps();
            //gpsIsStarted = false;
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

    private void registerRetartServiceReceiver() {
        Log.d(TAG, "MyFirebaseMessagingService - registerRetartServiceReceiver()");

        restartServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("MyFirebaseMessagingService - RestartServiceReceiver - onReceive()");
                //if (PrefsUtils.getPrefsGpsStatus(MyFirebaseMessagingService.this)) startLocationWatcher();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(restartServiceReceiver, new IntentFilter(ACTION_RESTART_SERVICE_BROADCAST));
    }

    private void registerServiceDestroyedReceiver() {
        AppUtils.appendLog("MyFirebaseMessagingService - registerServiceDestroyedReceiver()");

        serviceDestroyedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("MyFirebaseMessagingService - ServiceDestroyedReceiver - onReceive()");
                mBounded = false;
                mServiceNew = null;
                serviceIsStarted = false;
                sendServiceStatus();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(serviceDestroyedReceiver, new IntentFilter(ACTION_DESTROY_SERVICE));
    }

    private void registerServiceStartedReceiver() {
        AppUtils.appendLog("MyFirebaseMessagingService - registerServiceStartedReceiver()");

        serviceStartetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("MyFirebaseMessagingService - ServiceStartetReceiver - onReceive()");
                serviceIsStarted = true;
                sendServiceStatus();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(serviceStartetReceiver, new IntentFilter(ACTION_START_SERVICE));
    }

    private void registerServiceOnUnbindReceiver() {
        AppUtils.appendLog("MyFirebaseMessagingService - registerServiceOnUnbindReceiver()");

        serviceOnUnbindReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("MyFirebaseMessagingService - ServiceStartetReceiver - onReceive()");
                mBounded = false;
                //gpsIsStarted = false;
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(serviceOnUnbindReceiver, new IntentFilter(ACTION_ON_UNBIND_SERVICE));
    }

    private void unregisterRetartServiceReceiver() {
        Log.d(TAG, "MyFirebaseMessagingService - unregisterRetartServiceReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(restartServiceReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "MyFirebaseMessagingService - unregisterRetartServiceReceiver(): " + e.getMessage());
        }
    }

    private void unregisterServiceDestroyedReceiver() {
        Log.d(TAG, "MyFirebaseMessagingService - unregisterServiceDestroyedReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceDestroyedReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "MyFirebaseMessagingService - serviceDestroyedReceiver(): " + e.getMessage());
        }
    }

    private void unregisterServiceStartetReceiver() {
        Log.d(TAG, "MyFirebaseMessagingService - unregisterServiceStartetReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceStartetReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "MyFirebaseMessagingService - serviceStartetReceiver(): " + e.getMessage());
        }
    }

    private void unregisterServiceOnUnbindReceiver() {
        Log.d(TAG, "MyFirebaseMessagingService - unregisterServiceOnUnbindReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceOnUnbindReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "MyFirebaseMessagingService - serviceOnUnbindReceiver(): " + e.getMessage());
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass, String forDebugFrom) {
        Log.d(TAG, "isMyServiceRunning(), FROM: " + forDebugFrom);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "MyFirebaseMessagingService - isMyServiceRunning() - TRUE");
                return true;
            }
        }
        Log.d(TAG, "MyFirebaseMessagingService - isMyServiceRunning() - FALSE");
        return false;
    }

    private String prepareResponseMessage() {
        boolean isLocationEnabled = AppUtils.isLocationEnabled(this);
        //boolean gpsStarted = PrefsUtils.getPrefsGpsStatus(this);
        boolean isServiceStarted = isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - prepareResponseMessage()");

        boolean gpsStarted = false;

        if (isServiceStarted)
            if (mServiceNew != null) gpsStarted = mServiceNew.getGpsStatus();

        StringBuilder sb = new StringBuilder("Stav služby:");

        if (isServiceStarted) sb.append("\nSlužba na pozadí - ZAPNUTO");
        else sb.append("\nSlužba na pozadí - VYPNUTO");

        if (isLocationEnabled) sb.append("\nGPS - přístup POVOLEN");
        else sb.append("\nGPS - přístup ZAKÁZÁN");

        if (gpsStarted) sb.append("\nGPS - ZAPNUTO");
        else sb.append("\nGPS - VYPNUTO");

        return sb.toString();
    }

    private boolean checkServiceBounded() {
        if (!isMyServiceRunning(LocationMonitoringService.class, "checkServiceBounded()")) return false;
        if (!mBounded) {
            try {
                bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        return true;
    }

    private void playAlarm() {
        Log.d(TAG, "MyFirebaseMessagingService - playAlarm()");
        if (m != null) return;
        m = MediaPlayer.create(this, R.raw.sirena);

        m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                m.release();
                m = null;

                FcmManager.sendMessage(
                        MyFirebaseMessagingService.this,
                        "Alarm v pořádku přehrán",
                        batteryPercentages,
                        batteryPlugged,
                        tokenForResponse,
                        ACTION_MESSAGE_CODE_ALARM_STOP);
            }
        });

        m.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                m = null;

                FcmManager.sendMessage(
                        MyFirebaseMessagingService.this,
                        "Chyba při přehrávání alarmu",
                        batteryPercentages,
                        batteryPlugged,
                        tokenForResponse,
                        ACTION_MESSAGE_CODE_ALARM_STOP);

                return false;
            }
        });

        m.start();

        //AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        FcmManager.sendMessage(
                MyFirebaseMessagingService.this,
                "Alarm je spuštěn...",
                batteryPercentages,
                batteryPlugged,
                tokenForResponse,
                ACTION_MESSAGE_CODE_ALARM_START);
    }

    private void sendCall(String pnoneNumber) {
        Log.d(TAG, "MyFirebaseMessagingService - sendCall()");

        int simState = getSimState();
        if (simState != TelephonyManager.SIM_STATE_READY) {

            FcmManager.sendMessage(
                    MyFirebaseMessagingService.this,
                    "SIMkarta není připravena. SIMstatus = " + AppUtils.simStateToString(simState),
                    batteryPercentages,
                    batteryPlugged,
                    tokenForResponse,
                    ACTION_MESSAGE_CODE_CALL_ERROR);
            return;
        }

        String dial = "tel:" + pnoneNumber;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                FcmManager.sendMessage(
                        MyFirebaseMessagingService.this,
                        "Aplikace na vzdáleném zařízení nemá oprávnění k telefonním hovorům",
                        batteryPercentages,
                        batteryPlugged,
                        tokenForResponse,
                        ACTION_MESSAGE_CODE_CALL_ERROR);
                return;
            }
        }

        FcmManager.sendMessage(
                MyFirebaseMessagingService.this,
                "Požadavek na hovor úspěšně doručen. Vyčkej na příchozí hovor!",
                batteryPercentages,
                batteryPlugged,
                tokenForResponse,
                ACTION_MESSAGE_CODE_CALL_REQUEST);

        Intent intentCall = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
        intentCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentCall);
    }

    private int getSimState() {
        Log.d(TAG, "MyFirebaseMessagingService - getSimState()");

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                FcmManager.sendMessage(
                        MyFirebaseMessagingService.this,
                        "Aplikace na vzdáleném zařízení nemá oprávnění k telefonním hovorům",
                        batteryPercentages,
                        batteryPlugged,
                        tokenForResponse,
                        ACTION_MESSAGE_CODE_CALL_ERROR);

                return NO_PHONE_PERMISSIONS;
            }
        }

        Log.d(TAG, "MyFirebaseMessagingService - getSimState() : SIMstate = " + tm.getSimState());
        return tm.getSimState();
    }

    private void updateBatteryStatus() {
        batteryPercentages = AppUtils.getBatteryPercentage(MyFirebaseMessagingService.this);
        batteryPlugged = AppUtils.getBatteryIsPlugged(MyFirebaseMessagingService.this);
    }

    private void sendServiceStatus() {
        boolean isServiceStarted = isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - sendServiceStatus()");
        boolean gpsStarted = false;

        if (isServiceStarted) {
            if (mServiceNew != null) gpsStarted = mServiceNew.getGpsStatus();
        }

        ResponseToFcmData responseData = new ResponseToFcmDataServiceStatus(
                FCM_RESPONSE_SERVICE_STATUS,
                PrefsUtils.getPrefsToken(MyFirebaseMessagingService.this),
                PrefsUtils.getPrefsDatabaseId(MyFirebaseMessagingService.this),
                PrefsUtils.getAndroidId(MyFirebaseMessagingService.this),
                prepareResponseMessage(),
                "" + batteryPercentages,
                batteryPlugged,
                ACTION_MESSAGE_CODE_NONE,
                isServiceStarted ? STARTED : STOPED,
                gpsStarted ? STARTED : STOPED);

        ResponseToFcm responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
        sendResponse(responseToFcm);
    }
}
