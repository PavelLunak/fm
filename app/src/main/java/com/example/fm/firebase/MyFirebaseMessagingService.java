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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.fm.MainActivity;
import com.example.fm.R;
import com.example.fm.listeners.OnServiceBoundedListener;
import com.example.fm.objects.NewLocation;
import com.example.fm.objects.ResultForRequestActualLocation;
import com.example.fm.retrofit.ApiFcm;
import com.example.fm.retrofit.ControllerFcm;
import com.example.fm.retrofit.objects.ResponseToFcmData;
import com.example.fm.retrofit.objects.ResponseToFcmDataLocation;
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

    private boolean mBounded;
    private OnServiceBoundedListener onServiceBoundedListener;
    private LocationMonitoringService mServiceNew;

    private BroadcastReceiver locacionReceiver;
    private BroadcastReceiver restartServiceReceiver;
    private BroadcastReceiver serviceDestroyedReceiver;
    private BroadcastReceiver serviceStartetReceiver;
    private BroadcastReceiver serviceOnUnbindReceiver;
    private BroadcastReceiver messageReceiver;

    private int requestType = 0;
    private String tokenForResponse = "";

    boolean serviceIsStarted,
            //gpsIsStarted,
            isRequestLocation,
            isRequestStartGps,
            isRequestStopGps,
            isRequestLocationResult,
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

            updateBatteryPercentages();

            LocationMonitoringService.LocalBinderNewVersion mLocalBinder = (LocationMonitoringService.LocalBinderNewVersion) service;
            mServiceNew = mLocalBinder.getService();
            mBounded = true;

            if (isRequestStartGps) {
                AppUtils.appendLog("MyFirebaseMessagingService - onServiceConnected() : isRequestStartGps");

                if (mServiceNew != null) mServiceNew.startGps();
                else Log.i(TAG, "mServiceNew == null");

                isRequestStartGps = false;
                sendServiceStatus();
            } else if (isRequestStopGps) {
                AppUtils.appendLog("MyFirebaseMessagingService - onServiceConnected() : isRequestStopGps");

                if (mServiceNew != null) mServiceNew.stopGps();
                else Log.i(TAG, "mServiceNew == null");

                isRequestStopGps = false;
                sendServiceStatus();
            } else if (isRequestLocation) {
                AppUtils.appendLog("MyFirebaseMessagingService - onServiceConnected() : isRequestLocation");
                if (mServiceNew != null) mServiceNew.getActualLocation(REQUIRED_NUMBER_OF_LOCATIONS);
                else Log.i(TAG, "mServiceNew == null");
                isRequestLocation = false;
            } else if (isRequestLocationResult) {
                /*
                AppUtils.appendLog("MyFirebaseMessagingService - onServiceConnected() : isRequestLocationResult");

                if (mServiceNew != null) {
                    Log.i(TAG, mServiceNew.getActualLocationResult().getActualLocation().toString());
                    ResultForRequestActualLocation result = mServiceNew.getActualLocationResult();

                    if (result == null) {
                        sendMessage("ResultForRequestActualLocation == NULL");
                        return;
                    }

                    if (result.getMessage() != null) {
                        sendMessage(result.getMessage());
                    } else {
                        if (result.getMessage() == null) {
                            ResponseToFcmDataLocation responseDataLocation = new ResponseToFcmDataLocation(
                                    FCM_RESPONSE_TYPE_LOCATION,
                                    "",
                                    "" + batteryPercentages,
                                    "" + result.getActualLocation().getLatitude(),
                                    "" + result.getActualLocation().getLongitude(),
                                    "" + (result.getActualLocation().getSpeed() * 3.6),
                                    "" + result.getActualLocation().getAccuracy(),
                                    "" + result.getActualLocation().getDate());

                            ResponseToFcm responseToFcm = new ResponseToFcm(tokenForResponse, responseDataLocation);
                            sendResponse(responseToFcm);
                        } else {
                            sendMessage("Získaná poloha == NULL");
                        }
                    }
                } else {
                    Log.i(TAG, "mServiceNew == null");
                }

                isRequestLocationResult = false;
                */
            } else if (isRequestKillService) {
                AppUtils.appendLog("MyFirebaseMessagingService - onServiceConnected() : isRequestKillService");
                mServiceNew.setRequestStopService();
                killService();
                isRequestKillService = false;
            } else {
                //startLocationWatcher();
                sendServiceStatus();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.appendLog("MyFirebaseMessagingService - onCreate()");
        registerLocationReceiver();
        registerRetartServiceReceiver();
        registerServiceDestroyedReceiver();
        registerServiceOnUnbindReceiver();
        registerServiceStartedReceiver();
        registerMessageReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppUtils.appendLog("MyFirebaseMessagingService - onDestroy()");
        unregisterLocationReceiver();
        unregisterRetartServiceReceiver();
        unregisterServiceDestroyedReceiver();
        unregisterServiceOnUnbindReceiver();
        unregisterServiceStartetReceiver();
        unregisterMessageReceiver();

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

        AppUtils.appendLog("MyFirebaseMessagingService - onNewToken()");

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                PrefsUtils.updatePrefsToken(MyFirebaseMessagingService.this, newToken);
                Log.i(TAG, "new token: " + newToken);
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.i(TAG, "MyFirebaseMessagingService - onMessageReceived");

        updateBatteryPercentages();

        if (remoteMessage != null) {
            Map<String, String> data = remoteMessage.getData();

            if (data != null) {
                if (!data.isEmpty()) {
                    for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                        Log.i(TAG, "key : " + entry.getKey() + ", value : " + entry.getValue());
                    }

                    try {
                        if (data.containsKey(KEY_REQUEST_TYPE))
                            requestType = Integer.parseInt(data.get(KEY_REQUEST_TYPE));
                    } catch (NumberFormatException e) {
                        Log.i(TAG, "NumberFormatException: " + e.getMessage());
                        return;
                    }

                    if (data.containsKey(KEY_TOKEN_FOR_RESPONSE)) tokenForResponse = data.get(KEY_TOKEN_FOR_RESPONSE);
                    MainActivity.tokenTest = tokenForResponse;

                    Log.i(TAG, "requestType: " + AppUtils.requestTypeToString(requestType));
                    Log.i(TAG, "tokenForResponse: " + tokenForResponse);

                    ResponseToFcmData responseData;
                    ResponseToFcm responseToFcm;

                    switch (requestType) {
                        case FCM_REQUEST_TYPE_CANCEL:
                            break;
                        case FCM_REQUEST_TYPE_SERVICE_STATUS:
                            sendServiceStatus();
                            break;
                        case FCM_REQUEST_TYPE_SERVICE_START:
                            startService();
                            //startLocationWatcher();
                            break;
                        case FCM_REQUEST_TYPE_SERVICE_STOP:
                            stopService();
                            //stopLocationWatcher();
                            break;
                        case FCM_REQUEST_TYPE_GPS_START:
                            if (!AppUtils.isLocationEnabled(this)) {
                                sendServiceStatus();
                                sendMessage("Na zařízení není povolen přístup k poloze.");
                                return;
                            }

                            if (isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - onMessageReceived()")) {
                                if (!mBounded) {
                                    try {
                                        isRequestStartGps = true;
                                        bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
                                    } catch (IllegalArgumentException e) {
                                        Log.i(TAG, "Nepodařilo se připojit ke službě: " + e.getMessage());
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
                                sendMessage("Na zařízení není povolen přístup k poloze.");
                                return;
                            }

                            if (isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - onMessageReceived()")) {
                                if (!mBounded) {
                                    try {
                                        isRequestStopGps = true;
                                        bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
                                    } catch (IllegalArgumentException e) {
                                        Log.i(TAG, "Nepodařilo se připojit ke službě: " + e.getMessage());
                                    }
                                }
                            } else {
                                Log.i(TAG, "Služba neběží, neběží tedy ani GPS - není co vypnout.");
                            }
                            break;
                        case FCM_REQUEST_TYPE_LOCATION:
                            if (!AppUtils.isLocationEnabled(this)) {
                                sendServiceStatus();
                                sendMessage("Na zařízení není povolen přístup k poloze.");
                                return;
                            }

                            if (isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - onMessageReceived()")) {
                                if (!mBounded) {
                                    try {
                                        isRequestLocation = true;
                                        bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
                                    } catch (IllegalArgumentException e) {
                                        Log.i(TAG, "Nepodařilo se připojit ke službě: " + e.getMessage());
                                    }
                                }
                            } else {
                                isRequestLocation = true;
                                startService();
                            }
                            break;
                        case FCM_REQUEST_TYPE_LOCATION_RESULT:
                            /*
                            if (isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - onMessageReceived()")) {
                                if (!mBounded) {
                                    try {
                                        isRequestLocationResult = true;
                                        bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
                                    } catch (IllegalArgumentException e) {
                                        Log.i(TAG, "Nepodařilo se připojit ke službě: " + e.getMessage());
                                    }
                                }
                            } else {
                                isRequestLocationResult = true;
                                startService();
                            }
                            */
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

                                responseData = new ResponseToFcmData(FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVED, "Nové nastavení uloženo.", "" + batteryPercentages);
                                responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                responseData = new ResponseToFcmData(FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVE_ERROR, "Chyba při ukládání nastavení - NumberFormatException", "" + batteryPercentages);
                                responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                responseData = new ResponseToFcmData(FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVE_ERROR, "Chyba při ukládání nastavení - NullPointerException", "" + batteryPercentages);
                                responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
                            }

                            sendResponse(responseToFcm);

                            break;
                        case FCM_REQUEST_TYPE_LOAD_SETTINGS:
                            SharedPreferences sp = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            responseData = new ResponseToFcmDataSettings(
                                    FCM_RESPONSE_TYPE_SETTINGS_LOADED,
                                    "Nastavení načteno.",
                                    "" + batteryPercentages,
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

    public void startService() {
        Log.i(TAG, "MyFirebaseMessagingService - startService()");

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
        Log.i(TAG, "stopService()");

        if (mServiceNew != null) {
            Log.i(TAG, "stopService() - mServiceNew != null");
            mServiceNew.stopGps();
            mServiceNew.setRequestStopService();
        } else {
            Log.i(TAG, "stopService() - mServiceNew == null -> nové připojení k service...");
            isRequestKillService = true;
            bindService(new Intent(this, LocationMonitoringService.class), mConnection, BIND_AUTO_CREATE);
        }
    }

    public void killService() {
        Log.i(TAG, "killService()");

        if (mConnection != null) {
            if (mBounded) {
                try {
                    Log.i(TAG, "killService() - unbindService(mConnection)...");
                    unbindService(mConnection);
                    PrefsUtils.updatePrefsGpsStatus(this, false);
                } catch (IllegalArgumentException e) {
                    Log.i(TAG, "killService() - unbindService(mConnection)... SELHALO");
                    Log.i(TAG, e.getMessage());
                }
            }
        } else {
            Log.i(TAG, "killService() - mConnection == null");
        }

        if (mServiceNew != null) {
            mServiceNew.stopSelf();
        } else {
            Log.i(TAG, "killService() - mServiceNew == NULL -> nemůžu dát požadavek na stop");
        }

        Log.i(TAG, "killService() - is service running : " + isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - killService()"));
    }

    public void startLocationWatcher() {
        Log.i(TAG, "FirebaseMessagingService - startLocationWatcher()");

        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {
            if (checkPermissions()) {

                if (PrefsUtils.getPrefsGpsStatus(this)) {
                    Log.i(TAG, "FirebaseMessagingService - startLocationWatcher() : GPS již běží, nebudu znova zapínat...");
                    return;
                }

                if (!isMyServiceRunning(LocationMonitoringService.class, "FirebaseMessagingService - startLocationWatcher()")) {
                    Log.i(TAG, "FirebaseMessagingService - startLocationWatcher() - isMyServiceRunning == FALSE -> go start");
                    mServiceNew = new LocationMonitoringService(getApplicationContext());
                    mServiceIntent = new Intent(this, mServiceNew.getClass());
                    startService(mServiceIntent);
                }

                if (!mBounded) {
                    Log.i(TAG, "FirebaseMessagingService - startLocationWatcher() - mBounded == FALSE -> go bound");
                    if (mServiceNew == null) Log.i(TAG, "mServiceNew == null");
                    if (mConnection == null) Log.i(TAG, "mConnection == null");

                    mServiceIntent = new Intent(this, LocationMonitoringService.class);
                    bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
                    //GPS se zapne v onServiceConnected (MainActivity)
                    // po připojení k service díky nastavení gpsIsStarted na TRUE;
                }

                if (mServiceNew != null) mServiceNew.startGps();
            } else {
                Log.i(TAG, "FirebaseMessagingService - startLocationWatcher() - ERROR - no permissions!");
            }
        } else {
            Log.i(TAG, "FirebaseMessagingService - startLocationWatcher() - ERROR - no Google play service!");
        }
    }

    public void stopLocationWatcher() {
        Log.i(TAG, "FirebaseMessagingService - stopLocationWatcher()");
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

                //Location location = intent.getParcelableExtra(EXTRA_LOCATION);
                NewLocation location = intent.getParcelableExtra(EXTRA_LOCATION);

                if (location != null) {
                    ResponseToFcmDataLocation responseDataLocation = new ResponseToFcmDataLocation(
                            FCM_RESPONSE_TYPE_LOCATION,
                            "",
                            "" + batteryPercentages,
                            "" + location.getLatitude(),
                            "" + location.getLongitude(),
                            "" + location.getAccuracy(),
                            "" + (location.getSpeed() * 3.6),
                            "" + location.getDate());

                    ResponseToFcm responseToFcm = new ResponseToFcm(tokenForResponse, responseDataLocation);
                    sendResponse(responseToFcm);
                } else
                    AppUtils.appendLog("MyFirebaseMessagingService - LocationReceiver - location == null");
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
                if (PrefsUtils.getPrefsGpsStatus(MyFirebaseMessagingService.this)) startLocationWatcher();
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

    private void registerMessageReceiver() {
        AppUtils.appendLog("MyFirebaseMessagingService - registerMessageReceiver()");

        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppUtils.appendLog("MyFirebaseMessagingService - messageReceiver - onReceive()");
                sendMessage(intent.getStringExtra(KEY_MESSAGE));
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter(ACTION_MESSAGE));
    }

    private void unregisterLocationReceiver() {
        Log.i(TAG, "MyFirebaseMessagingService - unregisterLocationReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(locacionReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "MyFirebaseMessagingService - unregisterLocationReceiver(): " + e.getMessage());
        }
    }

    private void unregisterRetartServiceReceiver() {
        Log.i(TAG, "MyFirebaseMessagingService - unregisterRetartServiceReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(restartServiceReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "MyFirebaseMessagingService - unregisterRetartServiceReceiver(): " + e.getMessage());
        }
    }

    private void unregisterServiceDestroyedReceiver() {
        Log.i(TAG, "MyFirebaseMessagingService - unregisterServiceDestroyedReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceDestroyedReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "MyFirebaseMessagingService - serviceDestroyedReceiver(): " + e.getMessage());
        }
    }

    private void unregisterServiceStartetReceiver() {
        Log.i(TAG, "MyFirebaseMessagingService - unregisterServiceStartetReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceStartetReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "MyFirebaseMessagingService - serviceStartetReceiver(): " + e.getMessage());
        }
    }

    private void unregisterServiceOnUnbindReceiver() {
        Log.i(TAG, "MyFirebaseMessagingService - unregisterServiceOnUnbindReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceOnUnbindReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "MyFirebaseMessagingService - serviceOnUnbindReceiver(): " + e.getMessage());
        }
    }

    private void unregisterMessageReceiver() {
        Log.i(TAG, "MyFirebaseMessagingService - unregisterMessageReceiver()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "MyFirebaseMessagingService - messageReceiver(): " + e.getMessage());
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass, String forDebugFrom) {
        Log.i(TAG, "isMyServiceRunning(), FROM: " + forDebugFrom);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i(TAG, "MyFirebaseMessagingService - isMyServiceRunning() - TRUE");
                return true;
            }
        }
        Log.i(TAG, "MyFirebaseMessagingService - isMyServiceRunning() - FALSE");
        return false;
    }

    private String prepareResponseMessage() {
        boolean isLocationEnabled = AppUtils.isLocationEnabled(this);
        boolean startet = isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - prepareResponseMessage()");
        String message = "";

        if (isLocationEnabled && startet) message = "Služba na pozadí je zapnutá.";
        else if (isLocationEnabled && !startet) message = "Služba na pozadí je vypnutá";
        else if (!isLocationEnabled && startet)
            message = "Není povolen přístup k poloze zařízení. Služba na pozadí je ZAPNUTÁ";
        else if (!isLocationEnabled && !startet)
            message = "Není povolen přístup k poloze zařízení. Služba na pozadí je VYPNUTÁ";

        return message;
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
        Log.i(TAG, "MyFirebaseMessagingService - playAlarm()");
        if (m != null) return;
        m = MediaPlayer.create(this, R.raw.sirena);

        m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                m.release();
                m = null;
                sendMessage("Alarm v pořádku přehrán");
            }
        });

        m.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                m = null;
                sendMessage("Chyba při přehrávání alarmu");
                return false;
            }
        });

        m.start();

        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        sendMessage("Alarm se spuštěn...");
    }

    private void sendCall(String pnoneNumber) {
        Log.i(TAG, "MyFirebaseMessagingService - sendCall()");

        int simState = getSimState();
        if (simState != TelephonyManager.SIM_STATE_READY) {
            sendMessage("SIMkarta není připravena. SIMstatus = " + AppUtils.simStateToString(simState));
            return;
        }

        String dial = "tel:" + pnoneNumber;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                sendMessage("sendCall() - Není oprávnění");
                return;
            }
        }

        sendMessage("Požadavek na zavolání úspěšně doručen...");

        Intent intentCall = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
        intentCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentCall);
    }

    private int getSimState() {
        Log.i(TAG, "MyFirebaseMessagingService - getSimState()");

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                sendMessage("getSimState() - Není oprávnění");
                return NO_PHONE_PERMISSIONS;
            }
        }

        Log.i(TAG, "MyFirebaseMessagingService - getSimState() : SIMstate = " + tm.getSimState());
        return tm.getSimState();
    }

    private void sendMessage(String message) {
        ResponseToFcmData responseToFcmData = new ResponseToFcmData(
                FCM_RESPONSE_TYPE_MESSAGE,
                message,
                "" + batteryPercentages);

        ResponseToFcm response = new ResponseToFcm(tokenForResponse, responseToFcmData);
        sendResponse(response);
    }

    private void updateBatteryPercentages() {
        batteryPercentages = AppUtils.getBatteryPercentage(MyFirebaseMessagingService.this);
    }

    private void sendServiceStatus() {
        boolean serviceStartet = isMyServiceRunning(LocationMonitoringService.class, "MyFirebaseMessagingService - sendServiceStatus()");
        if (!serviceStartet) PrefsUtils.updatePrefsGpsStatus(this, false);

        boolean gpsStarted = PrefsUtils.getPrefsGpsStatus(this);

        ResponseToFcmData responseData = new ResponseToFcmDataServiceStatus(
                FCM_RESPONSE_SERVICE_STATUS,
                prepareResponseMessage(),
                "" + batteryPercentages,
                serviceStartet ? STARTED : STOPED,
                gpsStarted ? STARTED : STOPED);

        ResponseToFcm responseToFcm = new ResponseToFcm(tokenForResponse, responseData);
        sendResponse(responseToFcm);
    }
}
