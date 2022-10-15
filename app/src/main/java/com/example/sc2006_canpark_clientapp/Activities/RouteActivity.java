package com.example.sc2006_canpark_clientapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.sc2006_canpark_clientapp.Backend.Carpark;
import com.example.sc2006_canpark_clientapp.Backend.UserSelectPersistence;
import com.example.sc2006_canpark_clientapp.BuildConfig;
import com.example.sc2006_canpark_clientapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    int LOCATION_REFRESH_TIME = 15000; // 15 seconds to update
    int LOCATION_REFRESH_DISTANCE = 500; // 500 meters to update
    private static final int GPS_PERMISSION_CODE = 100;

    private UserSelectPersistence usp;
    private LocationManager mLocationManager;
    private GeoApiContext context;
    private Location location;

    private MapView mapView;
    private GoogleMap map;
    private Spinner cmBRouteSelection;
    private ArrayAdapter<CharSequence> adapter;
    private ProgressBar pBRoute;
    private Button bNavigate;

    private DirectionsResult DirResult;
    private int overviewRouteIndex;
    private Polyline MapLineOverlay;
    private Marker StartMarker;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location loc) {
            location = loc;
            RequestDirections();
        }
    };

    private final AdapterView.OnItemSelectedListener OnRouteSelectionListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            overviewRouteIndex = i;
            DrawDirectionOnMap();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private final View.OnClickListener OnNavClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + usp.getSelectedCarpark().getLatitude() +
                    "," + usp.getSelectedCarpark().getLongitude() + "&mode=d");//driving mode
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        this.mapView = findViewById(R.id.MVRoute);
        this.mapView.onCreate(savedInstanceState);
        this.mapView.onResume();
        this.mapView.getMapAsync(this);
        this.cmBRouteSelection = findViewById(R.id.cmBRouteSelection);
        this.cmBRouteSelection.setEnabled(false);
        this.pBRoute = findViewById(R.id.pBRoute);
        this.bNavigate = findViewById(R.id.bNavigate);
        this.bNavigate.setOnClickListener(this.OnNavClickListener);
        this.usp = (UserSelectPersistence) getIntent().getSerializableExtra("USER_SELECT");

        this.adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item);
        this.cmBRouteSelection.setAdapter(this.adapter);
        this.cmBRouteSelection.setOnItemSelectedListener(OnRouteSelectionListener);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, GPS_PERMISSION_CODE);
            return;
        }
        this.context = new GeoApiContext.Builder()
                .apiKey(BuildConfig.MAPS_API_KEY)
                .build();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) LOCATION_REFRESH_TIME,
                (float) LOCATION_REFRESH_DISTANCE, mLocationListener);

        Location location = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(new Criteria(), true));
        if (location != null) {
            mLocationListener.onLocationChanged(location);
        }
    }

    void RequestDirections()
    {
        DirectionsApiRequest apiRequest = DirectionsApi.newRequest(context);
        apiRequest.origin(new com.google.maps.model.LatLng(location.getLatitude(), location.getLongitude()));

        Carpark c = this.usp.getSelectedCarpark();
        apiRequest.destination(new com.google.maps.model.LatLng(c.getLatitude(), c.getLongitude()));
        apiRequest.mode(TravelMode.DRIVING); //set travelling mode
        apiRequest.alternatives(true);
        apiRequest.setCallback(new com.google.maps.PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                DirResult = result;
                overviewRouteIndex = 0;
                DrawDirectionOnMapOnMainThread();
            }

            @Override
            public void onFailure(Throwable e) {
                Log.d("MyTag", e.toString());
                //Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void DrawDirectionOnMapOnMainThread()
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pBRoute.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);

                adapter.clear();
                for(DirectionsRoute r : DirResult.routes){
                    adapter.add(r.summary);
                }
                cmBRouteSelection.setEnabled(true);
                DrawDirectionOnMap();
            }
        });
    }

    private void DrawDirectionOnMap()
    {
        addPolyline();
        positionCamera();
        addMarkersToMap();
    }

    private void addMarkersToMap()
    {
        if (this.StartMarker != null)
            return;
        this.StartMarker = this.map.addMarker(
                new MarkerOptions()
                        .position(
                                new LatLng(this.DirResult.routes[overviewRouteIndex].legs[0].startLocation.lat,
                                        this.DirResult.routes[overviewRouteIndex].legs[0].startLocation.lng))
                        .title(this.DirResult.routes[overviewRouteIndex].legs[0].startAddress));
        this.map.addMarker(new MarkerOptions().position(new LatLng(this.DirResult.routes[overviewRouteIndex].legs[0].endLocation.lat,
                        this.DirResult.routes[overviewRouteIndex].legs[0].endLocation.lng))
                .title(this.DirResult.routes[overviewRouteIndex].legs[0].endAddress).snippet("TEST"));
    }

    private void positionCamera()
    {
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(this.DirResult.routes[overviewRouteIndex].legs[0].startLocation.lat,
                this.DirResult.routes[overviewRouteIndex].legs[0].startLocation.lng), 16));
    }

    private void addPolyline()
    {
        if (this.MapLineOverlay != null)
            this.MapLineOverlay.remove();
        List<LatLng> decodedPath = PolyUtil.decode(this.DirResult.routes[overviewRouteIndex].overviewPolyline.getEncodedPath());
        this.MapLineOverlay = this.map.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setMyLocationEnabled(true);
        LatLng dest_loc = new LatLng(this.usp.getDest_latitude(), this.usp.getDest_longitude());
        this. map.animateCamera(CameraUpdateFactory.newLatLngZoom(dest_loc,16));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(dest_loc)      // Sets the center of the map to location user
                .zoom(16)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        this.map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        this.map.addMarker(new MarkerOptions()
                .title("Target")
                .position(dest_loc)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
        );
    }
}