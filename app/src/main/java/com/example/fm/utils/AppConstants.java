package com.example.fm.utils;

import com.example.fm.R;
import com.example.fm.firebase.MyFirebaseMessagingService;
import com.example.fm.services.LocationMonitoringService;

public interface AppConstants {

    String PREFS_NAME = "MainActivity__AppPrefs";

    String TAG = "log_tag";
    long LOCATION_DEFAULT_INTERVAL = 1000;
    long FASTEST_DEFAULT_LOCATION_INTERVAL = 1000;
    long MAX_AGE_OF_LOCATION = 60000;

    String EXTRA_LOCATION = "extra_location";
    String EXTRA_MESSAGE = "extra_message";

    String ACTION_LOCATION_BROADCAST = LocationMonitoringService.class.getName() + "LocationBroadcast";
    String ACTION_RESTART_SERVICE_BROADCAST = LocationMonitoringService.class.getName() + "RestartService";
    String ACTION_DESTROY_SERVICE = "destroy_service";
    String ACTION_START_SERVICE = "start_service";
    String ACTION_ON_UNBIND_SERVICE = "on_unbind_service";
    String ACTION_MESSAGE = "action_message";

    int FCM_REQUEST_TYPE_SERVICE_STATUS = 1;
    int FCM_REQUEST_TYPE_SERVICE_START = 2;
    int FCM_REQUEST_TYPE_SERVICE_STOP = 3;
    int FCM_REQUEST_TYPE_GPS_START = 4;
    int FCM_REQUEST_TYPE_GPS_STOP = 5;
    int FCM_REQUEST_TYPE_CANCEL = 6;
    int FCM_REQUEST_TYPE_LOCATION = 7;
    int FCM_REQUEST_TYPE_SETTINGS_DATABASE = 8;
    int FCM_REQUEST_TYPE_LOAD_SETTINGS = 9;
    int FCM_REQUEST_TYPE_ALARM = 10;
    int FCM_REQUEST_TYPE_CALL = 101;
    int FCM_REQUEST_TYPE_LOCATION_RESULT = 102;

    int FCM_RESPONSE_SERVICE_STATUS_STARTED = 11;
    int FCM_RESPONSE_SERVICE_STATUS_STOPED = 12;
    int FCM_RESPONSE_GPS_START = 15;
    int FCM_RESPONSE_GPS_STOP = 16;
    int FCM_RESPONSE_TYPE_LOCATION = 17;
    int FCM_RESPONSE_TYPE_LOCATION_DISABLED = 18;
    int FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVED = 19;
    int FCM_RESPONSE_TYPE_SETTINGS_DATABASE_SAVE_ERROR = 20;
    int FCM_RESPONSE_TYPE_SETTINGS_LOADED = 21;
    int FCM_RESPONSE_TYPE_MESSAGE = 22;
    int FCM_RESPONSE_SERVICE_STATUS = 23;

    String KEY_RESPONSE_TYPE = "key_response_type";
    String KEY_RESPONSE_SERVICE_STATUS = "key_service_status";
    String KEY_DATA = "data";
    String KEY_TOKEN_FOR_RESPONSE = "thisFcmToken";
    String KEY_REQUEST_TYPE = "requestType";
    String KEY_MESSAGE = "message";
    String KEY_DB_ENABLED = "savingToDatabaseEnabled";
    String KEY_SAVE_INTERVAL = "autoCheckedPositionSavingInterval";
    String KEY_TIME_UNIT = "timeUnit";
    String KEY_MAX_COUNT_LOC_SAVE = "maxCountOfLocationChecked";
    String KEY_SERVICE_STATUS = "service_status";
    String KEY_GPS_STATUS = "gps_status";

    int TIME_UNIT_SECONDS = 1;
    int TIME_UNIT_MINUTES = 2;
    int TIME_UNIT_HOURS = 3;

    int STARTED = 1;
    int STOPED = 0;

    int COUNT_OF_LOCATIONS_INFINITY = -2;
    long MAX_TIME_BEFORE_BIND_TO_SERVICE = 300000;
    int REQUIRED_NUMBER_OF_LOCATIONS = 5;

    int SAVING_INTO_EXTERNAL_DB_RESULT_FAILURE = 1;
    int SAVING_INTO_EXTERNAL_DB_RESULT_SUCCES = 2;

    int PERMISSION_REQUEST_CODE = 89;
    int NO_PHONE_PERMISSIONS = 180379;
}
