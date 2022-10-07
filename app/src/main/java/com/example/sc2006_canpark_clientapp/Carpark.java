package com.example.sc2006_canpark_clientapp;

import java.time.LocalDateTime;

public class Carpark {
    private String car_park_no;
    private String address;
    private double longitude;
    private double latitude;
    private int total_lots;
    private int lots_available;
    private String update_datetime;
    private double dist;

    public String getCar_park_no() {
        return car_park_no;
    }

    public String getAddress() {
        return address;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getTotal_lots() {
        return total_lots;
    }

    public int getLots_available() {
        return lots_available;
    }

    public String getUpdate_datetime() {
        return update_datetime;
    }

    public double getDist() {
        return dist;
    }
}
