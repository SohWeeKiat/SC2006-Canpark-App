package com.example.sc2006_canpark_clientapp;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class CanparkBackendAPI {
    public static final String BaseURL = "https://canpark.sohkiat.xyz/API/";

    public CanparkBackendAPI()
    {
    }

    public StringRequest GetCarparks(double longitude, double latitude)
    {
        return new StringRequest(Request.Method.GET,  String.format(BaseURL + "GetCarparks?long=%1&lat=%2",longitude, latitude)
                ,null, null);
    }
}
