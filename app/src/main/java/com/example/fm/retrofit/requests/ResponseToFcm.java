package com.example.fm.retrofit.requests;

import com.example.fm.retrofit.objects.ResponseToFcmData;

public class ResponseToFcm {

    private String to;
    private String collapse_key;
    private String priority;
    private int time_to_live;
    private ResponseToFcmData data;


    public ResponseToFcm() {}

    public ResponseToFcm(String to, ResponseToFcmData data) {
        this.to = to;
        this.collapse_key = "type_a";
        this.priority = "high";
        this.time_to_live = 10;
        this.data = data;
    }


    @Override
    public String toString() {
        return new StringBuilder("ResponseToFcm:")
                .append("\nto: ")
                .append(to == null ? "null" : to)
                .append("\ndata: ")
                .append(data == null ? "null" : data.toString())
                .toString();
    }
}
