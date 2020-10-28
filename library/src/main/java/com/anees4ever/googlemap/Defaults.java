package com.anees4ever.googlemap;

import com.google.android.gms.location.LocationRequest;

public class Defaults {
    /*
    Default Location Update Interval - 30 minutes
    */
    public static final int DEFAULT_UPDATE_INTERVAL= 1000 * 60 * 30;
    /*
    Default Location Fast Update Interval - 30 minutes
    */
    public static final int DEFAULT_FAST_INTERVAL= 1000 * 60 * 30;
    /*
    Default Update Distance - 1000meter/1KM
    */
    public static final float DEFAULT_UPDATE_DISTANCE= 1000;

    /*
    Default Location Priority
    */
    public static final int DEFAULT_PRIORITY= LocationRequest.PRIORITY_HIGH_ACCURACY;

    /*
    Default Update Count
    */
    public static final int DEFAULT_UPDATE_COUNT= -1;
}
