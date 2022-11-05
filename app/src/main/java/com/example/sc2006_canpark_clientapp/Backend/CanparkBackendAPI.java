package com.example.sc2006_canpark_clientapp.Backend;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

    public boolean SortCarparks(CarparkSortType type)
    {
        if (this.Carparklist == null)
            return false;
        switch (type){
            case SORT_TYPE_Distance:
                Collections.sort(this.Carparklist, new Comparator<Carpark>(){
                    @Override
                    public int compare(Carpark l, Carpark r) {
                        if (l.getDist() < r.getDist())
                            return -1;
                        else if (l.getDist() == r.getDist())
                            return 0;
                        else
                            return 1;
                    }
                });
                break;
            case SORT_TYPE_Availability:
                Collections.sort(this.Carparklist, new Comparator<Carpark>(){

                    @Override
                    public int compare(Carpark l, Carpark r) {
                        double a1 = (double)l.getLots_available() / (double)l.getTotal_lots();
                        double a2 = (double)r.getLots_available() / (double)r.getTotal_lots();
                        if (a1 > a2)
                            return -1;
                        else if (a1 == a2)
                            return 0;
                        else
                            return 1;
                    }
                });
                break;
        }
        return true;
    }

    public void PostViewCarpark(String UUID, Carpark c)
    {
        RequestQueue queue = Volley.newRequestQueue(this.context);
        JsonObjectRequest sr = null;
        try {
            sr = new JsonObjectRequest(Request.Method.POST,  BaseURL + "ViewCarpark", new JSONObject("{\"UUID\":\"" + UUID + "\", \"car_park_no\":\"" + c.getCar_park_no() + "\"}")
                    ,new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //Log.d("myTag", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(context,
                                "Timeout from server",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof AuthFailureError) {
                        //TODO
                    } else if (error instanceof ServerError) {
                        //TODO
                    } else if (error instanceof NetworkError) {
                        //TODO
                    } else if (error instanceof ParseError) {
                        //TODO
                        Log.d("myTag", "Error parsing");
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        queue.add(sr);
    }

    public Carpark GetCarpark(String carpark_no)
    {
        for(Carpark c : this.Carparklist){
            if (c.getCar_park_no().equals(carpark_no))
                return c;
        }
        return null;
    }

    public void GetCarparks(double longitude, double latitude, OnResultListener listener)
    {
        RequestQueue queue = Volley.newRequestQueue(this.context);
        StringRequest sr = new StringRequest(Request.Method.GET,  String.format(BaseURL + "GetCarparks?long=%1$s&lat=%2$s",longitude, latitude)
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
                        try {
                            return LocalDateTime.parse(time,ISO_OFFSET_DATE_TIME);
                        }catch (DateTimeParseException e){
                            return LocalDateTime.MIN;
                        }
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
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(context,
                            "Timeout from server",
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    //TODO
                } else if (error instanceof ServerError) {
                    //TODO
                } else if (error instanceof NetworkError) {
                    //TODO
                } else if (error instanceof ParseError) {
                    //TODO
                    Log.d("myTag", "Error parsing");
                }

            }
        });
        sr.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }
}
