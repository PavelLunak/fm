package com.example.fm.interfaces;


import org.androidannotations.annotations.sharedpreferences.SharedPref;


@SharedPref
public interface AppPrefs {
    String fcmToken();
}
