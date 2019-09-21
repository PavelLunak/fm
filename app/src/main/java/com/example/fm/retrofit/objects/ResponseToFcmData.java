package com.example.fm.retrofit.objects;

import com.example.fm.utils.AppUtils;

public class ResponseToFcmData {

    private int responseType;
    private String message;
    private String battery;


    public ResponseToFcmData() {}

    public ResponseToFcmData(int responseType, String message, String battery) {
        this.responseType = responseType;
        this.message = message;
        this.battery = battery;
    }


    @Override
    public String toString() {
        return new StringBuilder("ResponseToFcmData:")
                .append("\nresponseType: ")
                .append(AppUtils.responseTypeToString(responseType))
                .append("\nmessage: ")
                .append(message == null ? "null" : message)
                .append("\nbattery: ")
                .append(battery)
                .toString();
    }
}
