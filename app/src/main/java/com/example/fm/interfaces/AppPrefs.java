package com.example.fm.interfaces;


import org.androidannotations.annotations.sharedpreferences.SharedPref;


@SharedPref
public interface AppPrefs {
    String fcmToken();
    boolean savingToDatabaseEnabled();
    long autoCheckedPositionSavingInterval();
    long positionInterval();
    int maxCountOfLocationChecked();
    int timeUnit();
    boolean requestLocation();
}
