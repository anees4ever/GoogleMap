package com.anees4ever.googlemap.api;

import android.content.Context;
import android.util.Log;

import com.anees4ever.googlemap.PermissionInterface;
import com.anees4ever.googlemap.network.RetrofitCalls;

public class API_TimezoneByLocation {
    private static final String TIMEZONE_URL = "https://maps.googleapis.com/maps/api/timezone/json?location=%s,%s&timestamp=%s&key=%s";

    public interface Response {
        void processFinish(int dstOffset, int rawOffset, String status, String timeZoneId, String timeZoneName);
        void processError(String errorMessage);
    }

    public static boolean get(Context context, String apiKey, double lat, double lng, Response asyncResponse) {
        return get(context, String.format(TIMEZONE_URL, "" + lat, "" + lng, ""+(System.currentTimeMillis()/1000), apiKey), asyncResponse);
    }
    public static boolean get(Context context, String url, Response asyncResponse) {
        try {
            if(!PermissionInterface.validatePermission(context, PermissionInterface.API_TIMEZONE)) {
                if(asyncResponse != null) {
                    asyncResponse.processError("Permission rejected");
                }
                return false;
            }

            RetrofitCalls.getJSON(url, (status, object) -> {
                try {
                    if(asyncResponse != null) {
                        if (status && object != null) {
                            if(object.getString("status").equalsIgnoreCase("ok")) {
                                asyncResponse.processFinish(object.getInt("dstOffset"),
                                        object.getInt("rawOffset"),
                                        object.getString("status"), //OK
                                        object.getString("timeZoneId"),//Asia/Calcutta
                                        object.getString("timeZoneName"));//India Standard Time
                            } else {
                                asyncResponse.processError(object.getString("errorMessage"));
                            }
                        } else {
                            asyncResponse.processError("Invalid Response");
                        }
                    }
                } catch (Exception e) {
                    Log.e("Error", "Cannot process JSON results", e);
                    asyncResponse.processError("Failed to serve result");
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}