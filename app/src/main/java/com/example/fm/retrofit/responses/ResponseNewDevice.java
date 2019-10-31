package com.example.fm.retrofit.responses;

import com.google.gson.annotations.SerializedName;

public class ResponseNewDevice {

    @SerializedName("new_device_id")
    private int newDeviceName;

    @SerializedName("message")
    private String message;

    public ResponseNewDevice() {}

    public ResponseNewDevice(int newDeviceName, String message) {
        this.newDeviceName = newDeviceName;
        this.message = message;
    }

    public int getNew_position_id() {
        return newDeviceName;
    }

    public void setNew_position_id(int new_position_id) {
        this.newDeviceName = new_position_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
