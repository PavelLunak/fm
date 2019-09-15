package com.example.fm.retrofit.objects;

public class LocationData {

    private String latitude;
    private String longitude;
    private String accuracy;
    private String date;


    public LocationData() {}

    public LocationData(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationData(String latitude, String longitude, String accuracy, String date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.date = date;
    }


    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
