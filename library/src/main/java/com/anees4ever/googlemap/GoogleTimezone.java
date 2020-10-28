package com.anees4ever.googlemap;

import android.util.Log;
import com.anees4ever.googlemap.network.RetrofitCalls;

public class GoogleTimezone {
    private static final String TIMEZONE_URL = "https://maps.googleapis.com/maps/api/timezone/json?location=%s,%s&timestamp=%s&key=%s";

    public interface Response {
        void processFinish(int dstOffset, int rawOffset, String status, String timeZoneId, String timeZoneName);
    }

    public static boolean get(String apiKey, double lat, double lng, Response asyncResponse) {
        try {
            String sUrl= String.format(TIMEZONE_URL, "" + lat, "" + lng, ""+(System.currentTimeMillis()/1000), apiKey);
            RetrofitCalls.getJSON(sUrl, (status, object) -> {
                try {
                    if(asyncResponse != null) {
                        if (status && object != null) {
                            asyncResponse.processFinish(object.getInt("dstOffset"),
                                    object.getInt("rawOffset"),
                                    object.getString("status"), //OK
                                    object.getString("timeZoneId"),//Asia/Calcutta
                                    object.getString("timeZoneName"));//India Standard Time
                        } else {
                            asyncResponse.processFinish(0, 0, "ERROR", "", "");
                        }
                    }
                } catch (Exception e) {
                    Log.e("Error", "Cannot process JSON results", e);
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}