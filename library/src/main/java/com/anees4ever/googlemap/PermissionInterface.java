package com.anees4ever.googlemap;

import android.content.Context;

public class PermissionInterface {
    public static final int API_MAPVIEW= 0;
    public static final int API_ADDRESS_BY_LOCATION= 1;
    public static final int API_ELEVATION_BY_LOCATION= 2;
    public static final int API_LOCATION_BY_PLACEID= 3;
    public static final int API_PLACE_SEARCH= 4;
    public static final int API_TIMEZONE= 5;

    public static void setPermissionHelper(Helper helper) {
        mHelper= helper;
    }
    public static boolean validatePermission(Context context, int source) {
        try {
            if(mHelper != null) {
                return mHelper.validatePermission(context, source);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
    public static Helper mHelper= null;

    public interface Helper {
        boolean validatePermission(Context context, int source);
    }
}
