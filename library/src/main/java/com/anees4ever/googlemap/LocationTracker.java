package com.anees4ever.googlemap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationTracker {
    public static final int REQUEST_CODE= 0x9546;

    private final Context mContext;
    private final com.anees4ever.googlemap.Settings mSettings;

    private LocationManager mLocationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mFusedLocationRquest;
    private LocationCallback mLocationCallback;

    private boolean mIsTracking= false;

    public LocationTracker(Context context, com.anees4ever.googlemap.Settings settings) {
        mContext= context;
        mSettings= settings==null?new com.anees4ever.googlemap.Settings():settings;

        mLocationManager= (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(mContext);
    }

    public boolean hasPermission() {
        return hasPermission(false, null);
    }
    public boolean hasPermission(boolean prompt, Activity activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean permission= mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if(!permission) {
                    if (prompt) {
                        activity.requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 911);
                    }
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    public boolean enabled(boolean openPrompt) {
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
                !mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            if(openPrompt) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                if(mContext instanceof Activity) {
                    ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE);
                } else {
                    mContext.startActivity(intent);
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean isTracking() {
        return mIsTracking;
    }

    public void start(LocationCallback locationCallback) {
        try {
            if(mIsTracking) {
                stop();
            }
            mLocationCallback = locationCallback;
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (hasPermission()) {
                if(mLocationCallback != null) mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
            mIsTracking= false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() {
        try {
            if(mIsTracking) {
                stop();
            }
            if (hasPermission() && enabled(false)) {
                if(mLocationCallback==null) {
                    return;
                }
                createRequestObject();
                mFusedLocationClient.requestLocationUpdates(mFusedLocationRquest, mLocationCallback, null /* Looper */);
                mIsTracking= true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRequestObject() {
        try {
            mFusedLocationRquest = new LocationRequest();
            mFusedLocationRquest.setInterval(mSettings.UPDATE_INTERVAL);
            mFusedLocationRquest.setFastestInterval(mSettings.FAST_INTERVAL);
            mFusedLocationRquest.setPriority(mSettings.PRIORITY);
            mFusedLocationRquest.setSmallestDisplacement(mSettings.UPDATE_DISTANCE);
            if (mSettings.UPDATE_COUNT > 0) {
                mFusedLocationRquest.setNumUpdates(mSettings.UPDATE_COUNT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void quickRequest(Context mContext, LocationCallback mLocationCallback) {
        try {
            com.anees4ever.googlemap.Settings mSettings= new com.anees4ever.googlemap.Settings();
            mSettings.UPDATE_DISTANCE= 0;
            mSettings.UPDATE_INTERVAL= 1000 * 30;
            mSettings.FAST_INTERVAL= 1000 * 30;
            mSettings.UPDATE_COUNT= 1;
            mSettings.PRIORITY= LocationRequest.PRIORITY_HIGH_ACCURACY;
            LocationTracker mLocationTracker= new LocationTracker(mContext, mSettings);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!mLocationTracker.hasPermission()) {
                    if(mLocationCallback != null) {
                        mLocationCallback.onLocationResult(null);
                    }
                    return;
                }
            }

            if (!mLocationTracker.enabled(false)) {
                if(mLocationCallback != null) {
                    mLocationCallback.onLocationResult(null);
                }
                return;
            }

            mLocationTracker.start(mLocationCallback);
        } catch (Exception e) {
            if(mLocationCallback != null) {
                mLocationCallback.onLocationResult(null);
            }
            e.printStackTrace();
        }
    }
}
