package com.example.sc2006_canpark_clientapp;

import static androidx.core.content.ContextCompat.startActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private PlacesClient placesClient = null;
    private EditText tBSearchLocation;
    private RecyclerView lVLocationSearchResult;
    private SearchLocationResultAdapter ResultAdapter;
    private ProgressBar pBSearchResult;
    private TextView tVEmpty;
    CancellationTokenSource cts;
    Geocoder GeoQuery = null;
    private List<AutocompletePrediction> predictions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        this.placesClient = Places.createClient(this);
        this.tBSearchLocation = findViewById(R.id.tBSearchLocation);
        this.lVLocationSearchResult = findViewById(R.id.lVLocationSearchResult);
        this.pBSearchResult = findViewById(R.id.pBSearchResult);
        this.tVEmpty = findViewById(R.id.tVEmpty);
        this.pBSearchResult.setVisibility(View.GONE);
        this.GeoQuery = new Geocoder(getApplicationContext(), Locale.getDefault());
        this.tBSearchLocation.addTextChangedListener(SearchLocationWatcher);
        this.ResultAdapter = new SearchLocationResultAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position < 0 || position > predictions.size())
                    return;
                UserSelectPersistence usp = new UserSelectPersistence();
                usp.setPlaceId(predictions.get(position).getPlaceId());
                Intent c = new Intent(view.getContext(), CarparkActivity.class);
                c.putExtra("USER_SELECT", usp);
                startActivity(c, null);
                //Toast.makeText(view.getContext(), "position = " + getLayoutPosition(), Toast.LENGTH_SHORT).show();
            }
        });
        this.lVLocationSearchResult.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        this.lVLocationSearchResult.setAdapter(this.ResultAdapter);
    }

    private TextWatcher SearchLocationWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (cts != null){
                cts.cancel();
            }
            cts = new CancellationTokenSource();
            ResultAdapter.SetResult(new ArrayList<>());
            ResultAdapter.notifyDataSetChanged();
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setCountry("SG")
                    .setQuery(tBSearchLocation.getText().toString())
                    .setCancellationToken(cts.getToken())
                    .build();
            pBSearchResult.setVisibility(View.VISIBLE);
            tVEmpty.setVisibility(View.GONE);
            placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                Log.i("myTag","GOT RESULT " + response.getAutocompletePredictions().size());
                predictions = response.getAutocompletePredictions();
                tVEmpty.setVisibility(predictions.size() <= 0 ? View.VISIBLE : View.GONE);
                ResultAdapter.SetResult(predictions);
                ResultAdapter.notifyDataSetChanged();
                pBSearchResult.setVisibility(View.GONE);
            }).addOnFailureListener((Exception exception) ->{
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e("myTag", "Place not found: " + apiException.getStatusCode());
                }
                pBSearchResult.setVisibility(View.GONE);
            });
        }
    };
}