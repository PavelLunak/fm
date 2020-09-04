package com.example.fm;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
    public static String tokenForResponse, androidIdForResponse;

    @InstanceState
    public static boolean canLogInFile;

    @InstanceState
    boolean requestingPermissionInProgress;

    @InstanceState
    boolean requestingRegistrationInProgress;

    public boolean hasAllPermissionsGranted = false;

    BroadcastReceiver registrationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initStetho();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerRegistrationReceiver();

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
        unregisterMessageReceiver();
        hasAllPermissionsGranted = false;
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
    }

    private void checkRegistrationStatus() {
        if (!appPrefs.registered().getOr(false)) {
            Log.d(TAG, "Zařízení není registrováno");

            if (!AppUtils.isOnline(this)) {
                Log.d(TAG, "Zařízení není online");
                labelRegistrationStatus.setText("Zařízení není registrováno. Registraci nelze dokončit - není dostupné připojení k internetu.");
                return;
            }

            CountDownTimer timer = new CountDownTimer(20000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    if (appPrefs.registered().get()) {
                        requestingRegistrationInProgress = false;
                        return;
                    }

                    labelToken.setText("" + millisUntilFinished / 1000);

                    if (requestingRegistrationInProgress) {
                        return;
                    }

                    String token = appPrefs.fcmToken().getOr("");
                    Log.d(TAG, "TOKEN: " + appPrefs.fcmToken().getOr("null"));

                    if (!token.equals("")) {
                        requestingRegistrationInProgress = true;

                        AppUtils.appendLog("Registrace zařízení...");

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
                    }
                }

                @Override
                public void onFinish() {
                    requestingRegistrationInProgress = false;
                    updateRegistrationViews(false);
                }
            };

            updateRegistrationViews(true);
            timer.start();
        } else {
            Log.d(TAG, "Zařízení je registrováno");
            Log.d(TAG, "TOKEN: " + appPrefs.fcmToken().getOr("null"));
            updateRegistrationViews(false);
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
                String[] asArray = new String[permissionsToGrant.size()];
                for (int i = 0, count = permissionsToGrant.size(); i < count; i ++) {
                    asArray[i] = permissionsToGrant.get(i);
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
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    hasAllPermissionsGranted = true;

                    for (int i = 0, count = grantResults.length; i < count; i ++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            hasAllPermissionsGranted = false;
                        }
                    }

                    if (hasAllPermissionsGranted) {
                        checkRegistrationStatus();
                    } else {
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
        registrationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

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

                AppUtils.appendLog("Zařízení registrováno...");
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(registrationReceiver, new IntentFilter(ACTION_REGISTRATION));
    }

    private void unregisterMessageReceiver() {
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
