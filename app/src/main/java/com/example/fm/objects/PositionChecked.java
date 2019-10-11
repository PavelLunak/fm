package com.example.fm.objects;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;


public class PositionChecked implements Parcelable {

    int id;
    String name;
    private long date;
    private double latitude;
    private double longitude;
    private float speed;
    private double accuracy;
    private boolean isChecked;

    //data z objektu Address
    private String countryCode;
    private String countryName;
    private String featureName;
    private String locality;
    private String phone;
    private String postalCode;
    private String premises;
    private String adminArea;
    private String subAdminArea;
    private String subLocality;
    private String thoroughfare;
    private String subThoroughfare;
    private String url;
    private Bundle extras;

    private int status;


    public PositionChecked() {}

    public PositionChecked(long date, double latitude, double longitude, float speed, double accuracy) {
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPremises() {
        return premises;
    }

    public void setPremises(String premises) {
        this.premises = premises;
    }

    public String getAdminArea() {
        return adminArea;
    }

    public void setAdminArea(String adminArea) {
        this.adminArea = adminArea;
    }

    public String getSubAdminArea() {
        return subAdminArea;
    }

    public void setSubAdminArea(String subAdminArea) {
        this.subAdminArea = subAdminArea;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public void setSubLocality(String subLocality) {
        this.subLocality = subLocality;
    }

    public String getThoroughfare() {
        return thoroughfare;
    }

    public void setThoroughfare(String thoroughfare) {
        this.thoroughfare = thoroughfare;
    }

    public String getSubThoroughfare() {
        return subThoroughfare;
    }

    public void setSubThoroughfare(String subThoroughfare) {
        this.subThoroughfare = subThoroughfare;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bundle getExtras() {
        return extras;
    }

    public void setExtras(Bundle extras) {
        this.extras = extras;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeLong(this.date);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeFloat(this.speed);
        dest.writeDouble(this.accuracy);
        dest.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
        dest.writeString(this.countryCode);
        dest.writeString(this.countryName);
        dest.writeString(this.featureName);
        dest.writeString(this.locality);
        dest.writeString(this.phone);
        dest.writeString(this.postalCode);
        dest.writeString(this.premises);
        dest.writeString(this.adminArea);
        dest.writeString(this.subAdminArea);
        dest.writeString(this.subLocality);
        dest.writeString(this.thoroughfare);
        dest.writeString(this.subThoroughfare);
        dest.writeString(this.url);
        dest.writeBundle(this.extras);
        dest.writeInt(this.status);
    }

    protected PositionChecked(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.date = in.readLong();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.speed = in.readFloat();
        this.accuracy = in.readDouble();
        this.isChecked = in.readByte() != 0;
        this.countryCode = in.readString();
        this.countryName = in.readString();
        this.featureName = in.readString();
        this.locality = in.readString();
        this.phone = in.readString();
        this.postalCode = in.readString();
        this.premises = in.readString();
        this.adminArea = in.readString();
        this.subAdminArea = in.readString();
        this.subLocality = in.readString();
        this.thoroughfare = in.readString();
        this.subThoroughfare = in.readString();
        this.url = in.readString();
        this.extras = in.readBundle();
        this.status = in.readInt();
    }

    public static final Creator<PositionChecked> CREATOR = new Creator<PositionChecked>() {
        @Override
        public PositionChecked createFromParcel(Parcel source) {
            return new PositionChecked(source);
        }

        @Override
        public PositionChecked[] newArray(int size) {
            return new PositionChecked[size];
        }
    };
}
