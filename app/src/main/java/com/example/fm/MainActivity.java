package com.example.fm;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fm.interfaces.AppPrefs_;
import com.example.fm.objects.Device;
import com.example.fm.objects.DeviceIdentification;
import com.example.fm.utils.AppConstants;
import com.example.fm.utils.AppUtils;
import com.example.fm.utils.FcmManager;
import com.example.fm.utils.PrefsUtils;
import com.facebook.stetho.Stetho;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;


@EActivity
public class MainActivity extends AppCompatActivity implements AppConstants {

    @ViewById
    TextView
            labelToken,
            labelDeviceName,
            labelRegistrationStatus,
            labelAndroidId,
            labelDeviceId,
            labelDatabaseId;

    @ViewById
    ProgressBar progressRegistration;

    @Pref
    public static AppPrefs_ appPrefs;

    @InstanceState
    public static String tokenTest;

    RelativeLayout root;

    @InstanceState
    public static boolean canLogInFile;

    @InstanceState
    boolean requestingPermissionInProgress;

    public boolean hasAllPermissionsGranted = false;

    BroadcastReceiver registrationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG_DB, "MainActivity - onCreate()");
        initStetho();

        DeviceIdentification di = AppUtils.getDeviceIdentification(this);
        Log.i(TAG_DB, "MainActivity - onCreate() - android ID: " + di.getAndroidId() + "\nMainActivity - onCreate() - device ID: " + di.getDeviceId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG_DB, "MainActivity - onResume()");
        registerRegistrationReceiver();
        //registerNewTokenReceiver();

        //Během zobrazení dialogu pro udělení oprávnění, projde MainActivity životním cyklem onPause() - onResume().
        //Při odmítnutí některého oprávnění se požadavek na oprávnění zacyklí a dokud nedojde k udělení všech
        //právnění, bude neustále vyskakovat dialog vyžadující oprávnění. Proto si tu dělám malej, krátkodobej příznak,
        //že jsem v procesu udělování oprávnění, aby se mi po onResume() oprávnění ihned nevyžadovala.
        //Zde je metoda checkPermission() proto, aby se kontrolovalo, jestli nedošlo k odebrání některého oprávnění

        if (!requestingPermissionInProgress) checkPermission();
        else requestingPermissionInProgress = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG_DB, "MainActivity - onPause()");
        unregisterMessageReceiver();
        //unregisterNewTokenReceiver();
        hasAllPermissionsGranted = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG_DB, "MainActivity - onStop()");
    }

    @AfterViews
    void afterViews() {
        labelToken.setText("Token:\n" + appPrefs.fcmToken().get());
        labelDeviceName.setText(AppUtils.getDeviceName());
        updateRegistrationViews(false);

        showDeviceIdentification(
                appPrefs.androidId().getOr("?"),
                appPrefs.deviceId().getOr("?"),
                appPrefs.databaseId().getOr(-1));


        Log.i(TAG, AppUtils.getDeviceName());
        Log.i(TAG, PrefsUtils.getPrefsToken(this));
        Log.i(TAG, "Registrováno: " + PrefsUtils.getPrefsTokenRegistrationStatus(this));
    }

    private void checkRegistrationStatus() {
        Log.i(TAG_DB, "MainActivity - checkRegistrationStatus()");

        if (!appPrefs.registered().getOr(false)) {
            Log.i(TAG_DB, "MainActivity - checkRegistrationStatus() : is not registered");

            final CountDownTimer timer = new CountDownTimer(20000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    labelToken.setText("" + millisUntilFinished / 1000);

                    if (appPrefs.fcmToken().getOr(null) != null) {
                        if (!appPrefs.fcmToken().getOr(null).equals("")) {
                            FcmManager.registerDevice(
                                    MainActivity.this,
                                    new Device(
                                            -1,
                                            AppUtils.getDeviceName(),
                                            "",
                                            appPrefs.fcmToken().get(),
                                            new DeviceIdentification(appPrefs.androidId().get(), appPrefs.deviceId().get())),
                                    appPrefs.fcmToken().get());
                            this.cancel();
                        } else {
                            Log.i(TAG_DB, "MainActivity - checkRegistrationStatus() : token is empty");
                        }
                    }
                }

                @Override
                public void onFinish() {
                    if (appPrefs.fcmToken().getOr(null) != null) {
                        if (!appPrefs.fcmToken().getOr(null).equals("")) {
                            FcmManager.registerDevice(
                                    MainActivity.this,
                                    new Device(
                                            -1,
                                            AppUtils.getDeviceName(),
                                            "",
                                            appPrefs.fcmToken().get(),
                                            new DeviceIdentification(appPrefs.androidId().get(), appPrefs.deviceId().get())),
                                    appPrefs.fcmToken().get());
                        } else {
                            Log.i(TAG_DB, "MainActivity - checkRegistrationStatus() : token is empty");
                        }
                    } else {
                        Log.i(TAG_DB, "MainActivity - checkRegistrationStatus() : token is NULL");
                    }
                }
            };

            updateRegistrationViews(true);
            timer.start();
        } else {
            Log.i(TAG_DB, "MainActivity - checkRegistrationStatus() : is registered");
        }
    }

    private void initStetho() {
        Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(this);
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this));
        initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(this));
        Stetho.Initializer initializer = initializerBuilder.build();
        Stetho.initialize(initializer);
    }

    public void checkPermission() {
        Log.i(TAG_DB, "MainActivity - checkPermission()");

        requestingPermissionInProgress = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ArrayList<String> permissionsToGrant = new ArrayList<>();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
                permissionsToGrant.add(Manifest.permission.READ_SMS);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                permissionsToGrant.add(Manifest.permission.READ_PHONE_STATE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissionsToGrant.add(Manifest.permission.ACCESS_FINE_LOCATION);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissionsToGrant.add(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (!permissionsToGrant.isEmpty()) {

                Log.i(TAG_DB, "MainActivity - checkPermission() - item to request permission:");

                String[] asArray = new String[permissionsToGrant.size()];
                for (int i = 0, count = permissionsToGrant.size(); i < count; i ++) {
                    asArray[i] = permissionsToGrant.get(i);
                    Log.i(TAG_DB, "" + asArray[i]);
                }

                ActivityCompat.requestPermissions(
                        this,
                        asArray,
                        PERMISSION_REQUEST_CODE
                );
            } else {
                checkRegistrationStatus();
            }
        } else {
            checkRegistrationStatus();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG_DB, "MainActivity - onRequestPermissionsResult()");

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    hasAllPermissionsGranted = true;

                    Log.i(TAG_DB, "MainActivity - onRequestPermissionsResult() - permissions:");
                    for (String s : permissions) {
                        Log.i(TAG_DB, "" + s);
                    }

                    for (int i = 0, count = grantResults.length; i < count; i ++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            hasAllPermissionsGranted = false;
                            Log.i(TAG_DB, "DENIED");
                        } else {
                            Log.i(TAG_DB, "GRANTED");
                        }
                    }

                    if (hasAllPermissionsGranted) {
                        Log.i(TAG_DB, "hasAllPermissionsGranted == TRUE");
                        checkRegistrationStatus();
                    } else {
                        Log.i(TAG_DB, "hasAllPermissionsGranted == FALSE");
                        labelRegistrationStatus.setText("Nemůžu registrovat zařízení, dokud nebudou udělena všechna potřebná oprávnění");
                        labelRegistrationStatus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkPermission();
                                labelRegistrationStatus.setOnClickListener(null);
                            }
                        });
                    }
                }
            }
        }
    }

    public void registerRegistrationReceiver() {
        Log.i(TAG_DB, "MainActivity - registerRegistrationReceiver()");
        registrationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG_DB, "registrationReceiver - onReceive()");

                updateRegistrationViews(false);

                PrefsUtils.updatePrefsTokenRegistrationStatus(
                        MainActivity.this,
                        intent.getBooleanExtra(EXTRA_REGISTRATION, false));

                PrefsUtils.updatePrefsDatabaseId(
                        MainActivity.this,
                        intent.getIntExtra(EXTRA_DB_DEVICE_ID, -1));

                if (intent.getStringExtra(EXTRA_MESSAGE) == null) {
                    labelRegistrationStatus.setText(intent.getBooleanExtra(EXTRA_REGISTRATION, false) ? "Registrováno" : "Neregistrováno");
                } else {
                    labelRegistrationStatus.setText(intent.getStringExtra(EXTRA_MESSAGE));
                }

                labelToken.setText(appPrefs.fcmToken().get());

                showDeviceIdentification(
                        appPrefs.androidId().getOr("?"),
                        appPrefs.deviceId().getOr("?"),
                        appPrefs.databaseId().getOr(-1));
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(registrationReceiver, new IntentFilter(ACTION_REGISTRATION));
    }

    private void unregisterMessageReceiver() {
        Log.i(TAG_DB, "MainActivity - unregisterMessageReceiver()");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(registrationReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void showDeviceIdentification(String androidId, String deviceId, int databaseId) {
        labelAndroidId.setText("Android ID:\n" + androidId);
        labelDeviceId.setText("Device ID:\n" + deviceId);
        labelDatabaseId.setText("Database ID: " + databaseId);
    }

    private void updateRegistrationViews(boolean isRegistrationRunning) {
        if (isRegistrationRunning) {
            progressRegistration.setVisibility(View.VISIBLE);
            labelRegistrationStatus.setText("Probíhá registrace...");
        } else {
            progressRegistration.setVisibility(View.GONE);
            labelRegistrationStatus.setText(appPrefs.registered().get() ? "Registrováno" : "Neregistrováno");
        }
    }
}
