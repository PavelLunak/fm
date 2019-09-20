package com.example.fm.utils;

import com.example.fm.R;
import com.example.fm.firebase.MyFirebaseMessagingService;
import com.example.fm.services.LocationMonitoringService;

public interface AppConstants {

    public static final String TAG = "log_tag";
    public static final int LOCATION_INTERVAL = 5000;
    public static final int FASTEST_LOCATION_INTERVAL = 5000;

    int REQUEST_TYPE_START = 1;
    int REQUEST_TYPE_STOP = 2;

    public static final String EXTRA_LOCATION = "extra_location";

    public static final String ACTION_LOCATION_BROADCAST = LocationMonitoringService.class.getName() + "LocationBroadcast";
    public static final String ACTION_RESTART_SERVICE_BROADCAST = LocationMonitoringService.class.getName() + "RestartService";
    public static final String ACTION_DESTROY_SERVICE = "destroy_service";
    public static final String ACTION_START_SERVICE = "start_service";
    public static final String ACTION_ON_UNBIND_SERVICE = "on_unbind_service";

    int FCM_REQUEST_TYPE_SERVICE_STATUS = 1;
    int FCM_REQUEST_TYPE_SERVICE_START = 2;
    int FCM_REQUEST_TYPE_SERVICE_STOP = 3;
    int FCM_REQUEST_TYPE_GPS_START = 4;
    int FCM_REQUEST_TYPE_GPS_STOP = 5;

    int FCM_RESPONSE_SERVICE_STATUS_STARTED = 11;
    int FCM_RESPONSE_SERVICE_STATUS_STOPED = 12;
    int FCM_RESPONSE_SERVICE_START = 13;
    int FCM_RESPONSE_SERVICE_STOP = 14;
    int FCM_RESPONSE_GPS_START = 15;
    int FCM_RESPONSE_GPS_STOP = 16;
    int FCM_RESPONSE_TYPE_LOCATION = 17;
    int FCM_RESPONSE_TYPE_MESSAGE = 18;
    int FCM_RESPONSE_TYPE_MESSAGE_ERROR = 19;

    String KEY_RESPONSE_TYPE = "key_response_type";
    String KEY_RESPONSE_SERVICE_STATUS = "key_service_status";
    String KEY_DATA = "data";
    String KEY_TOKEN_FOR_RESPONSE = "thisFcmToken";
    String KEY_REQUEST_TYPE = "requestType";
    String KEY_MESSAGE = "message";
}
