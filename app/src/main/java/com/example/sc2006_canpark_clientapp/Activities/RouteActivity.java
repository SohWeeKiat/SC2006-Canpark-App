package com.example.sc2006_canpark_clientapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.sc2006_canpark_clientapp.Backend.Carpark;
import com.example.sc2006_canpark_clientapp.Backend.UserSelectPersistence;
import com.example.sc2006_canpark_clientapp.BuildConfig;
import com.example.sc2006_canpark_clientapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
    private UserSelectPersistence usp;
    private LocationManager mLocationManager;
    private GeoApiContext context;
    private Location location;
    private MapView mapView;
    private GoogleMap map;
    private DirectionsResult Dirresult;
    private int overviewRouteIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        this.mapView = findViewById(R.id.MVRoute);
        this.mapView.getMapAsync(this);
        this.usp = (UserSelectPersistence) getIntent().getSerializableExtra("USER_SELECT");

        this.context = new GeoApiContext.Builder()
                .apiKey(BuildConfig.MAPS_API_KEY)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) LOCATION_REFRESH_TIME,
                (float) LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    void RequestDirections()
    {
        DirectionsApiRequest apiRequest = DirectionsApi.newRequest(context);
        apiRequest.origin(new com.google.maps.model.LatLng(location.getLatitude(), location.getLongitude()));

        Carpark c = this.usp.getSelectedCarpark();
        apiRequest.destination(new com.google.maps.model.LatLng(c.getLatitude(), c.getLatitude()));
        apiRequest.mode(TravelMode.DRIVING); //set travelling mode
        apiRequest.setCallback(new com.google.maps.PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Dirresult = result;
                DirectionsRoute[] routes = result.routes;
                overviewRouteIndex = 0;
                addPolyline(Dirresult);
                positionCamera(Dirresult.routes[overviewRouteIndex]);
                addMarkersToMap(Dirresult);
            }

            @Override
            public void onFailure(Throwable e) {
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location loc) {
            location = loc;
            RequestDirections();
        }
    };

    private void addMarkersToMap(DirectionsResult results) {
        this.map.addMarker(
                new MarkerOptions()
                        .position(
                        new LatLng(results.routes[overviewRouteIndex].legs[overviewRouteIndex].startLocation.lat,
                                results.routes[overviewRouteIndex].legs[overviewRouteIndex].startLocation.lng))
                        .title(results.routes[overviewRouteIndex].legs[overviewRouteIndex].startAddress));
        this.map.addMarker(new MarkerOptions().position(new LatLng(results.routes[overviewRouteIndex].legs[overviewRouteIndex].endLocation.lat,
                results.routes[overviewRouteIndex].legs[overviewRouteIndex].endLocation.lng))
                .title(results.routes[overviewRouteIndex].legs[overviewRouteIndex].endAddress).snippet("TEST"));
    }

    private void positionCamera(DirectionsRoute route) {
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overviewRouteIndex].startLocation.lat, route.legs[overviewRouteIndex].startLocation.lng), 12));
    }

    private void addPolyline(DirectionsResult results) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overviewRouteIndex].overviewPolyline.getEncodedPath());
        this.map.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
    }
}