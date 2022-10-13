package com.example.sc2006_canpark_clientapp.Backend;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class CanparkBackendAPI {
    private static final String BaseURL = "https://canpark.sohkiat.xyz/API/";
    private final Context context;

    private ArrayList<Carpark> Carparklist;

    public interface OnResultListener{
        public void OnResult(boolean Success);
    }

    public CanparkBackendAPI(Context context)
    {
        this.context = context;
    }

    public ArrayList<Carpark> getCarparklist() {
        return Carparklist;
    }

    public void GetCarparks(double longitude, double latitude, OnResultListener listener)
    {
        RequestQueue queue = Volley.newRequestQueue(this.context);
        queue.add(new StringRequest(Request.Method.GET,  String.format(BaseURL + "GetCarparks?long=%1$s&lat=%2$s",longitude, latitude)
                ,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("myTag", response);
                Type listType = new TypeToken<ArrayList<Carpark>>(){}.getType();
                Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                        String time = json.getAsJsonPrimitive().getAsString();
                        if (time.isEmpty())
                            return LocalDateTime.MIN;
                        return LocalDateTime.parse(time);
                    }
                }).create();
                Carparklist = gson.fromJson(response, listType);
                listener.OnResult(true);
            }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // enjoy your error status
                listener.OnResult(false);
                Log.d("myTag", error.getMessage());
            }
        }));
    }
}
