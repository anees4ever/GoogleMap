package com.anees4ever.googlemap.api;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;

import com.anees4ever.googlemap.PermissionInterface;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class API_PlaceSearch {
    private static final String TAG = "LocationAddress";

    public interface OnAddressSearch {
        void onResult(List<PlaceAutocomplete> addressList);
    }
    public static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    public static class AddressSearch {
        final WeakReference<Context> mContext;
        final String API_KEY;
        final OnAddressSearch mListener;
        String mSearchStr= "";
        List<PlaceAutocomplete> mAddressList= null;
        boolean aborted= false;
        public AddressSearch(Context context, String apiKey, OnAddressSearch listener) {
            mContext= new WeakReference<>(context);
            API_KEY= apiKey;
            mListener= listener;
        }
        public void abort() {
            aborted= true;
        }
        public void startSearch(String search) {
            mSearchStr= search;
            mAddressList = null;
            try {
                if(!PermissionInterface.validatePermission(mContext.get(), PermissionInterface.API_PLACE_SEARCH)) {
                    if(mListener != null) {
                        mListener.onResult(null);
                    }
                    return;
                }

                Places.initialize(mContext.get().getApplicationContext(), API_KEY);
                PlacesClient placesClient = Places.createClient(mContext.get());

                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(mSearchStr)
                        .build();

                placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                    try {
                        mAddressList= new ArrayList<>();
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            mAddressList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                                    prediction.getPrimaryText(STYLE_BOLD),
                                    prediction.getSecondaryText(null),
                                    prediction.getFullText(null)));

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    onComplete();
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                    onComplete();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void onComplete() {
            if(!aborted) {
                if (mListener != null) {
                    mListener.onResult(mAddressList);
                }
            }
        }
    }

    public static class PlaceAutocomplete {
        public String placeId;
        public CharSequence primaryText;
        public CharSequence secondaryText;
        public CharSequence fullText;

        PlaceAutocomplete(String placeId, CharSequence primaryText, CharSequence secondaryText, CharSequence fullText) {
            PlaceAutocomplete.this.placeId = placeId;
            PlaceAutocomplete.this.primaryText = primaryText;
            PlaceAutocomplete.this.secondaryText = secondaryText;
            PlaceAutocomplete.this.fullText = fullText;
        }
    }
}