package com.example.fm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.fm.MainActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import static com.example.fm.utils.AppConstants.TAG;


public class AppUtils {

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
}
