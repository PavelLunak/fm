package com.example.fm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fm.interfaces.AppPrefs_;
import com.example.fm.listeners.OnOuterDatabaseChangedListener;
import com.example.fm.objects.PositionChecked;
import com.example.fm.retrofit.ApiDatabase;
import com.example.fm.retrofit.ControllerDatabase;
import com.example.fm.retrofit.requests.RequestSendPositionsToDB;
import com.example.fm.retrofit.responses.ResponseSendPositionsIntoDatabase;
import com.example.fm.utils.AppConstants;
import com.example.fm.utils.AppUtils;
import com.facebook.stetho.Stetho;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@EActivity
public class MainActivity extends AppCompatActivity implements AppConstants {

    @Pref
    public static AppPrefs_ appPrefs;

    RelativeLayout root;
    TextView label;

    @InstanceState
    public static boolean canLogInFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label = (TextView) findViewById(R.id.label);
        root = (RelativeLayout) findViewById(R.id.root);

        initStetho();
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Click(R.id.root)
    void clickRoot() {
        label.setText(appPrefs.fcmToken().get());
        Log.i(TAG, appPrefs.fcmToken().get());
    }

    private void initStetho() {
        Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(this);
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this));
        initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(this));
        Stetho.Initializer initializer = initializerBuilder.build();
        Stetho.initialize(initializer);
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_SMS,
                                Manifest.permission.READ_PHONE_NUMBERS,
                                Manifest.permission.READ_PHONE_STATE},
                        PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {}
                }
            }
        }
    }
}
