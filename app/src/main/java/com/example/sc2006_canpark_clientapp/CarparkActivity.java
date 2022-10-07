package com.example.sc2006_canpark_clientapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarparkActivity extends AppCompatActivity implements TabLayoutMediator.TabConfigurationStrategy{
    private ViewPager2 viewPager2;
    private ProgressBar pBCarpark;
    private UserSelectPersistence usp;
    private PlacesClient placesClient = null;
    private CarparkAdapter adapter;
    private CanparkBackendAPI api = new CanparkBackendAPI();
    private ArrayList<Carpark> Carparks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpark);
        TabLayout tabLayout = findViewById(R.id.TLCarpark);
        this.viewPager2 = findViewById(R.id.VPCarpark);
        this.pBCarpark = findViewById(R.id.pBCarpark);

        this.usp = (UserSelectPersistence)getIntent().getSerializableExtra("USER_SELECT");
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        this.placesClient = Places.createClient(this);

        this.adapter = new CarparkAdapter(this);
        viewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager2,this).attach();
        viewPager2.setVisibility(View.INVISIBLE);
        if (this.usp.getDest_latitude() == 0)
            this.placesClient.fetchPlace(FetchPlaceRequest
                    .builder(
                    this.usp.getPlaceId(), Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
                    .build()).addOnSuccessListener((response) -> {
                LatLng loc = response.getPlace().getLatLng();
               usp.setDest_latitude(loc.latitude);
               usp.setDest_longitude(loc.longitude);
               GetCarparks();
               MapViewFragment frag = (MapViewFragment)adapter.GetItem(0);
               frag.UpdateMapLoc();
            });

    }

    public UserSelectPersistence GetUSP()
    {
        return this.usp;
    }

    private void GetCarparks()
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(new StringRequest(Request.Method.GET,  String.format(CanparkBackendAPI.BaseURL + "GetCarparks?long=%1$s&lat=%2$s", this.usp.getDest_longitude(), this.usp.getDest_latitude())
                ,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Type listType = new TypeToken<ArrayList<Carpark>>(){}.getType();
                Carparks = new Gson().fromJson(response, listType);

                MapViewFragment frag = (MapViewFragment)adapter.GetItem(0);
                frag.UpdateCarparkMarkers(Carparks);

                ListViewFragment frag2 = (ListViewFragment)adapter.GetItem(1);
                frag2.SetCarparks(Carparks);

                Log.d("myTag", response);
                viewPager2.setVisibility(View.VISIBLE);
                pBCarpark.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // enjoy your error status
                Log.d("myTag", error.getMessage());
            }
        }));

    }

    @Override
    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
        switch (position){
            case 0:
                tab.setText("Map");
                break;
            case 1:
                tab.setText("List");
                break;
        }
    }
}