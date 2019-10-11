package com.example.fm.retrofit.objects;

public class ResponseToFcmDataSettings extends ResponseToFcmData {

    private int savingToDatabaseEnabled;        //0 = DISABLED, 1 = ENABLED
    private long autoCheckedPositionSavingInterval;
    private int maxCountOfLocationChecked;
    private int timeUnit;


    public ResponseToFcmDataSettings(
            int responseType,
            String message,
            String battery,
            int savingToDatabaseEnabled,
            long autoCheckedPositionSavingInterval,
            int maxCountOfLocationChecked,
            int timeUnit) {

        super(responseType, message, battery);
        this.savingToDatabaseEnabled = savingToDatabaseEnabled;
        this.autoCheckedPositionSavingInterval = autoCheckedPositionSavingInterval;
        this.maxCountOfLocationChecked = maxCountOfLocationChecked;
        this.timeUnit = timeUnit;
    }
}
