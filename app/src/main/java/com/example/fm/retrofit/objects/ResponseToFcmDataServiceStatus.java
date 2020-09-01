package com.example.fm.retrofit.objects;

public class ResponseToFcmDataServiceStatus extends ResponseToFcmData {

    private int serviceStatus;
    private int gpsStatus;


    public ResponseToFcmDataServiceStatus(
            int responseType,
            String thisFcmToken,
            int thisDatabaseId,
            String thisAndroidId,
            String message,
            String batteryPercentages,
            int batteryPlugged,
            int actionCode,
            int serviceStatus,
            int gpsStatus) {

        super(responseType, thisFcmToken, thisDatabaseId, thisAndroidId, message, batteryPercentages, batteryPlugged, actionCode);
        this.serviceStatus = serviceStatus;
        this.gpsStatus = gpsStatus;
    }
}
