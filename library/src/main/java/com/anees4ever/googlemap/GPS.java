package com.anees4ever.googlemap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class GPS {
    private static Activity mActivity;
    private static Context mContext;
	private LocationManager mLocationManager= null;
	private LocationProvider mLocationProvider= null;
	private LocationListener mLocationListener= null;
	private String provider= LocationManager.GPS_PROVIDER;

	private long mMinimumTime= 0;
	private long mMinimumDistance= 0;

    public GPS(Activity activity) {
        mActivity= activity;
        mContext= activity;
        mLocationManager= (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if(allowed(true)) {
            mLocationProvider= mLocationManager.getProvider(provider);
        }
    }

    public GPS(Context context) {
        mActivity= null;
        mContext= context;
        mLocationManager= (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if(allowed(true)) {
            mLocationProvider= mLocationManager.getProvider(provider);
        }
    }

	public GPS setListener(LocationListener locationListener) {
		mLocationListener= locationListener;
		return this;
	}
	public LocationListener getListener() {
		return mLocationListener;
	}

    public GPS setMinimumTime(long minimumTime) {
        mMinimumTime= minimumTime;
        return this;
    }
    public long getMinimumTime() {
        return mMinimumTime;
    }

	public GPS setMinimumDistance(long minimumDistance) {
		mMinimumDistance= minimumDistance;
		return this;
	}
	public long getMinimumDistance() {
		return mMinimumDistance;
	}

	public boolean allowed(boolean takeAction) {
	    try {
	        if( (mActivity != null && !allowed(mActivity, takeAction)) || !allowed(mContext)) {
	            return false;
            }
            if(!mLocationManager.isProviderEnabled(provider)) {
                if(takeAction) {
                    showSettingsAlert(mContext);
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("GPS.allowed", e.toString());
        }
        return false;
    }

	public Location last() {
	    Location location= null;
        try {
            if(allowed(false)) {
                location= mLocationManager.getLastKnownLocation(provider);
            }
            if (location == null) {
                location = new Location(provider);
            }
        } catch (Exception e) {
            Log.e("GPS.last", e.toString());
        }
		return location;
	}
	public void listen() {
	    try {
            if(allowed(false)) {
                stop();
                Log.i("GPS", "listen started");
                mLocationManager.requestLocationUpdates(provider, mMinimumTime, mMinimumDistance, mLocationListener);
            }
        } catch (Exception e) {
            Log.e("GPS.listen", e.toString());
        }
	}
	public void stop() {
	    try {
            if(allowed(false)) {
                Log.i("GPS", "stop");
                if (mLocationManager != null) {
                    mLocationManager.removeUpdates(mLocationListener);
                    try {
                        mLocationManager.removeGpsStatusListener((Listener) mLocationListener);
                    } catch (Exception e) {
                        Log.e("GPS.stop.listener", e.toString());
                    }
                }
            }
        } catch (Exception e) {
            Log.e("GPS.stop", e.toString());
        }
	}

    public static void showSettingsAlert(Context mContext) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("SETTINGS");
            alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
            alertDialog.setPositiveButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            });
            alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            alertDialog.show();
        } catch (Exception e) {
            Log.e("GPS.showSettingsAlert", e.toString());
        }
    }

    public static boolean allowed(Activity activity, boolean takeAction) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (takeAction) {
                        activity.requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                    }
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("GPS.allowed", e.toString());
        }
        return false;
    }

    public static boolean enabled(Context context) {
        try {
            LocationManager locationManager= (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showSettingsAlert(context);
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean allowed(Context context) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("GPS.allowed", e.toString());
        }
        return false;
    }

    public static float distanceBetween(LatLng mFrom, LatLng mTo) {
        return distanceBetween(mFrom.latitude, mFrom.longitude, mTo.latitude, mTo.longitude) ;
    }

    public static float distanceBetween(double latA, double lonA, double latB, double lonB) {
        try {
            Location locationA = new Location("A");
            locationA.setLatitude(latA);
            locationA.setLongitude(lonA);

            Location locationB = new Location("B");
            locationB.setLatitude(latB);
            locationB.setLongitude(lonB);

            return Math.abs(locationA.distanceTo(locationB));
        } catch (Exception e) {
            return 0;
        }
    }
}
