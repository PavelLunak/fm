package com.example.fm.objects;

public class ResultForRequestActualLocation {

    private NewLocation actualLocation;
    private String message;


    public ResultForRequestActualLocation(NewLocation actualLocation, String message) {
        this.actualLocation = actualLocation;
        this.message = message;
    }


    public NewLocation getActualLocation() {
        return actualLocation;
    }

    public void setActualLocation(NewLocation actualLocation) {
        this.actualLocation = actualLocation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
