package com.example.fm.retrofit.requests;

import com.example.fm.objects.PositionChecked;

import java.util.ArrayList;
import java.util.List;

public class RequestSendPositionsToDB {

    private List<PositionChecked> items;


    public RequestSendPositionsToDB() {}

    public RequestSendPositionsToDB(List<PositionChecked> items) {
        this.items = items;
    }


    public void addItem(PositionChecked item) {
        if (items == null) {
            items = new ArrayList<>();
        }

        items.add(item);
    }


    public List<PositionChecked> getItems() {
        return items;
    }

    public void setItems(List<PositionChecked> items) {
        this.items = items;
    }
}
