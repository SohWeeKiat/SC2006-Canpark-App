package com.example.sc2006_canpark_clientapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sc2006_canpark_clientapp.Backend.Carpark;
import com.example.sc2006_canpark_clientapp.BuildConfig;
import com.example.sc2006_canpark_clientapp.Backend.CanparkBackendAPI;
import com.example.sc2006_canpark_clientapp.Adapters.CarparkAdapter;
import com.example.sc2006_canpark_clientapp.Charts.DayAxisValueFormatter;
import com.example.sc2006_canpark_clientapp.Charts.Fill;
import com.example.sc2006_canpark_clientapp.R;
import com.example.sc2006_canpark_clientapp.Backend.UserSelectPersistence;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarparkActivity extends AppCompatActivity implements TabLayoutMediator.TabConfigurationStrategy{
    private ViewPager2 viewPager2;
    private ProgressBar pBCarpark;
    private TextView TVDestination;

    private LineChart chart;
    private BarChart barChart;

    private BottomSheetBehavior behavior;
    private UserSelectPersistence usp;
    private PlacesClient placesClient = null;
    private CarparkAdapter adapter;
    private final CanparkBackendAPI api = new CanparkBackendAPI(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpark);

        this.behavior = BottomSheetBehavior.from(findViewById(R.id.sheet));
        this.behavior.setPeekHeight(0);
        this.behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        this.chart = findViewById(R.id.chart1);
        this.barChart = findViewById(R.id.chart2);
        this.SetupChart();
        this.SetUpBarChart();

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
        viewPager2.setVisibility(View.INVISIBLE);
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

    private void SetupChart()
    {
        XAxis xAxis;
        {
            xAxis = chart.getXAxis();
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();
            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(false);
            yAxis.setAxisMinimum(0f);
        }
        ArrayList<Entry> values = new ArrayList<>();
        LineDataSet set1 = new LineDataSet(values, "DataSet 1");

        set1.enableDashedLine(10f, 5f, 0f);

        // black lines and points
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);

        // line thickness and point size
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);

        // draw points as solid circles
        set1.setDrawCircleHole(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        chart.setData(new LineData(dataSets));
    }

    private void SetUpBarChart()
    {
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new DayAxisValueFormatter());

        ArrayList<BarEntry> values = new ArrayList<>();
        BarDataSet set1 = new BarDataSet(values, "The year 2017");
        set1.setDrawIcons(false);

        int startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
        int startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light);
        int startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
        int startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light);
        int startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light);
        int endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
        int endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple);
        int endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark);
        int endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        int endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark);

        List<Fill> gradientFills = new ArrayList<>();
        gradientFills.add(new Fill(startColor1, endColor1));
        gradientFills.add(new Fill(startColor2, endColor2));
        gradientFills.add(new Fill(startColor3, endColor3));
        gradientFills.add(new Fill(startColor4, endColor4));
        gradientFills.add(new Fill(startColor5, endColor5));

        //set1.setf(gradientFills);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);
        barChart.setData(data);
    }

    public void OnSelection(int index)
    {
        Carpark c = this.api.getCarparklist().get(index);
        this.usp.setSelectedCarpark(c);
        this.behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        if (c.getHistory() == null)
            return;
        if (barChart.getData() != null &&
                barChart.getData().getDataSetCount() > 0) {
            BarDataSet set1 = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            set1.setValues(c.getHistory().GenerateBarEntryList(0));
            set1.notifyDataSetChanged();
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        }
        /*if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            LineDataSet set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);

            set1.setValues(c.getHistory().GenerateEntryList(0));
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        }*/
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

    public void OnbRouteClick(View v)
    {
        Intent c = new Intent(getApplicationContext(), RouteActivity.class);
        c.putExtra(getResources().getString(R.string.user_config), this.usp);
        startActivity(c, null);
    }
}