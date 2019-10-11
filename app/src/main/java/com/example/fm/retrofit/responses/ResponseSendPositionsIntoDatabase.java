package com.example.fm.retrofit.responses;

import java.util.List;

public class ResponseSendPositionsIntoDatabase {

    private List<Integer> saved_positions_ids;
    private List<String> messages;


    public ResponseSendPositionsIntoDatabase() {}

    public List<Integer> getSaved_positions_ids() {
        return saved_positions_ids;
    }

    public void setSaved_positions_ids(List<Integer> saved_positions_ids) {
        this.saved_positions_ids = saved_positions_ids;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
