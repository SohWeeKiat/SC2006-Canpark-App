package com.example.sc2006_canpark_clientapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.sc2006_canpark_clientapp.BuildConfig;
import com.example.sc2006_canpark_clientapp.OnItemClickListener;
import com.example.sc2006_canpark_clientapp.R;
import com.example.sc2006_canpark_clientapp.Adapters.SearchLocationResultAdapter;
import com.example.sc2006_canpark_clientapp.Backend.UserSelectPersistence;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
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
        this.tBSearchLocation.addTextChangedListener(SearchLocationWatcher);

        this.GeoQuery = new Geocoder(getApplicationContext(), Locale.getDefault());
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