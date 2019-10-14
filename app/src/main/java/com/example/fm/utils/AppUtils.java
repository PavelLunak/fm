package com.example.fm.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.fm.MainActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.telephony.TelephonyManager.SIM_STATE_ABSENT;
import static android.telephony.TelephonyManager.SIM_STATE_CARD_IO_ERROR;
import static android.telephony.TelephonyManager.SIM_STATE_CARD_RESTRICTED;
import static android.telephony.TelephonyManager.SIM_STATE_NETWORK_LOCKED;
import static android.telephony.TelephonyManager.SIM_STATE_NOT_READY;
import static android.telephony.TelephonyManager.SIM_STATE_PERM_DISABLED;
import static android.telephony.TelephonyManager.SIM_STATE_PIN_REQUIRED;
import static android.telephony.TelephonyManager.SIM_STATE_PUK_REQUIRED;
import static android.telephony.TelephonyManager.SIM_STATE_READY;
import static android.telephony.TelephonyManager.SIM_STATE_UNKNOWN;


public class AppUtils implements AppConstants {

    public static boolean isNumber(String string) {
        if (string == null) return false;
        if (string.length() == 0) return false;

        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.indexOf(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean isWifi(MainActivity activity) {
        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public static void appendLog(String text) {
        Log.i(TAG, text);
        if (!MainActivity.canLogInFile) return;

        File directory = new File((Environment.getExternalStorageDirectory().toString()), "GpsUtils");
        File file = new File(
                (Environment.getExternalStorageDirectory().toString()) +
                        File.separator +
                        "GpsUtils" +
                        File.separator +
                        DateTimeUtils.getDateTimeForLog(new Date().getTime()) +
                        "_log.txt");

        if(!directory.exists()) directory.mkdir();

        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        String textWithDate = DateTimeUtils.getDateTime(new Date().getTime()) + " \t\t: " + text;

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(textWithDate);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteLogFile(MainActivity activity) {
        File directory = new File((Environment.getExternalStorageDirectory().toString()), "GpsUtils");

        File file = new File(
                (Environment.getExternalStorageDirectory().toString()) +
                        File.separator +
                        "GpsUtils");

        if (file.isDirectory()) {
            File[] files = file.listFiles();

            for (int i = 0, count = files.length; i < count; i ++) {
                files[i].delete();
            }
        }

        Toast.makeText(activity, "Logovací soubor smazán", Toast.LENGTH_LONG).show();
    }

    public static boolean hasInternet(MainActivity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null) return false;
        return activeNetworkInfo.isConnected();
    }

    public static String responseTypeToString(int responseType) {
        switch (responseType) {
            case FCM_RESPONSE_SERVICE_STATUS_STARTED:
                return "FCM_RESPONSE_SERVICE_STATUS_STARTED";
            case FCM_RESPONSE_SERVICE_STATUS_STOPED:
                return "FCM_RESPONSE_SERVICE_STATUS_STOPED";
            case FCM_RESPONSE_GPS_START:
                return "FCM_RESPONSE_GPS_START";
            case FCM_RESPONSE_GPS_STOP:
                return "FCM_RESPONSE_GPS_STOP";
            case FCM_RESPONSE_TYPE_LOCATION:
                return "FCM_RESPONSE_TYPE_LOCATION";
            case FCM_RESPONSE_TYPE_LOCATION_DISABLED:
                return "FCM_RESPONSE_TYPE_LOCATION_DISABLED";
            case FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVED:
                return "FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVED";
            case FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVE_ERROR:
                return "FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVE_ERROR";
            case FCM_RESPONSE_TYPE_SETTINGS_LOADED:
                return "FCM_RESPONSE_TYPE_SETTINGS_LOADED";
            case FCM_RESPONSE_TYPE_MESSAGE:
                return "FCM_RESPONSE_TYPE_MESSAGE";
            case FCM_RESPONSE_SERVICE_STATUS:
                return "FCM_RESPONSE_SERVICE_STATUS";
            default: return "unknown";
        }
    }

    public static String requestTypeToString(int requestType) {
        switch (requestType) {
            case FCM_REQUEST_TYPE_SERVICE_STATUS:
                return "FCM_REQUEST_TYPE_SERVICE_STATUS";
            case FCM_REQUEST_TYPE_SERVICE_START:
                return "FCM_REQUEST_TYPE_SERVICE_START";
            case FCM_REQUEST_TYPE_SERVICE_STOP:
                return "FCM_REQUEST_TYPE_SERVICE_STOP";
            case FCM_REQUEST_TYPE_GPS_START:
                return "FCM_REQUEST_TYPE_GPS_START";
            case FCM_REQUEST_TYPE_GPS_STOP:
                return "FCM_REQUEST_TYPE_GPS_STOP";
            case FCM_REQUEST_TYPE_LOCATION:
                return "FCM_REQUEST_TYPE_LOCATION";
            case FCM_REQUEST_TYPE_SETTINGS_DATABASE:
                return "FCM_REQUEST_TYPE_SETTINGS_DATABASE";
            case FCM_REQUEST_TYPE_LOAD_SETTINGS:
                return "FCM_REQUEST_TYPE_LOAD_SETTINGS";
            case FCM_REQUEST_TYPE_ALARM:
                return "FCM_REQUEST_TYPE_ALARM";
            case FCM_REQUEST_TYPE_CALL:
                return "FCM_REQUEST_TYPE_CALL";
            default: return "unknown";
        }
    }

    public static String simStateToString(int simState) {
        switch (simState) {
            case SIM_STATE_UNKNOWN:
                return "SIM_STATE_UNKNOWN (" + SIM_STATE_UNKNOWN + ")";
            case SIM_STATE_ABSENT:
                return "SIM_STATE_ABSENT (" + SIM_STATE_ABSENT + ")";
            case SIM_STATE_PIN_REQUIRED:
                return "SIM_STATE_PIN_REQUIRED (" + SIM_STATE_PIN_REQUIRED + ")";
            case SIM_STATE_PUK_REQUIRED:
                return "SIM_STATE_PUK_REQUIRED (" + SIM_STATE_PUK_REQUIRED + ")";
            case SIM_STATE_NETWORK_LOCKED:
                return "SIM_STATE_NETWORK_LOCKED (" + SIM_STATE_NETWORK_LOCKED + ")";
            case SIM_STATE_READY:
                return "SIM_STATE_READY (" + SIM_STATE_READY + ")";
            case SIM_STATE_NOT_READY:
                return "SIM_STATE_NOT_READY (" + SIM_STATE_NOT_READY + ")";
            case SIM_STATE_PERM_DISABLED:
                return "SIM_STATE_PERM_DISABLED (" + SIM_STATE_PERM_DISABLED + ")";
            case SIM_STATE_CARD_IO_ERROR:
                return "SIM_STATE_CARD_IO_ERROR (" + SIM_STATE_CARD_IO_ERROR + ")";
            case SIM_STATE_CARD_RESTRICTED:
                return "SIM_STATE_CARD_RESTRICTED (" + SIM_STATE_CARD_RESTRICTED + ")";
            default: return "unknown";
        }
    }

    public static Boolean isLocationEnabled(Context context) {
        int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
        return  (mode != Settings.Secure.LOCATION_MODE_OFF);
    }

    public static Address getAddressForLocation(Context context, double la, double lo) {

        try {
            double latitude = la;
            double longitude = lo;
            int maxResults = 1;

            Geocoder gc = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = gc.getFromLocation(latitude, longitude, maxResults);

            if (addresses.size() == 1) {
                return addresses.get(0);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setPrefsRequsetLocation(Context context, boolean isRequest) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("requestLocation", isRequest);
        editor.commit();
    }

    public static int getBatteryPercentage(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        int plugged = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;

        float batteryPct = level / (float) scale;

        Log.i(TAG, "battery2: " + ((int) (batteryPct * 100)));
        return (int) (batteryPct * 100);
    }

    public static int getBatteryIsPlugged(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        //0 = nedobíjí se
        //pokud je cokoliv jiného než 0, dobíjí se
        return batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;
    }
}
