package com.example.sc2006_canpark_clientapp.Backend;

import com.google.android.libraries.places.api.model.Place;

import java.io.Serializable;

public class UserSelectPersistence implements Serializable {

    private String PlaceId;
    private double dest_longitude;
    private double dest_latitude;
    private String dest_name;
    private Carpark SelectedCarpark;

    public String getPlaceId(){
        return PlaceId;
    }

    public void setPlaceId(String placeId){
        this.PlaceId = placeId;
    }

    public double getDest_latitude() {
        return dest_latitude;
    }

    public void setDest_latitude(double dest_latitude) {
        this.dest_latitude = dest_latitude;
    }

    public double getDest_longitude() {
        return dest_longitude;
    }

    public void setDest_longitude(double dest_longitude) {
        this.dest_longitude = dest_longitude;
    }

    public String getDest_name() {
        return dest_name;
    }

    public void setDest_name(String dest_name) {
        this.dest_name = dest_name;
    }

    public Carpark getSelectedCarpark() {
        return SelectedCarpark;
    }

    public void setSelectedCarpark(Carpark selectedCarpark) {
        SelectedCarpark = selectedCarpark;
    }
}
