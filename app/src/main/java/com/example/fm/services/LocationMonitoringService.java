package com.example.fm.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fm.MainActivity;
import com.example.fm.listeners.OnOuterDatabaseChangedListener;
import com.example.fm.objects.NewLocation;
import com.example.fm.objects.PositionChecked;
import com.example.fm.objects.ResultForRequestActualLocation;
import com.example.fm.retrofit.ApiDatabase;
import com.example.fm.retrofit.ControllerDatabase;
import com.example.fm.retrofit.requests.RequestSendPositionsToDB;
import com.example.fm.retrofit.responses.ResponseSendPositionsIntoDatabase;
import com.example.fm.utils.AppConstants;
import com.example.fm.utils.AppUtils;
import com.example.fm.utils.DateTimeUtils;
import com.example.fm.utils.FcmManager;
import com.example.fm.utils.PrefsUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocationMonitoringService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AppConstants {

    Context applicationContext;
    IBinder mBinder = new LocalBinderNewVersion();

    FusedLocationProviderClient fusedLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();
    LocationCallback mLocationCallback;

    boolean isStopRequest = false;
    NewLocation newLocation;

    int batteryPercentages;
    int batteryPlugged;

    ArrayList<PositionChecked> autoSaveBuffer;
    ArrayList<PositionChecked> autoCheckedPositions;

    //TRUE pokud chci získat jen jednu aktuální pozici
    boolean onlyGivenNumberOfPositions;

    //Pokud chci jednu aktuální pozici, bude vybrána ta s největší přesností
    // z určitého počtu pozic, které se uloží sem
    ArrayList<NewLocation> tempLocations;

    //Tady bude uložená poloha po vyžádání aktální polohy, případně chybová zpráva
    ResultForRequestActualLocation resultForRequestActualLocation;

    OnGivenLocationsCheckedListener listener;

    //Příznak, že probíhá ukládání poloh do databáze na serveru.
    //Pokud je TRUE, bude pokus o odeslání dat odložen na příště
    boolean isSavingDataToExternalDatabase;

    boolean isGpsRunning = false;

    long lastAutoSaveToDb;
    long lastCheckedPositionTime;

    //NUTNO NASTAVIT Z VENČÍ
    boolean savingToDatabaseEnabled;
    long autoCheckedPositionSavingInterval = AppConstants.LOCATION_DEFAULT_INTERVAL;
    int maxCountOfLocationChecked = -1;
    long positionInterval = LOCATION_DEFAULT_INTERVAL;

    int locationsCounter = 0;


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
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppUtils.appendLog("*** LocationMonitoringService - ondestroy() ***");

        if (!isStopRequest) {
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

        AppUtils.appendLog("LocationMonitoringService - onLocationChanged()");

        if (counter == Integer.MAX_VALUE) counter = 0;
        counter ++;
        Log.d(TAG, "counter = " + counter);

        if (location == null) {
            AppUtils.appendLog("LocationMonitoringService - onLocationChanged() -> location == NULL");
            return;
        }

        updateBatteryStatus();

        this.newLocation = new NewLocation(
                location.getTime(),
                location.getLatitude(),
                location.getLongitude(),
                location.hasSpeed() ? (location.getSpeed() * 3.6f) : 0,
                location.getAccuracy(),
                batteryPercentages,
                batteryPlugged);

        this.lastCheckedPositionTime = new Date().getTime();

        Log.d(TAG, "LocationMonitoringService - onLocationChanged() - accuracy: " + this.newLocation.getAccuracy());
        Log.d(TAG, "LocationMonitoringService - onLocationChanged() - latitude: " + this.newLocation.getLatitude());
        Log.d(TAG, "LocationMonitoringService - onLocationChanged() - longitude: " + this.newLocation.getLongitude());
        Log.d(TAG, "LocationMonitoringService - onLocationChanged() - lastUpdate: " + this.newLocation.getDate());
        Log.d(TAG, "LocationMonitoringService - onLocationChanged() - battery percentages: " + this.newLocation.getBatteryPercentages());
        Log.d(TAG, "LocationMonitoringService - onLocationChanged() - battery plugged: " + (this.newLocation.getBatterStatus() == 0 ? "Nenabíjí se" : "Nabíjí se") + " ("+ this.newLocation.getBatterStatus() + ")");

        if (onlyGivenNumberOfPositions) {
            Log.d(TAG, "POUZE URČENÝ POČET POLOH == TRUE");
            if (tempLocations == null) tempLocations = new ArrayList<>();
            tempLocations.add(newLocation);

            if (tempLocations.size() >= REQUIRED_NUMBER_OF_LOCATIONS) {
                if (this.listener != null) this.listener.onGivenLocationsChecked();
            }

            return;
        }

        savingToDatabaseEnabled = PrefsUtils.getPrefsDatabaseEnabled(this);

        Log.d(TAG, "savingToDatabaseEnabled : " + savingToDatabaseEnabled);
        Log.d(TAG, "isSavingDataToExternalDatabase : " + isSavingDataToExternalDatabase);

        if (savingToDatabaseEnabled && !isSavingDataToExternalDatabase) {
            if (checkTimeIntervalForSavePositionToDatabase()) {
                if (hasDataToSend()) {
                    if (checkMaxCountOfLocations()) {
                        saveLocationToDb();
                    } else {
                        Log.d(TAG, "> maxCount");
                    }
                } else {
                    Log.d(TAG, "hasDataToSend == FALSE");
                }
            } else {
                Log.d(TAG, "< INTERVAL");
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Service  - onConnectionFailed", Toast.LENGTH_SHORT);
    }

    private void updateBatteryStatus() {
        batteryPercentages = AppUtils.getBatteryPercentage(LocationMonitoringService.this);
        batteryPlugged = AppUtils.getBatteryIsPlugged(LocationMonitoringService.this);
    }

    public void startGps() {

        AppUtils.appendLog("*** LocationMonitoringService - startGps() ***");

        this.positionInterval = PrefsUtils.getPrefsLocationInterval(this);
        if (this.positionInterval < AppConstants.LOCATION_DEFAULT_INTERVAL) this.positionInterval = AppConstants.LOCATION_DEFAULT_INTERVAL;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AppUtils.appendLog("LocationMonitoringService - nemá oprávnění k získání polohy");
                return;
            }
        }

        isGpsRunning = true;

        mLocationRequest.setInterval(positionInterval);
        mLocationRequest.setFastestInterval(positionInterval);
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
        fusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper());
    }

    public void stopGps() {
        AppUtils.appendLog("*** LocationMonitoringService - stopGps() ***");
        //PrefsUtils.updatePrefsGpsStatus(this, false);
        isGpsRunning = false;

        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
        } else {
            Log.d(TAG, "stopGps() : fusedLocationClient == null");
        }
    }

    public boolean getGpsStatus() {
        return isGpsRunning;
    }

    public void setRequestStopService() {
        Log.d(TAG, "LocationMonitoringService - setRequestStopService()");
        isStopRequest = true;
        stopGps();
    }

    private void saveLocationToDb() {
        this.lastAutoSaveToDb = new Date().getTime();
        isSavingDataToExternalDatabase = true;
        moveDataToBuffer();
        insertIntoExternalDatabase();
    }

    //Přesune získané polohy do bufferu, ze kterého budou data odeslána do databáze na serveru
    private void moveDataToBuffer() {
        if (this.autoSaveBuffer == null) this.autoSaveBuffer = new ArrayList<>();
        this.autoSaveBuffer.addAll(this.autoCheckedPositions);
        AppUtils.appendLog("autoSaveBuffer.addAll(autoCheckedPositions) - počet položek v bufferu : " + autoSaveBuffer.size());
    }

    public RequestSendPositionsToDB prepareDataForDatabase() {
        Log.d(TAG, "prepareDataForDatabase()");

        if (autoSaveBuffer == null) {
            Log.d(TAG, "autoSaveBuffer == null");
            return null;
        }

        if (autoSaveBuffer.isEmpty()) {
            Log.d(TAG, "autoSaveBuffer.isEmpty()");
            return null;
        }

        RequestSendPositionsToDB toReturn = new RequestSendPositionsToDB();

        //Přesunutí získaných poloh do bufferu
        toReturn.setItems(new ArrayList<PositionChecked>(autoSaveBuffer));
        autoCheckedPositions = new ArrayList<>();

        return toReturn;
    }

    //Zjistí, jestli existují nějaká data k odeslání do databáze na server
    private boolean hasDataToSend() {
        if (this.autoCheckedPositions == null) {
            AppUtils.appendLog("autoCheckedPositions == null ---> return");
            return false;
        }

        if (this.autoCheckedPositions.isEmpty()) {
            AppUtils.appendLog("autoCheckedPositions.isEmpty() ---> return");
            return false;
        }

        return true;
    }

    //Zjistí, jestli už uběhl nastavený časový interval pro uložení další polohy do databáze
    //Pokud ano, přidá novou polohu do seznamu zjištěných poloh a vrátí TRUE.
    //Pokud ne, nic nepřidá a vrátí FALSE
    private boolean checkTimeIntervalForSavePositionToDatabase() {
        autoCheckedPositionSavingInterval = PrefsUtils.getPrefsLocationsAutoCheckedInterval(this);
        if (autoCheckedPositionSavingInterval == 0) autoCheckedPositionSavingInterval = 180000;

        long time = new Date().getTime();
        long difference = time - this.lastAutoSaveToDb;
        boolean hasTimeLimitToSaveCheckedPosition = difference > autoCheckedPositionSavingInterval;

        AppUtils.appendLog("LAST : " + this.lastCheckedPositionTime);
        AppUtils.appendLog("INTERVAL : " + autoCheckedPositionSavingInterval);
        AppUtils.appendLog("TIME : " + time);
        AppUtils.appendLog("DIFF : " + difference);

        //Uplynula nastavená doba mezi zjišťováním poloh
        if (hasTimeLimitToSaveCheckedPosition) {
            if (this.autoCheckedPositions == null) this.autoCheckedPositions = new ArrayList<>();
            if (this.newLocation != null) this.autoCheckedPositions.add(locationToPositionChecked());

            AppUtils.appendLog("ZÍSKÁNA POLOHA -> ukládám do mezipaměti... Počet položek v mezipaměti : " + autoCheckedPositions.size());
            AppUtils.appendLog("lastCheckedPositionTime = " + DateTimeUtils.getDateTime(lastCheckedPositionTime));
        }

        return hasTimeLimitToSaveCheckedPosition;
    }

    private boolean checkMaxCountOfLocations() {
        maxCountOfLocationChecked = PrefsUtils.getPrefsMaxCountOfCheckedLocations(this);

        if (maxCountOfLocationChecked == 0) return true;
        if (maxCountOfLocationChecked == COUNT_OF_LOCATIONS_INFINITY) return true;

        if (locationsCounter > maxCountOfLocationChecked) {
            locationsCounter = 0;
            stopGps();
            return false;
        }

        locationsCounter ++;

        return true;
    }

    //Zavolá metodu pro uložení zjištěných poloh do databáze na serveru a zpracuje výsledek
    //Pokud se uložení nepovede, budou neuložené polohy odeslány při dalším pokusu o uložení poloh do databáze na serveru
    private void insertIntoExternalDatabase() {
        AppUtils.appendLog( "Ukládání dat do externí databáze...");
        AppUtils.appendLog("insertIntoExternalDatabase()");

        sendCheckedPositionsToDatabase(new OnOuterDatabaseChangedListener() {
            @Override
            public void onOuterDatabaseChanged(int result) {
                if (result == SAVING_INTO_EXTERNAL_DB_RESULT_SUCCES) {
                    AppUtils.appendLog("uloženo do databáze...");
                    LocationMonitoringService.this.autoSaveBuffer.clear();
                } else {
                    AppUtils.appendLog("Uložení do databáze se nepodařilo...");
                }

                isSavingDataToExternalDatabase = false;
            }
        });
    }

    //Uloží zjištěné polohy do databáze na serveru
    public void sendCheckedPositionsToDatabase(final OnOuterDatabaseChangedListener listener) {

        AppUtils.appendLog("MainActivity.sendCheckedPositionsToDatabase()");
        Log.d("testovani", "Odeslání dat do externí databáze");

        final RequestSendPositionsToDB request = prepareDataForDatabase();

        if (request == null) {
            AppUtils.appendLog("RequestSendPositionsToDB == NULL ---> return");
            Log.d("testovani", "request == null");
            return;
        } else {
            AppUtils.appendLog("RequestSendPositionsToDB.itemsCount : " + request.getItems().size());
            AppUtils.appendLog("RequestSendPositionsToDB.items : ");

            for (PositionChecked p : request.getItems()) {
                AppUtils.appendLog("--------------------------------------------START");
                AppUtils.appendLog("Name : " + p.getName());
                AppUtils.appendLog("Locality : " + p.getLocality());
                AppUtils.appendLog("AdminArea : " + p.getAdminArea());
                AppUtils.appendLog("SubAdminArea : " + p.getSubAdminArea());
                AppUtils.appendLog("Thoroughfare : " + p.getThoroughfare());
                AppUtils.appendLog("FeatureName : " + p.getFeatureName());
                AppUtils.appendLog("Premises : " + p.getPremises());
                AppUtils.appendLog("PostalCode : " + p.getPostalCode());
                AppUtils.appendLog("PostalCode : " + p.getPostalCode());
                AppUtils.appendLog("--------------------------------------------END");
            }
        }

        ApiDatabase api = ControllerDatabase.getRetrofitInstance().create(ApiDatabase.class);

        final Call<ResponseSendPositionsIntoDatabase> call = api.sendCheckedPositions(request);

        call.enqueue(new Callback<ResponseSendPositionsIntoDatabase>() {
            @Override
            public void onResponse(Call<ResponseSendPositionsIntoDatabase> call, Response<ResponseSendPositionsIntoDatabase> response) {
                Log.d("testovani", "onResponse()");
                if (response.isSuccessful()) {
                    AppUtils.appendLog("response.isSuccessful() CODE : " + response.code());
                    Log.d("testovani", "isSuccessful - CODE: " + response.code());

                    /*
                    ResponseBody responseBody = response.body();
                    try {
                        Log.d("testovani", "isSuccessful - CODE: " + responseBody.string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    */

                    ResponseSendPositionsIntoDatabase apiResponse = response.body();
                    List<Integer> savedPositionsIds = apiResponse.getSaved_positions_ids();
                    List<String> messagesFromApi = apiResponse.getMessages();

                    Log.d("testovani", "Vložená ID :");
                    StringBuilder sb = new StringBuilder("Vložená ID do databáze : ");
                    for (Integer i : savedPositionsIds) {
                        Log.d("testovani", "ID: " + i);
                        sb.append(i);
                        sb.append(" ");
                    }
                    AppUtils.appendLog(sb.toString());

                    Log.d("testovani", "Chybové zprávy :");
                    sb = new StringBuilder("Zprávy z API : ");
                    if (messagesFromApi == null) {
                        Log.d("testovani", "Messages == NULL");
                        sb.append("Messages == NULL");
                        //DialogInfo.createDialog(MainActivity.this).setTitle("Info").setMessage("Polohy úspěšně vloženy do databáze").show();
                    } else {
                        if (!messagesFromApi.isEmpty()) {
                            for (String s : messagesFromApi) {
                                Log.d("testovani", "Message: " + s);
                                sb.append(s);
                                sb.append("\n");
                            }
                        } else {
                            //DialogInfo.createDialog(MainActivity.this).setTitle("Info").setMessage("Polohy úspěšně vloženy do databáze").show();
                            Log.d("testovani", "Messages isEmpty");
                            sb.append("Messages isEmpty");
                        }
                    }
                    AppUtils.appendLog(sb.toString());

                    if (listener != null) {
                        listener.onOuterDatabaseChanged(SAVING_INTO_EXTERNAL_DB_RESULT_SUCCES);
                    }
                } else {
                    AppUtils.appendLog("!response.isSuccessful() CODE : " + response.code());
                    Log.d("testovani", "CHYBA 2");

                    if (listener != null) {
                        listener.onOuterDatabaseChanged(SAVING_INTO_EXTERNAL_DB_RESULT_FAILURE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseSendPositionsIntoDatabase> call, Throwable t) {
                Log.d("testovani", "onFailure()");
                Log.d("testovani", t.getMessage());

                AppUtils.appendLog("response.onFailure()");
                AppUtils.appendLog("message : " + t.getMessage());

                if (listener != null) {
                    listener.onOuterDatabaseChanged(SAVING_INTO_EXTERNAL_DB_RESULT_FAILURE);
                }
            }
        });
    }

    //Z objektu typu Location vytvoří objekt typu PositionChecked
    private PositionChecked locationToPositionChecked() {

        PositionChecked newCheckedPosition = new PositionChecked();

        newCheckedPosition.setId(-1);
        newCheckedPosition.setDevice_id(PrefsUtils.getPrefsDatabaseId(LocationMonitoringService.this));
        newCheckedPosition.setDate(this.newLocation.getDate());
        newCheckedPosition.setLatitude(this.newLocation.getLatitude());
        newCheckedPosition.setLongitude(this.newLocation.getLongitude());
        newCheckedPosition.setSpeed(this.newLocation.getSpeed());
        newCheckedPosition.setAccuracy(this.newLocation.getAccuracy());

        setPositionAddressData(newCheckedPosition);

        return newCheckedPosition;
    }

    //Pokusí se zjištěné poloze přiřadit informace o adrese
    public void setPositionAddressData(PositionChecked positionChecked) {
        Address address = AppUtils.getAddressForLocation(getApplicationContext(), positionChecked.getLatitude(), positionChecked.getLongitude());

        String positionName;

        if (address != null) {
            positionName = ""
                    + (address.getThoroughfare() != null ? address.getThoroughfare() : address.getSubAdminArea())
                    + " "
                    + (address.getFeatureName().equals(address.getThoroughfare()) ? "" : address.getFeatureName())
                    + ", "
                    + address.getLocality();

            positionChecked.setName(positionName);
            positionChecked.setCountryCode(address.getCountryCode() == null ? "?" : address.getCountryCode());
            positionChecked.setCountryName(address.getCountryName() == null ? "?" : address.getCountryName());
            positionChecked.setFeatureName(address.getFeatureName() == null ? "?" : address.getFeatureName());
            positionChecked.setLocality(address.getLocality() == null ? "?" : address.getLocality());
            positionChecked.setPhone(address.getPhone() == null ? "?" : address.getPhone());
            positionChecked.setPostalCode(address.getPostalCode() == null ? "?" : address.getPostalCode());
            positionChecked.setPremises(address.getPremises() == null ? "?" : address.getPremises());
            positionChecked.setAdminArea(address.getAdminArea() == null ? "?" : address.getAdminArea());
            positionChecked.setSubAdminArea(address.getSubAdminArea() == null ? "?" : address.getSubAdminArea());
            positionChecked.setSubLocality(address.getSubLocality() == null ? "?" : address.getSubLocality());
            positionChecked.setThoroughfare(address.getThoroughfare() == null ? "?" : address.getThoroughfare());
            positionChecked.setSubThoroughfare(address.getSubThoroughfare() == null ? "?" : address.getSubThoroughfare());
            positionChecked.setUrl(address.getUrl() == null ? "?" : address.getUrl());
            positionChecked.setExtras(address.getExtras());

        } else {
            positionChecked.setId(-1);
            positionChecked.setName("Jméno ???");
            positionChecked.setCountryCode("?");
            positionChecked.setCountryName("?");
            positionChecked.setFeatureName("?");
            positionChecked.setLocality("?");
            positionChecked.setPhone("?");
            positionChecked.setPostalCode("?");
            positionChecked.setPremises("?");
            positionChecked.setAdminArea("?");
            positionChecked.setSubAdminArea("?");
            positionChecked.setSubLocality("?");
            positionChecked.setThoroughfare("?");
            positionChecked.setSubThoroughfare("?");
            positionChecked.setUrl("?");
        }
    }

    // countOfLocations - počet poloh, které se zjistí a z nich se vybere ta s největší přesností
    public void getActualLocation(int countOfLocations) {
        Log.d(TAG, "LocationMonitoringService - getActualLocation(" + countOfLocations + ")");

        this.listener = new OnGivenLocationsCheckedListener() {
            @Override
            public void onGivenLocationsChecked() {
                Log.d(TAG, "LocationMonitoringService - OnGivenLocationsCheckedListener - onGivenLocationsChecked()");

                stopGps();
                onlyGivenNumberOfPositions = false;
                NewLocation loc = getBestAccuracyLocation(LocationMonitoringService.this.tempLocations) ;

                if (loc != null) FcmManager.sendResponseLocation(LocationMonitoringService.this, loc, batteryPercentages, batteryPlugged);
                else FcmManager.sendMessage(LocationMonitoringService.this,"Získaná poloha == NULL", batteryPercentages, batteryPlugged, MainActivity.tokenForResponse, ACTION_MESSAGE_CODE_NONE);

                positionInterval = PrefsUtils.getPrefsLocationInterval(LocationMonitoringService.this);
                Intent intent;

                if (loc != null) {
                    intent = new Intent(ACTION_LOCATION_BROADCAST);
                    intent.putExtra(EXTRA_LOCATION, loc);
                    LocalBroadcastManager.getInstance(LocationMonitoringService.this).sendBroadcast(intent);
                } else {
                    intent = new Intent(ACTION_MESSAGE);
                    intent.putExtra(EXTRA_MESSAGE, "Nepodařilo se získat polohu zařízení.");
                    LocalBroadcastManager.getInstance(LocationMonitoringService.this).sendBroadcast(intent);
                }

                LocationMonitoringService.this.tempLocations = null;
            }
        };

        onlyGivenNumberOfPositions = true;
        positionInterval = AppConstants.LOCATION_DEFAULT_INTERVAL;
        startGps();
    }

    private NewLocation getBestAccuracyLocation(ArrayList<NewLocation> locations) {
        Log.d(TAG, "LocationMonitoringService - getBestAccuracyLocation()");
        if (locations == null) {
            Log.d(TAG, "LocationMonitoringService - getBestAccuracyLocation() : locations == null");
            return null;
        }

        if (locations.isEmpty()) {
            Log.d(TAG, "LocationMonitoringService - getBestAccuracyLocation() : locations.isEmpty()");
            return null;
        }

        NewLocation toReturn = null;

        for (NewLocation nl : locations) {
            Log.d(TAG, "LocationMonitoringService - getBestAccuracyLocation() - iterate - accuracy : " + nl.getAccuracy());
            if (toReturn == null) {
                toReturn = nl;
                continue;
            }

            if (nl.getAccuracy() < toReturn.getAccuracy()) toReturn = nl;
        }

        Log.d(TAG, "LocationMonitoringService - getBestAccuracyLocation() - toReturn accuracy : " + toReturn.getAccuracy());
        return toReturn;
    }

    interface OnGivenLocationsCheckedListener {
        void onGivenLocationsChecked();
    }
}