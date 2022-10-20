package com.example.sc2006_canpark_clientapp.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sc2006_canpark_clientapp.Backend.Carpark;
import com.example.sc2006_canpark_clientapp.R;
import com.example.sc2006_canpark_clientapp.Backend.UserSelectPersistence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private static final int GPS_PERMISSION_CODE = 100;

    private MapView mapView;
    private GoogleMap map;
    private UserSelectPersistence usp;
    public MapViewFragment() {
        // Required empty public constructor
    }

    public static MapViewFragment newInstance() {
        MapViewFragment fragment = new MapViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.usp = ((CarparkActivity)getActivity()).GetUSP();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map_view, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.GMVControl);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            //requestPermissionLauncher.launch(perms);
            ActivityCompat.requestPermissions(getActivity(), perms,GPS_PERMISSION_CODE);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);
        map.setIndoorEnabled(false);
        this.AddMarkerAndAnimateToLocation();
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag() == null){
                    marker.showInfoWindow();
                    return true;
                }
                CarparkActivity cp = (CarparkActivity) getActivity();
                cp.OnSelection((Integer) marker.getTag());
                return false;
            }
        });
    }

    public void AddMarkerAndAnimateToLocation()
    {
        if (this.map == null) return;
        else if (this.usp == null) return;
        LatLng dest_loc = new LatLng(this.usp.getDest_latitude(), this.usp.getDest_longitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(dest_loc,16));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(dest_loc)      // Sets the center of the map to location user
                .zoom(16)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        map.addMarker(new MarkerOptions()
                .title("Target")
                .position(dest_loc)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
        );
    }

    public void UpdateCarparkMarkers(List<Carpark> list)
    {
        String color;
        int index = 0;
        for(Carpark c : list){
            if(c.getLots_available() != 0){
                if((double)c.getLots_available()/(double)c.getTotal_lots()<=0.2) {
                    Marker m = map.addMarker(new MarkerOptions()
                            .title(c.getAddress())
                            .position(new LatLng(c.getLatitude(), c.getLongitude()))
                            .snippet(c.getLots_available() + "/" + c.getTotal_lots())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    );
                    m.setTag(index++);
                    m.showInfoWindow();
                }
                else if((double)c.getLots_available()/(double)c.getTotal_lots()>0.7){
                    Marker m = map.addMarker(new MarkerOptions()
                            .title(c.getAddress())
                            .position(new LatLng(c.getLatitude(), c.getLongitude()))
                            .snippet(c.getLots_available() + "/" + c.getTotal_lots())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    );
                    m.setTag(index++);
                    m.showInfoWindow();
                }
                else {

                        Marker m = map.addMarker(new MarkerOptions()
                                .title(c.getAddress())
                                .position(new LatLng(c.getLatitude(), c.getLongitude()))
                                .snippet(c.getLots_available() + "/" + c.getTotal_lots())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        );
                        m.setTag(index++);
                        m.showInfoWindow();

                }
            }





        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });
}