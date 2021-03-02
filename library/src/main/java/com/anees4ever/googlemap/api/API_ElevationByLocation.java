package com.anees4ever.googlemap.api;

import android.content.Context;

import com.anees4ever.googlemap.PermissionInterface;
import com.anees4ever.googlemap.network.RetrofitCalls;
import org.json.JSONObject;

public class API_ElevationByLocation {
    public static final String ELEVATION_URL= "https://maps.googleapis.com/maps/api/elevation/json?locations=%s,%s&key=%s";

    public interface OnAltitudeListener {
        void onResult(double altitude);
        void onError(String errorMessage);
    }
    public static boolean findAltitude(Context context, String apiKey, double lat, double lng, OnAltitudeListener mListener) {
        return findAltitude(context, String.format(ELEVATION_URL, "" + lat, "" + lng, apiKey), mListener);
    }
    public static boolean findAltitude(Context context, String url, OnAltitudeListener mListener) {
        try {
            if(!PermissionInterface.validatePermission(context, PermissionInterface.API_ELEVATION_BY_LOCATION)) {
                if(mListener != null) {
                    mListener.onError("Permission rejected");
                }
                return true;
            }
            RetrofitCalls.getJSON(url, (status, object) -> {
                try {
                    if(mListener != null) {
                        if(status && object != null) {
                            if(object.getString("status").equalsIgnoreCase("ok")) {
                                JSONObject elevResult= object.getJSONArray("results").getJSONObject(0);
                                mListener.onResult(elevResult.getDouble("elevation"));
                            } else {
                                mListener.onError(object.getString("error_message"));
                            }
                        } else {
                            mListener.onError("Invalid response");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mListener.onError("Failed to serve result");
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}