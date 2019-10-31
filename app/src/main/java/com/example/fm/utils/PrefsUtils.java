package com.example.fm.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsUtils implements AppConstants {

    //STAV REGISTRACE TOKENU
    // -------------------------------------------------
    public static void updatePrefsTokenRegistrationStatus(Context context, boolean registered) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("registered", registered);
        editor.commit();
    }

    public static boolean getPrefsTokenRegistrationStatus(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("registered", false);
    }
    // -------------------------------------------------

    //TOKEN
    // -------------------------------------------------
    public static void updatePrefsToken(Context context, String token) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("fcmToken", token);
        editor.commit();
    }

    public static String getPrefsToken(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString("fcmToken", "");
    }
    // -------------------------------------------------

    //ID PŘIDĚLENÉ ZAŘÍZENÍ DATABÁZÍ
    // -------------------------------------------------
    public static void updatePrefsDatabaseId(Context context, int id) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("databaseId", id);
        editor.commit();
    }

    public static int getPrefsDatabaseId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("databaseId", -1);
    }
    // -------------------------------------------------

    //ADROID ID
    // -------------------------------------------------
    public static void updateAndroidId(Context context, String androidId) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("androidId", androidId);
        editor.commit();
    }

    public static String getAndroidId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString("androidId", "");
    }
    // -------------------------------------------------

    //DEVICE ID
    // -------------------------------------------------
    public static void updateDeviceId(Context context, String androidId) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("deviceId", androidId);
        editor.commit();
    }

    public static String getDeviceId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString("deviceId", "");
    }
    // -------------------------------------------------

    /*
    //GPS STATUS
    // -------------------------------------------------
    public static void updatePrefsGpsStatus(Context context, boolean isRunning) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("gpsIsRunning", isRunning);
        editor.commit();
    }

    public static boolean getPrefsGpsStatus(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("gpsIsRunning", false);
    }
    // -------------------------------------------------
    */

    //LOCATION INTERVAL
    // -------------------------------------------------
    public static void updatePrefsLocationInterval(Context context, long interval) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong("locationInterval", interval);
        editor.commit();
    }

    public static long getPrefsLocationInterval(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getLong("locationInterval", LOCATION_DEFAULT_INTERVAL);
    }
    // -------------------------------------------------

    //DATABASE ENABLED
    // -------------------------------------------------
    public static void updatePrefsDatabaseEnabled(Context context, boolean enabled) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("savingToDatabaseEnabled", enabled);
        editor.commit();
    }

    public static boolean getPrefsDatabaseEnabled(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("savingToDatabaseEnabled", false);
    }
    // -------------------------------------------------

    //LOCATION AUTOCHECK INTERVAL
    // -------------------------------------------------
    public static void updatePrefsLocationsAutoCheckedInterval(Context context, long interval) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong("autoCheckedPositionSavingInterval", interval);
        editor.commit();
    }

    public static long getPrefsLocationsAutoCheckedInterval(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getLong("autoCheckedPositionSavingInterval", AppConstants.LOCATION_DEFAULT_INTERVAL);
    }
    // -------------------------------------------------

    //CHECKED LOCATIONS COUNT LIMIT
    // -------------------------------------------------
    public static void updatePrefsMaxCountOfCheckedLocations(Context context, int count) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("maxCountOfLocationChecked", count);
        editor.commit();
    }

    public static int getPrefsMaxCountOfCheckedLocations(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("maxCountOfLocationChecked", AppConstants.REQUIRED_NUMBER_OF_LOCATIONS);
    }
    // -------------------------------------------------
}
