package com.example.sc2006_canpark_clientapp.Backend;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Carpark implements Serializable {
    private String car_park_no;
    private String address;
    private double longitude;
    private double latitude;
    private int total_lots;
    private int lots_available;
    private LocalDateTime update_datetime;
    private String free_parking;
    private String night_parking;
    private int viewing_now;
    private double dist;
    private HistoryData history;

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

    public int getLots_available() { return lots_available; }

    public LocalDateTime getUpdate_datetime() {
        return update_datetime;
    }

    public String getFree_parking() {
        return free_parking;
    }

    public String getNight_parking() {
        return night_parking;
    }

    public int getViewing_now() {
        return viewing_now;
    }

    public double getDist() {
        return dist;
    }

    public HistoryData getHistory() {
        return history;
    }
}
