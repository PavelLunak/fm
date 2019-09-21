package com.example.fm.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.fm.utils.AppConstants;
import com.example.fm.utils.AppUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class LocationMonitoringService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AppConstants {

    IBinder mBinder = new LocalBinderNewVersion();

    FusedLocationProviderClient fusedLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();
    LocationCallback mLocationCallback;

    Context applicationContext;

    boolean isStopRequestFromUI = false;

    Location location;                                  //Získaná pozice
    float accuracy;                                     //Přesnost získané pozice

    //Data připravená k odeslání a zobrazení
    double latitude = 0;
    double longitude = 0;
    long lastUpdate = 0;                                //Čas poslední přijaté polohy


    public class LocalBinderNewVersion extends Binder {
        public LocationMonitoringService getService() {
            return LocationMonitoringService.this;
        }
    }


    public LocationMonitoringService() {
        super();
    }

    public LocationMonitoringService(Context applicationContext) {
        super();
        AppUtils.appendLog("*** LocationMonitoringService - construct ***");
        this.applicationContext = applicationContext;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppUtils.appendLog("*** LocationMonitoringService - onStartCommand() ***");
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_START_SERVICE));

        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppUtils.appendLog("*** LocationMonitoringService - onBind() ***");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppUtils.appendLog("*** LocationMonitoringService - onUnbind() ***");
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_ON_UNBIND_SERVICE));
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppUtils.appendLog("*** LocationMonitoringService - ondestroy() ***");

        if (!isStopRequestFromUI) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_RESTART_SERVICE_BROADCAST));
        } else {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_DESTROY_SERVICE));
        }
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        AppUtils.appendLog("LocationMonitoringService - onConnected()");
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        AppUtils.appendLog("*** LocationMonitoringService - onConnectionSuspended() ***");
    }


    int counter = 0;

    public void onLocationChanged(Location location) {

        if (location == null) {
            AppUtils.appendLog("LocationMonitoringService - onLocationChanged() -> location == NULL");
            return;
        }

        if (counter == Integer.MAX_VALUE) counter = 0;
        counter ++;
        Log.i(TAG, "counter = " + counter);


        this.location = location;
        this.accuracy = location.getAccuracy();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.lastUpdate = location.getTime();

        sendPositionToUI(location);
    }

    private void sendPositionToUI(Location location) {
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Service  - onConnectionFailed", Toast.LENGTH_SHORT);
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

    public void setRequestStopService() {
        Log.i(TAG, "LocationMonitoringService - setRequestStopService()");
        isStopRequestFromUI = true;
        if (fusedLocationClient != null) fusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}