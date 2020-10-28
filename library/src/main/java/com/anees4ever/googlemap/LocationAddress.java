package com.anees4ever.googlemap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;

import com.anees4ever.googlemap.network.RetrofitCalls;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class LocationAddress {
    private static final String TAG = "LocationAddress";
    public static final int ERROR_NETWROK_OFFLINE= 1;
    public static final int ERROR_NETWROK_ERROR= 2;
    public static final int ERROR_EMPTY_ADDRESS= 3;

    public static final String MAP_GEOCODER_ELEVATION_URL= "https://maps.googleapis.com/maps/api/elevation/json?locations=%s,%s&key=%s";

    public interface OnAddressListener {
        void onAddressFound(final List<Address> addressList);
        void onAddressNotFound(final int error, final String errorStr);
    }

    public static void getAddressFromLocation(final Context context, final double latitude, final double longitude,
                                              final OnAddressListener listener) {
        AsyncTaskLocationAddress locationAddress= new AsyncTaskLocationAddress(context, listener);
        locationAddress.executeOnExecutor(Executors.newSingleThreadExecutor(), latitude, longitude);
    }

    public static class AsyncTaskLocationAddress extends AsyncTask<Double,Void,Void> {
        private final WeakReference<Context> mContext;
        private final OnAddressListener mListener;

        List<Address> addressList = null;

        int error = 0;
        String errorStr = "";

        public AsyncTaskLocationAddress(Context context, OnAddressListener listener) {
            mContext = new WeakReference<>(context);
            mListener = listener;
        }

        public boolean isOnline() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mContext.get().checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
                ConnectivityManager connMgr = (ConnectivityManager) mContext.get().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                return (networkInfo != null && networkInfo.isConnected());
            } catch (Exception e) {
                return true;
            }
        }

        private static final String address_type_premise = "premise";
        private static final String address_type_street_number = "street_number";
        private static final String address_type_route = "route";
        private static final String address_type_sublocality_level_3 = "sublocality_level_3";
        private static final String address_type_sublocality_level_2 = "sublocality_level_2";
        private static final String address_type_sublocality_level_1 = "sublocality_level_1";
        private static final String address_type_locality = "locality";
        private static final String address_type_administrative_area_level_2 = "administrative_area_level_2";
        private static final String address_type_administrative_area_level_1 = "administrative_area_level_1";
        private static final String address_type_country = "country";
        private static final String address_type_postal_code = "postal_code";

        @Override
        protected Void doInBackground(Double... params) {
            try {
                if (!isOnline()) {
                    error = ERROR_NETWROK_OFFLINE;
                    errorStr = "No internet connection.";
                    return null;
                }
                File file = new File(mContext.get().getCacheDir(), "location.json");
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
                return getAddressWithGeoCoder(params);
            } catch (Exception e) {
                return null;
            }
        }

        private Void getAddressWithGeoCoder(Double... params) {
            Geocoder geocoder= new Geocoder(mContext.get(), Locale.getDefault());
            try {
                addressList= geocoder.getFromLocation(params[0], params[1], 1);
                error= 0;
                errorStr= "";
            } catch (Exception e) {
                error= ERROR_NETWROK_ERROR;
                errorStr= e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (error == 0) {
                if (addressList != null && addressList.size() > 0) {
                    if (mListener != null) {
                        mListener.onAddressFound(addressList);
                    }
                } else {
                    if (mListener != null) {
                        mListener.onAddressNotFound(ERROR_EMPTY_ADDRESS, "Address not found.");
                    }
                }
            } else {
                if (mListener != null) {
                    mListener.onAddressNotFound(error, errorStr);
                }
            }
            super.onPostExecute(aVoid);
        }
    }


    public interface OnAltitudeListener {
        void onResult(double altitude);
    }
    public static boolean findAltitude(String apiKey, double lat, double lng, OnAltitudeListener mListener) {
        try {
            String sUrl= String.format(MAP_GEOCODER_ELEVATION_URL, "" + lat, "" + lng, apiKey);
            RetrofitCalls.getJSON(sUrl, (status, object) -> {
                try {
                    if(mListener != null) {
                        if(status && object != null) {
                            JSONObject elevResult= object.getJSONArray("results").getJSONObject(0);
                            mListener.onResult(elevResult.getDouble("elevation"));
                        } else {
                            mListener.onResult(0D);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mListener.onResult(0D);
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface OnPlaceLatLngListener {
        void onResult(LatLng latlng);
    }

    public static class PlaceLatLngFinder extends AsyncTask<String, Void, LatLng> {
        final WeakReference<Context> mContext;
        final String API_KEY;
        final OnPlaceLatLngListener mListener;
        public PlaceLatLngFinder(Context context, String apiKey, OnPlaceLatLngListener listener) {
            mContext= new WeakReference<>(context);
            API_KEY= apiKey;
            mListener= listener;
        }
        public void find(String placeID) {
            //executeOnExecutor(Executors.newSingleThreadExecutor(), placeID);
            doInBackground(placeID);
        }
        @Override
        protected LatLng doInBackground(final String... params) {
            try {
                Places.initialize(mContext.get().getApplicationContext(), API_KEY);
                PlacesClient placesClient = Places.createClient(mContext.get());
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);
                FetchPlaceRequest request = FetchPlaceRequest.builder(params[0], placeFields).build();
                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    if(mListener!=null) {
                        mListener.onResult(place.getLatLng());
                    }
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        int statusCode = apiException.getStatusCode();
                        if(mListener!=null) {
                            mListener.onResult(null);
                        }
                    }
                });
            } catch (Exception e) {
                //ignore
            }
            return null;
        }

        @Override
        protected void onPostExecute(LatLng result) {
            if(mListener!=null) {
                mListener.onResult(result);
            }
            super.onPostExecute(result);
        }
    }


    public interface OnAddressSearch {
        void onResult(List<PlaceAutocomplete> addressList);
    }
    public static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    public static class AddressSearch extends AsyncTask<Double, Void, Void> {
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
        public void startSearch(String search) {
            mSearchStr= search;
            //executeOnExecutor(Executors.newSingleThreadExecutor());
            doInBackground();
        }
        public void abort() {
            aborted= true;
        }
        @Override
        protected Void doInBackground(Double... params) {
            try {
                mAddressList = null;
                try {
                    Places.initialize(mContext.get().getApplicationContext(), API_KEY);
                    PlacesClient placesClient = Places.createClient(mContext.get());

                    // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
                    // and once again when the user makes a selection (for example when calling fetchPlace()).
                    AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

                    // Create a RectangularBounds object.
                    //RectangularBounds bounds = RectangularBounds.newInstance(
                    //        new LatLng(-33.880490, 151.184363),
                    //        new LatLng(-33.858754, 151.229596));
                    // Use the builder to create a FindAutocompletePredictionsRequest.
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            // Call either setLocationBias() OR setLocationRestriction().
                            //.setLocationBias(bounds)
                            //.setLocationRestriction(bounds)
                            //.setCountry("au")
                            //.setTypeFilter(TypeFilter.ADDRESS)
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
                        onPostExecute(null);
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                        onPostExecute(null);
                    });
                } catch (Exception e) {
                    //ignore
                }
            } catch (Exception e) {
                //ignore
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if(!aborted) {
                if (mListener != null) {
                    mListener.onResult(mAddressList);
                }
            }
            super.onPostExecute(result);
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