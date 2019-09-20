package com.example.fm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fm.interfaces.AppPrefs_;
import com.example.fm.utils.AppConstants;
import com.facebook.stetho.Stetho;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.sharedpreferences.Pref;

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
}
