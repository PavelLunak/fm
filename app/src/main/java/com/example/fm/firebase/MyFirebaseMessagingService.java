package com.example.fm.firebase;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.fm.MainActivity;
import com.example.fm.R;
import com.example.fm.retrofit.ApiFcm;
import com.example.fm.retrofit.ControllerFcm;
import com.example.fm.retrofit.objects.LocationData;
import com.example.fm.retrofit.requests.RequestSendPosition;
import com.example.fm.utils.AppConstants;
import com.example.fm.utils.AppUtils;
import com.example.fm.utils.DateTimeUtils;
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
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements AppConstants {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest = new LocationRequest();
    private LocationCallback mLocationCallback;

    private int requestType = 0;
    private String tokenForResponse = "";

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
            Log.i(TAG, "MessageType: " + remoteMessage.getMessageType());
            Log.i(TAG, "CollapseKey: " + remoteMessage.getCollapseKey());
            Log.i(TAG, "From: " + remoteMessage.getFrom());
            Log.i(TAG, "MessageId: " + remoteMessage.getMessageId());
            Log.i(TAG, "To: " + remoteMessage.getTo());
            Log.i(TAG, "Priority: " + remoteMessage.getPriority());
            Log.i(TAG, "SentTime: " + DateTimeUtils.getDateTime(remoteMessage.getSentTime()));
            Log.i(TAG, "Ttl: " + remoteMessage.getTtl() / 60);

            Map<String, String> data = remoteMessage.getData();

            if (data != null) {
                if (!data.isEmpty()) {
                    for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                        Log.i(TAG, "key : " + entry.getKey() + ", value : " + entry.getValue());
                    }

                    try {
                        if (data.containsKey("requestType")) requestType = Integer.parseInt(data.get("requestType"));
                    } catch (NumberFormatException e) {
                        Log.i(TAG, "NumberFormatException: " + e.getMessage());
                    }

                    if (data.containsKey("thisFcmToken")) tokenForResponse = data.get("thisFcmToken");

                    if (requestType == REQUEST_TYPE_START) startGps();
                    else if (requestType == REQUEST_TYPE_STOP) stopGps();
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

    /*
    public void getLocation() {
        Log.i(TAG, "getLocation()-------------------------------------");
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "No permissions");
            }

            List<String> providers = locationManager.getAllProviders();
            for (String provider : providers){
                Log.i(TAG, "PROVIDER: " + provider);
            }

            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNetwork =  locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location locationPassive =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (locationGPS != null) {
                Log.i(TAG, "GPS: " + "LAT: " + locationGPS.getLatitude() + " LONG: " + locationGPS.getLongitude());
            } else {
                Log.i(TAG, "locationGPS == NULL");
            }

            if (locationNetwork != null){
                Log.i(TAG, "NETWORK: " + "LAT: " + locationNetwork.getLatitude() + " LONG: " + locationNetwork.getLongitude());
            } else {
                Log.i(TAG, "locationNetwork == NULL");
            }

            if (locationPassive != null){
                Log.i(TAG, "PASSIVE: " + "LAT: " + locationPassive.getLatitude() + " LONG: " + locationPassive.getLongitude());
            } else {
                Log.i(TAG, "locationPassive == NULL");
            }

            if (locationGPS != null) sendData(locationGPS);
            else if (locationNetwork != null) sendData(locationNetwork);
            else if (locationPassive != null) sendData(locationPassive);
        } else {
            Log.i(TAG, "locationManager == NULL");
        }
    }
    */

    public void sendData(Location location) {

        if (location == null) return;

        LocationData locationData = new LocationData(
                "" + location.getLatitude(),
                "" + location.getLongitude(),
                "" + location.getAccuracy(),
                "" + location.getTime());

        RequestSendPosition requestSendPosition = new RequestSendPosition(
                tokenForResponse,
                locationData);

        ApiFcm apiFcm = ControllerFcm.getRetrofitInstance().create(ApiFcm.class);
        Call<ResponseBody> callFcm = apiFcm.sendLocation(requestSendPosition);

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

    public void startGps() {

        AppUtils.appendLog("*** LocationMonitoringService - startGps() ***");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AppUtils.appendLog("LocationMonitoringService - nemá oprávnění k získání polohy");
            return;
        }

        mLocationRequest.setInterval(AppConstants.LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(AppConstants.FASTEST_LOCATION_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location mCurrentLocation = locationResult.getLastLocation();
                onLocationChanged(mCurrentLocation);
            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper());
    }

    public void stopGps() {
        AppUtils.appendLog("*** LocationMonitoringService - stopGps() ***");
        fusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void onLocationChanged(Location location) {

        if (location == null) {
            AppUtils.appendLog("LocationMonitoringService - onLocationChanged() -> location == NULL");
            return;
        }

        sendData(location);
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
}
