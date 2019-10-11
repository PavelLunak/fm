package com.example.fm.retrofit.requests;

import com.example.fm.objects.PositionChecked;

import java.util.ArrayList;
import java.util.List;

public class RequestSendPositionsToDB {

    private List<PositionChecked> items;
    private int fm; //Příznak pto databázi


    public RequestSendPositionsToDB() {
        this.fm = 1;
    }

    public RequestSendPositionsToDB(List<PositionChecked> items) {
        this.items = items;
        this.fm = 1;
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

    public int getFm() {
        return fm;
    }

    public void setFm(int fm) {
        this.fm = fm;
    }
}
