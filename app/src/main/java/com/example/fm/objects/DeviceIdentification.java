package com.example.fm.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceIdentification implements Parcelable {

    private String androidId;
    private String deviceId;


    public DeviceIdentification() {}

    public DeviceIdentification(String androidId, String deviceId) {
        this.androidId = androidId;
        this.deviceId = deviceId;
    }


    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.androidId);
        dest.writeString(this.deviceId);
    }

    protected DeviceIdentification(Parcel in) {
        this.androidId = in.readString();
        this.deviceId = in.readString();
    }

    public static final Parcelable.Creator<DeviceIdentification> CREATOR = new Parcelable.Creator<DeviceIdentification>() {
        @Override
        public DeviceIdentification createFromParcel(Parcel source) {
            return new DeviceIdentification(source);
        }

        @Override
        public DeviceIdentification[] newArray(int size) {
            return new DeviceIdentification[size];
        }
    };
}
