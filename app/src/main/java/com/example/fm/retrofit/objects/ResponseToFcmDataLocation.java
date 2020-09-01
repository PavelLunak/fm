package com.example.fm.retrofit.objects;

public class ResponseToFcmDataLocation extends ResponseToFcmData {

    private String latitude;
    private String longitude;
    private String speed;
    private String accuracy;
    private String date;


    public ResponseToFcmDataLocation(
            int responseType,
            String thisFcmToken,
            int thisDatabaseId,
            String thisAndroidId,
            String message,
            String batteryPercentages,
            int batteryPlugged,
            int actionCode,
            String latitude,
            String longitude,
            String speed,
            String accuracy,
            String date) {

        super(responseType, thisFcmToken, thisDatabaseId, thisAndroidId, message, batteryPercentages, batteryPlugged, actionCode);

        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.accuracy = accuracy;
        this.date = date;
    }
}
