package com.min.nisal.weathertracker;

import java.io.Serializable;

class DateObject implements Serializable {

    private String location;
    private Long date;
    private int ID;

    DateObject(String location, Long date, int ID){
        this.date = date;
        this.location = location;
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public Long getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
