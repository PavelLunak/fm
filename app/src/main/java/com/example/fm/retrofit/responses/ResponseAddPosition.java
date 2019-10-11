package com.example.fm.retrofit.responses;

public class ResponseAddPosition {

    private int new_position_id;
    private String message;

    public ResponseAddPosition() {}

    public ResponseAddPosition(int new_position_id, String message) {
        this.new_position_id = new_position_id;
        this.message = message;
    }

    public int getNew_position_id() {
        return new_position_id;
    }

    public void setNew_position_id(int new_position_id) {
        this.new_position_id = new_position_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
