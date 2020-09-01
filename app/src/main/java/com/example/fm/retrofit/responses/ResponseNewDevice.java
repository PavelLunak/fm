package com.example.fm.retrofit.responses;

import com.google.gson.annotations.SerializedName;

public class ResponseNewDevice {

    @SerializedName("new_device_id")
    private int newDeviceId;

    @SerializedName("message")
    private String message;

    public ResponseNewDevice() {}

    public ResponseNewDevice(int newDeviceId, String message) {
        this.newDeviceId = newDeviceId;
        this.message = message;
    }

    public int getNewDeviceId() {
        return newDeviceId;
    }

    public void setNewDeviceId(int newDeviceId) {
        this.newDeviceId = newDeviceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
