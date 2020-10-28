package com.anees4ever.googlemap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class FusedLocation {
    public Activity mActivity;
    public LocationManager mLocationManager;
    public FusedLocationProviderClient mFusedLocationClient;
    public LocationRequest mFusedLocationRquest;
    public LocationListener mLocationListener;
    public LocationCallback mLocationCallback;

    public FusedLocation(Activity activity) {
        mActivity= activity;
        mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(mActivity);
        mFusedLocationRquest = new LocationRequest();
        mFusedLocationRquest.setInterval(100);
        mFusedLocationRquest.setFastestInterval(100);
        mFusedLocationRquest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback= new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if(mLocationListener!=null) {
                        mLocationListener.onLocationChanged(location);
                    }
                    stop();
                    break;
                }
            };
        };
    }

    public boolean enabled(boolean takeAction) {
        try {
            boolean enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!enabled && takeAction) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mActivity.startActivity(intent);
            }
            return enabled;
        } catch (Exception e) {
            /**/
        }
        return false;
    }

    public void start() {
        try {
            mFusedLocationClient.requestLocationUpdates(mFusedLocationRquest, mLocationCallback, null /* Looper */);
        } catch (Exception e) {
            /**/
        }
    }

    public void stop() {
        try { mFusedLocationClient.removeLocationUpdates(mLocationCallback); } catch (Exception e) {/**/}
        try { mLocationManager.removeUpdates(mLocationListener); } catch (Exception e) {/**/}
    }
}
