package com.example.fm.retrofit.objects;

public class ResponseToFcmDataLocation extends ResponseToFcmData {

    private String latitude;
    private String longitude;
    private String speed;
    private String accuracy;
    private String date;


    public ResponseToFcmDataLocation(
            int responseType,
            String message,
            String batteryPercentages,
            int batteryPlugged,
            String latitude,
            String longitude,
            String speed,
            String accuracy,
            String date) {

        super(responseType, message, batteryPercentages, batteryPlugged);

        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.accuracy = accuracy;
        this.date = date;
    }
}
