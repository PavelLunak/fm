package com.example.fm.interfaces;


import org.androidannotations.annotations.sharedpreferences.SharedPref;


@SharedPref
public interface AppPrefs {
    String fcmToken();
    int databaseId();
    long locationInterval();
    int locationIntervalTimeUnit();
    boolean savingToDatabaseEnabled();
    long autoCheckedPositionSavingInterval();
    int maxCountOfLocationChecked();
    int timeUnit();
    boolean requestLocation();
    boolean registered(); //Token úspěšně odeslán do databáze
    String androidId();
    String deviceId();
}
