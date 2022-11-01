package com.example.sc2006_canpark_clientapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sc2006_canpark_clientapp.BuildConfig;
import com.example.sc2006_canpark_clientapp.Utils.OnItemClickListener;
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

    private Scene welcome_scene, search_scene;
    private Transition transition;
    private List<AutocompletePrediction> predictions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        this.placesClient = Places.createClient(this);

        SearchView sv = findViewById(R.id.welcomeSearchView);
        transition = TransitionInflater.from(this).inflateTransition(R.transition.welcome_to_search_transition);
        sv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitToSearchScene();
            }
        });
        FrameLayout fl = findViewById(R.id.sceneRootFrameLayout);
        welcome_scene =  Scene.getSceneForLayout(fl, R.layout.welcome_scene, this);
        search_scene =  Scene.getSceneForLayout(fl, R.layout.search_scene, this);
    }

    private void TransitToSearchScene()
    {
        TransitionManager.go(search_scene, transition);
        this.tBSearchLocation = findViewById(R.id.tBSearchLocation);
        this.lVLocationSearchResult = findViewById(R.id.lVLocationSearchResult);
        this.pBSearchResult = findViewById(R.id.pBSearchResult);
        this.tVEmpty = findViewById(R.id.tVEmpty);

        this.pBSearchResult.setVisibility(View.GONE);
        this.tBSearchLocation.addTextChangedListener(SearchLocationWatcher);
        //this.tBSearchLocation.requestFocus();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                tBSearchLocation.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0f, 0f, 0));
                tBSearchLocation.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0f, 0f, 0));
            }
        }, 200);
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
                    Toast.makeText(getApplicationContext(), "Failed to query google places", Toast.LENGTH_SHORT).show();
                    ApiException apiException = (ApiException) exception;
                    Log.e("myTag", "Place not found: " + apiException.getStatusCode());
                }
                tVEmpty.setVisibility(View.VISIBLE);
                pBSearchResult.setVisibility(View.GONE);
            });
        }
    };
}