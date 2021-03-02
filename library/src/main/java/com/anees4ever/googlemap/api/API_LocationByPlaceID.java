package com.anees4ever.googlemap.api;

import android.content.Context;

import com.anees4ever.googlemap.PermissionInterface;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

public class API_LocationByPlaceID {
    public interface OnPlaceLatLngListener {
        void onResult(LatLng latlng);
        void onError(String errorMessage);
    }

    public static class PlaceLatLngFinder  {
        final WeakReference<Context> mContext;
        final String API_KEY;
        final OnPlaceLatLngListener mListener;
        public PlaceLatLngFinder(Context context, String apiKey, OnPlaceLatLngListener listener) {
            mContext= new WeakReference<>(context);
            API_KEY= apiKey;
            mListener= listener;
        }
        public void find(String placeID) {
            try {
                if(!PermissionInterface.validatePermission(mContext.get(), PermissionInterface.API_LOCATION_BY_PLACEID)) {
                    if(mListener != null) {
                        mListener.onError("Permission rejected");
                    }
                    return;
                }

                Places.initialize(mContext.get().getApplicationContext(), API_KEY);
                PlacesClient placesClient = Places.createClient(mContext.get());
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeID, placeFields).build();
                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    if(mListener!=null) {
                        mListener.onResult(place.getLatLng());
                    }
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        if(mListener!=null) {
                            mListener.onError(apiException.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}