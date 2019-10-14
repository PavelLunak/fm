package com.example.fm.retrofit.objects;

import com.example.fm.utils.AppUtils;

public class ResponseToFcmData {

    private int responseType;
    private String message;
    private String batteryPercentages;
    private int batteryPlugged;


    public ResponseToFcmData() {}

    public ResponseToFcmData(int responseType, String message, String batteryPercentages, int batteryPlugged) {
        this.responseType = responseType;
        this.message = message;
        this.batteryPercentages = batteryPercentages;
        this.batteryPlugged = batteryPlugged;
    }


    @Override
    public String toString() {
        return new StringBuilder("ResponseToFcmData:")
                .append("\nresponseType: ")
                .append(AppUtils.responseTypeToString(responseType))
                .append("\nmessage: ")
                .append(message == null ? "null" : message)
                .append("\nbattery %: ")
                .append(batteryPercentages).append("%")
                .append("\nbattery status: ")
                .append(batteryPlugged == 0 ? "NENABÍJÍ SE" : "NANABÍJÍ SE")
                .toString();
    }
}
