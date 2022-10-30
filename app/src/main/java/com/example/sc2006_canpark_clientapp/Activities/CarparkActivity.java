package com.example.sc2006_canpark_clientapp.Activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sc2006_canpark_clientapp.Backend.Carpark;
import com.example.sc2006_canpark_clientapp.Backend.CarparkSortType;
import com.example.sc2006_canpark_clientapp.BuildConfig;
import com.example.sc2006_canpark_clientapp.Backend.CanparkBackendAPI;
import com.example.sc2006_canpark_clientapp.Adapters.CarparkAdapter;
import com.example.sc2006_canpark_clientapp.R;
import com.example.sc2006_canpark_clientapp.Backend.UserSelectPersistence;
import com.example.sc2006_canpark_clientapp.Utils.Config;
import com.example.sc2006_canpark_clientapp.Utils.GpsReceiver;
import com.example.sc2006_canpark_clientapp.Utils.LocationCallBack;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class CarparkActivity extends AppCompatActivity implements TabLayoutMediator.TabConfigurationStrategy{
    private ViewPager2 viewPager2;
    private ProgressBar pBCarpark;
    private TextView TVDestination;

    private BottomSheetBehavior behavior;
    private TextView TVLotsStatus, TVPeopleViewingNow, TVFreeParking, TVNightParking, TVLastUpdated;
    private Spinner cmBDaySelection;
    private BarChart barChart;

    private UserSelectPersistence usp;
    private PlacesClient placesClient = null;
    private CarparkAdapter adapter;
    private final CanparkBackendAPI api = new CanparkBackendAPI(this);
    private GpsReceiver mLocationReceiver;
    private String AdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpark);

        this.behavior = BottomSheetBehavior.from(findViewById(R.id.sheet));
        this.behavior.setPeekHeight(0);
        this.behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        this.TVPeopleViewingNow = findViewById(R.id.tVPeopleViewing);
        this.TVFreeParking = findViewById(R.id.tVFreeParking);
        this.TVNightParking = findViewById(R.id.tVNightParking);
        this.TVLastUpdated = findViewById(R.id.tVLastUpdated);
        this.TVLotsStatus = findViewById(R.id.TVLotsStatus);
        this.cmBDaySelection = findViewById(R.id.cmBDaySelection);
        this.barChart = findViewById(R.id.chart2);
        this.SetUpBarChart();
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
        this.cmBDaySelection.setAdapter(dayAdapter);
        dayAdapter.add("Monday");
        dayAdapter.add("Tuesday");
        dayAdapter.add("Wednesday");
        dayAdapter.add("Thursday");
        dayAdapter.add("Friday");
        dayAdapter.add("Saturday");
        dayAdapter.add("Sunday");
        this.cmBDaySelection.setOnItemSelectedListener(OnDaySelectionListener);

        TabLayout tabLayout = findViewById(R.id.TLCarpark);
        this.viewPager2 = findViewById(R.id.VPCarpark);
        this.pBCarpark = findViewById(R.id.pBCarpark);
        this.TVDestination = findViewById(R.id.TVDestination);

        this.adapter = new CarparkAdapter(this);
        this.viewPager2.setAdapter(adapter);
        this.viewPager2.setUserInputEnabled(false);

        this.usp = (UserSelectPersistence)getIntent().getSerializableExtra(getResources().getString(R.string.user_config));
        if (this.usp == null){
            this.usp = (UserSelectPersistence) savedInstanceState.getSerializable(getResources().getString(R.string.user_config));
        }
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        this.placesClient = Places.createClient(this);

        new TabLayoutMediator(tabLayout, viewPager2,this).attach();
        this.viewPager2.setVisibility(View.INVISIBLE);
        this.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                super.onPageSelected(position);
            }
        });
        if (this.usp.getDest_latitude() == 0)
            this.placesClient.fetchPlace(FetchPlaceRequest
                    .builder(
                    this.usp.getPlaceId(), Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
                    .build()).addOnSuccessListener((response) -> {
               LatLng loc = response.getPlace().getLatLng();
               TVDestination.setText(response.getPlace().getName() + " - " + response.getPlace().getAddress());
               usp.setDest_latitude(loc.latitude);
               usp.setDest_longitude(loc.longitude);
               GetCarparks();
               MapViewFragment frag = (MapViewFragment)adapter.GetItem(0);
               frag.AddMarkerAndAnimateToLocation();
            });
        Button bRoute = (Button)findViewById(R.id.bRoute);
        bRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnbRouteClick(view);
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                else
                    finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
        mLocationReceiver = new GpsReceiver(new LocationCallBack() {
            @Override
            public void onLocationTriggered() {
                Toast.makeText(getApplicationContext(), "Location services is not switched on, please enable it", Toast.LENGTH_LONG).show();
                //Location state changed
            }
        });
        this.registerReceiver(mLocationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        new GetGAIDTask().execute(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mLocationReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(getResources().getString(R.string.user_config), this.usp);
        super.onSaveInstanceState(outState);
    }

    public UserSelectPersistence GetUSP()
    {
        return this.usp;
    }

    private void GetCarparks()
    {
        this.api.GetCarparks(this.usp.getDest_longitude(), this.usp.getDest_latitude(),
                new CanparkBackendAPI.OnResultListener() {
                    @Override
                    public void OnResult(boolean Success) {
                        if (Success){
                            MapViewFragment frag = (MapViewFragment)adapter.GetItem(0);
                            frag.UpdateCarparkMarkers(api.getCarparklist());

                            ListViewFragment frag2 = (ListViewFragment)adapter.GetItem(1);
                            frag2.SetCarparks(api.getCarparklist());

                            viewPager2.setVisibility(View.VISIBLE);
                            pBCarpark.setVisibility(View.GONE);
                        }else{
                            pBCarpark.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Failed to query webserver", Toast.LENGTH_LONG).show();
                        }
                    }
        });
    }

    private void SetUpBarChart()
    {
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);

        ArrayList<BarEntry> values = new ArrayList<>();
        BarDataSet set1 = new BarDataSet(values, "24 Hours history");
        set1.setDrawIcons(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);
        barChart.setData(data);
    }

    public void OnSelection(int index)
    {
        Log.d("myTag", "OnSelection: " + index);
        Carpark c = this.api.getCarparklist().get(index);
        this.usp.setSelectedCarpark(c);
        this.TVLotsStatus.setText(String.format("%d/%d\nLots\nAvailable", c.getLots_available(), c.getTotal_lots()));
        this.TVPeopleViewingNow.setText(String.format("%d",c.getViewing_now()));
        this.TVFreeParking.setText(c.getFree_parking().toLowerCase(Locale.ROOT));
        this.TVNightParking.setText(c.getNight_parking().toLowerCase(Locale.ROOT));
        this.TVLastUpdated.setText(c.getUpdate_datetime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        double percentage = (double) c.getLots_available() / c.getTotal_lots();
        Drawable bg = this.TVLotsStatus.getBackground();
        if (bg instanceof GradientDrawable){
            GradientDrawable gradientDrawable = (GradientDrawable)bg;
            if (percentage <= Config.LowerPercentage)
                gradientDrawable.setColor(Color.RED);
            else if (percentage < Config.HigherPercentage)
                gradientDrawable.setColor(Color.rgb(255, 153, 0));
            else
                gradientDrawable.setColor(Color.GREEN);
        }
        this.behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        this.SetGraphDay(0);
        this.api.PostViewCarpark(this.AdId, c);
    }

    private void SetGraphDay(int day)
    {
        this.cmBDaySelection.setSelection(day);
        Carpark c = this.usp.getSelectedCarpark();
        if (c == null) return;
        else if (c.getHistory() == null) return;
        if (this.barChart.getData() != null &&
                this.barChart.getData().getDataSetCount() > 0) {
            BarDataSet set1 = (BarDataSet) this.barChart.getData().getDataSetByIndex(0);
            set1.setValues(c.getHistory().GenerateBarEntryList(day));
            set1.notifyDataSetChanged();
            this.barChart.getData().notifyDataChanged();
            this.barChart.notifyDataSetChanged();
            this.barChart.invalidate();
        }
    }

    public void SortCarparks(CarparkSortType type)
    {
        this.behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (!this.api.SortCarparks(type))
            return;
        MapViewFragment frag = (MapViewFragment)adapter.GetItem(0);
        frag.UpdateCarparkMarkers(api.getCarparklist());

        ListViewFragment frag2 = (ListViewFragment)adapter.GetItem(1);
        frag2.SetCarparks(api.getCarparklist());
    }

    private final AdapterView.OnItemSelectedListener OnDaySelectionListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            SetGraphDay(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

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

    public void OnbRouteClick(View v)
    {
        Intent c = new Intent(getApplicationContext(), RouteActivity.class);
        c.putExtra(getResources().getString(R.string.user_config), this.usp);
        startActivity(c, null);
    }

    private class GetGAIDTask extends AsyncTask<Context, Integer, String> {
        @Override
        protected String doInBackground(Context... cts) {
            AdvertisingIdClient.Info adInfo;
            adInfo = null;
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(cts[0]);
                return adInfo.getId();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            AdId = s;
        }
    }
}