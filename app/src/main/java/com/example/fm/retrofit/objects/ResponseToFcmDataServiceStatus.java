package com.example.fm.retrofit.objects;

public class ResponseToFcmDataServiceStatus extends ResponseToFcmData {

    private int serviceStatus;
    private int gpsStatus;


    public ResponseToFcmDataServiceStatus(
            int responseType,
            String message,
            String battery,
            int serviceStatus,
            int gpsStatus) {

        super(responseType, message, battery);
        this.serviceStatus = serviceStatus;
        this.gpsStatus = gpsStatus;
    }
}
