package com.example.fm.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable {
    private int id;
    private String name;
    private String description;
    private String token;
    private DeviceIdentification deviceIdentification;


    public Device() {}

    public Device(int id, String name, String description, String token, DeviceIdentification deviceIdentification) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.token = token;
        this.deviceIdentification = deviceIdentification;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DeviceIdentification getDeviceIdentification() {
        return deviceIdentification;
    }

    public void setDi(DeviceIdentification deviceIdentification) {
        this.deviceIdentification = deviceIdentification;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.token);
        dest.writeParcelable(this.deviceIdentification, flags);
    }

    protected Device(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.description = in.readString();
        this.token = in.readString();
        this.deviceIdentification = in.readParcelable(DeviceIdentification.class.getClassLoader());
    }

    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel source) {
            return new Device(source);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
}
