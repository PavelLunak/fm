package com.example.fm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.fm.interfaces.AppPrefs_;
import com.example.fm.utils.AppConstants;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity
public class MainActivity extends AppCompatActivity implements AppConstants {

    @Pref
    public static AppPrefs_ appPrefs;

    @ViewById
    TextView label;

    @InstanceState
    public static boolean canLogInFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label.setText(appPrefs.fcmToken().get());
        Log.i(TAG, appPrefs.fcmToken().get());
    }
}
