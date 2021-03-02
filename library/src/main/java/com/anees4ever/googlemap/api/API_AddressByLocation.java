package com.anees4ever.googlemap.api;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;

import com.anees4ever.googlemap.PermissionInterface;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class API_AddressByLocation {
    public static final int ERROR_NETWROK_OFFLINE= 1;
    public static final int ERROR_NETWROK_ERROR= 2;
    public static final int ERROR_EMPTY_ADDRESS= 3;

    public interface OnAddressListener {
        void onAddressFound(final List<Address> addressList);
        void onAddressNotFound(final int error, final String errorStr);
    }

    public static void getAddressFromLocation(final Context context, final double latitude, final double longitude,
                                              final OnAddressListener listener) {
        if(!PermissionInterface.validatePermission(context, PermissionInterface.API_ADDRESS_BY_LOCATION)) {
            if(listener != null) {
                listener.onAddressNotFound(0, "Permission rejected");
            }
            return;
        }
        AsyncTaskLocationAddress locationAddress= new AsyncTaskLocationAddress(context, listener);
        locationAddress.executeOnExecutor(Executors.newSingleThreadExecutor(), latitude, longitude);
    }

    private static class AsyncTaskLocationAddress extends AsyncTask<Double,Void,Void> {
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
}