package com.example.sc2006_canpark_clientapp.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class GpsReceiver extends BroadcastReceiver {
    private final LocationCallBack locationCallBack;

    public GpsReceiver(LocationCallBack iLocationCallBack){
        this.locationCallBack = iLocationCallBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!gpsEnabled)
                locationCallBack.onLocationTriggered();
        }
    }
}
