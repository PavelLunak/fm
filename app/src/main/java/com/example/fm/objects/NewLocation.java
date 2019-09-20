package com.example.fm.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class NewLocation implements Parcelable {

    long date;
    double latitude;
    double longitude;
    float accuracy;


    public NewLocation() {}

    public NewLocation(long date, double latitude, double longitude, float accuracy) {
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.date);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeFloat(this.accuracy);
    }

    protected NewLocation(Parcel in) {
        this.date = in.readLong();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.accuracy = in.readFloat();
    }

    public static final Creator<NewLocation> CREATOR = new Creator<NewLocation>() {
        @Override
        public NewLocation createFromParcel(Parcel source) {
            return new NewLocation(source);
        }

        @Override
        public NewLocation[] newArray(int size) {
            return new NewLocation[size];
        }
    };
}
