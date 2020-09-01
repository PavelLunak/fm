package com.example.fm.retrofit.objects;

import com.example.fm.utils.AppUtils;

public class ResponseToFcmData {

    private int responseType;
    private String thisFcmToken;
    private int thisDatabaseId;
    private String thisAndroidId;
    private String message;
    private String batteryPercentages;
    private int batteryPlugged;
    private int actionCode;


    public ResponseToFcmData() {}

    public ResponseToFcmData(
            int responseType,
            String thisFcmToken,
            int thisDatabaseId,
            String thisAndroidId,
            String message,
            String batteryPercentages,
            int batteryPlugged,
            int actionCode) {

        this.responseType = responseType;
        this.thisFcmToken = thisFcmToken;
        this.thisDatabaseId = thisDatabaseId;
        this.thisAndroidId = thisAndroidId;
        this.message = message;
        this.batteryPercentages = batteryPercentages;
        this.batteryPlugged = batteryPlugged;
        this.actionCode = actionCode;
    }


    @Override
    public String toString() {
        return new StringBuilder("ResponseToFcmData:")
                .append("\nresponseType: ")
                .append(AppUtils.responseTypeToString(responseType))
                .append("\nthisFcmToken: ")
                .append(thisFcmToken)
                .append("\nthisDatabaseId: ")
                .append(AppUtils.responseTypeToString(thisDatabaseId))
                .append("\nandroidID: ")
                .append(thisAndroidId == null ? "null" : thisAndroidId)
                .append("\nmessage: ")
                .append(message == null ? "null" : message)
                .append("\nbattery %: ")
                .append(batteryPercentages).append("%")
                .append("\nbattery status: ")
                .append(batteryPlugged == 0 ? "NENABÍJÍ SE" : "NANABÍJÍ SE")
                .toString();
    }
}
