package com.anees4ever.googlemap;

import com.anees4ever.dmsedit.DMS;
import com.anees4ever.googlemap.api.API_AddressByLocation;
import com.anees4ever.googlemap.api.API_LocationByPlaceID;
import com.anees4ever.googlemap.api.API_PlaceSearch;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Activity_GoogleMap extends ActivityEx implements LocationListener, OnMapReadyCallback {
    String GMAP_API_KEY_APP= "";
    FusedLocation fusedLocation;
    boolean locationReceived= false;

	GoogleMap googleMap;
    Location mLocation;
    Address mAddress;
    List<API_PlaceSearch.PlaceAutocomplete> mAddressList;
    API_PlaceSearch.AddressSearch mAddressSearch;
    private boolean setTextFromSearch= false;

	RelativeLayout rlSearchBar;
    ScrollView svSearchResult;
    EditText edSearch;
	ProgressBar pbProgress;
	ImageButton btnClearSearch, btnMapMode;
	LinearLayout llSearchResults;

    FloatingActionButton fabClose, fabSelect, fabRetry;
	TextView tvLatLon, tvAddress;

    Timer searchTimer;
    boolean bAllowChanges= true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_googlemap);

        if(!PermissionInterface.validatePermission(context, PermissionInterface.API_MAPVIEW)) {
            finish();
            return;
        }

		getAPIKeys();
        rlSearchBar= (RelativeLayout) findViewById(R.id.rlSearchBar);
        svSearchResult= (ScrollView) findViewById(R.id.svSearchResult);
        edSearch= (EditText) findViewById(R.id.edSearch);
        pbProgress= (ProgressBar) findViewById(R.id.pbProgress);
        btnClearSearch= (ImageButton) findViewById(R.id.btnClearSearch);
        btnMapMode= (ImageButton) findViewById(R.id.btnMapMode);
        llSearchResults= (LinearLayout) findViewById(R.id.llSearchResults);


        svSearchResult.setVisibility(View.GONE);
        pbProgress.setVisibility(View.GONE);
        btnClearSearch.setVisibility(View.GONE);

        fabClose= (FloatingActionButton) findViewById(R.id.fabClose);
        fabSelect= (FloatingActionButton) findViewById(R.id.fabSelect);
        fabRetry= (FloatingActionButton) findViewById(R.id.fabRetry);

        tvLatLon= (TextView) findViewById(R.id.tvLatLon);
        tvAddress= (TextView) findViewById(R.id.tvAddress);

        fabSelect.setVisibility(View.GONE);
        fabRetry.setVisibility(View.GONE);

        fusedLocation= new FusedLocation(this);
        fusedLocation.mLocationListener= this;

        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    btnClearSearch.setVisibility(View.VISIBLE);

                    if(searchTimer!=null) {
                        searchTimer.cancel();
                        searchTimer= null;
                    }

                    if(edSearch.getText().length() < 3) {
                        return;
                    }
                    if(bAllowChanges) {
                        searchTimer = new Timer();
                        searchTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(Activity_GoogleMap.this::startSearch);
                            }
                        }, 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        edSearch.setOnEditorActionListener(
                (textView, id, keyEvent) -> {
                    hideFocus(edSearch);
                    startSearch();
                    return true;
                }
        );
		MapFragment mapFragment= (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		if(getIntent().hasExtra("latitude")) {
		    locationReceived= true;
		    mLocation= new Location("gps");
            mLocation.setLatitude(getIntent().getDoubleExtra("latitude", 0));
            mLocation.setLongitude(getIntent().getDoubleExtra("longitude", 0));
        } else if (getIntent().hasExtra("searchfor")) {
		    bAllowChanges= false;
		    edSearch.setText(getIntent().getStringExtra("searchfor"));
		    bAllowChanges= true;
            rlSearchBar.postDelayed(handlerSearchPlaces, 500);
        }
	}

	private void getAPIKeys() {
	    try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            GMAP_API_KEY_APP = appInfo.metaData.getString("com.google.android.geo.API_KEY");
        } catch (Exception e) {
	        e.printStackTrace();
            GMAP_API_KEY_APP= "";
        }
    }

    private final Runnable handlerSearchPlaces= this::startSearch;

	//Activity Handlers
    @Override
    protected void onResume() {
        super.onResume();
        if(!locationReceived) {
            if(fusedLocation.enabled(true)) {
                locationReceived= false;
                fusedLocation.start();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        fusedLocation.stop();
    }
    @Override
    public void onBackPressed() {
        if(svSearchResult.getVisibility()==View.VISIBLE) {
            onClickClearSearch(btnClearSearch);
            return;
        }
        setResult(RESULT_CANCELED, getIntent());
        super.onBackPressed();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    //Activity Handlers


    //Map Handlers
	@Override
	public void onMapReady(GoogleMap map) {
        try {
            btnMapMode.setImageResource(getMapMode()==GoogleMap.MAP_TYPE_NORMAL?R.drawable.map_sattelite:R.drawable.map_normal);
            googleMap = map;
            googleMap.setMapType(getMapMode());
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.setOnCameraChangeListener((position) -> {
                    //findAddress();
                    try { edSearch.removeCallbacks(handlerFindAddress); } catch (Exception e) {/**/}
                    try { edSearch.postDelayed(handlerFindAddress, 1000); } catch (Exception e) {/**/}
                }
            );
            googleMap.setOnPoiClickListener((poi)-> {
                locationReceived= true;
                bAllowChanges= false;
                edSearch.setText(poi.name);
                btnClearSearch.setVisibility(View.VISIBLE);
                bAllowChanges= false;
                setTextFromSearch= true;
                setLocation(new LatLng(poi.latLng.latitude, poi.latLng.longitude));
            });
            if (mLocation != null) {
                onLocationChanged(mLocation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setLocation(LatLng myLocation) {
	    try {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(myLocation)      //Sets the center of the map to Mountain View
                    .zoom(17)                //Sets the zoom
                    .build();                //Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            String localtionStr= DMS.parseDDtoDMS(mLocation.getLatitude(), mLocation.getLongitude());
            tvLatLon.setText(localtionStr);
            //findAddress();
        } catch (Exception e) {
	        e.printStackTrace();
        }
	}
	private final Runnable handlerFindAddress= this::findAddress;
	public void findAddress() {
	    try {
	        if(googleMap.getCameraPosition().target.latitude == 0) {
	            return;
            }
            try { edSearch.removeCallbacks(handlerFindAddress); } catch (Exception e) {/**/}
            tvAddress.setText(R.string.loading_address);
            fabSelect.setVisibility(View.GONE);
            fabRetry.setVisibility(View.GONE);
            String localtionStr= DMS.parseDDtoDMS(googleMap.getCameraPosition().target.latitude,
                                googleMap.getCameraPosition().target.longitude);
            tvLatLon.setText(localtionStr);
            API_AddressByLocation.getAddressFromLocation(this, googleMap.getCameraPosition().target.latitude,
                    googleMap.getCameraPosition().target.longitude, new API_AddressByLocation.OnAddressListener() {
                @Override
                public void onAddressFound(List<Address> addressList) {
                    try {
                        fabSelect.setVisibility(View.VISIBLE);
                        fabRetry.setVisibility(View.GONE);
                        mAddress= addressList.get(0);
                        if(setTextFromSearch) {
                            mAddress.setSubLocality(edSearch.getText().toString().trim());
                        }
                        String locality= mAddress.getSubLocality();
                        locality= locality==null||locality.isEmpty()?"":"<strong>" + locality + "</strong><br />";
                        tvAddress.setText(Html.fromHtml(locality + mAddress.getAddressLine(0)));
                        setTextFromSearch= false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAddressNotFound(int error, String errorStr) {
                    try {
                        fabSelect.setVisibility(View.VISIBLE);
                        fabRetry.setVisibility(View.VISIBLE);
                        tvAddress.setText(R.string.address_not_found);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
	        e.printStackTrace();
        }
	}
    //Map Handlers

    //Location Handlers
    private final API_PlaceSearch.OnAddressSearch mAddressSearchListener= new API_PlaceSearch.OnAddressSearch() {
        @Override
        public void onResult(List<API_PlaceSearch.PlaceAutocomplete> addressList) {
            try {
                pbProgress.setVisibility(View.GONE);
                drawSearchResultEx(addressList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private void startSearch() {
        try {
            if(searchTimer!=null) {
                searchTimer.cancel();
                searchTimer= null;
            }
            if(edSearch.getText().toString().trim().isEmpty()) {
                return;
            }
            if(mAddressSearch!=null) {
                mAddressSearch.abort();
            }

            String searchStr= edSearch.getText().toString();
            LatLng latLng= DMS.parseDMStoDD(searchStr);
            if(latLng!=null) {
                int selStart= edSearch.getSelectionStart();
                onClickClearSearch(btnClearSearch);
                bAllowChanges= false;
                edSearch.setText(searchStr);
                edSearch.setSelection(selStart);
                btnClearSearch.setVisibility(View.VISIBLE);
                bAllowChanges= true;
                setLocation(latLng);
            } else {
                llSearchResults.removeAllViews();
                pbProgress.setVisibility(View.VISIBLE);
                mAddressSearch = new API_PlaceSearch.AddressSearch(this, GMAP_API_KEY_APP, mAddressSearchListener);
                mAddressSearch.startSearch(edSearch.getText().toString().trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void drawSearchResultEx(List<API_PlaceSearch.PlaceAutocomplete> addressList) {
        try {
            mAddressList= new ArrayList<>();
            svSearchResult.setVisibility(View.VISIBLE);
            llSearchResults.removeAllViews();
            if(addressList!=null && addressList.size() > 0) {
                mAddressList.addAll(addressList);
                for(int i=0; i<mAddressList.size(); i++) {
                    drawAddress(mAddressList.get(i), i);
                }
            } else {
                drawAddress(null, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void drawAddress(API_PlaceSearch.PlaceAutocomplete placeAutocomplete, int position) {
        try {
            View view= getLayoutInflater().inflate(R.layout.activity_googlemap_search_item, null);
            TextView tvTitle= (TextView) view.findViewById(R.id.tvTitle);
            TextView tvDesc= (TextView) view.findViewById(R.id.tvDesc);

            llSearchResults.addView(view);
            if(placeAutocomplete==null) {
                tvTitle.setText(R.string.no_result_found);
                tvDesc.setVisibility(View.GONE);
            } else {
                tvTitle.setText(placeAutocomplete.primaryText);
                tvDesc.setText(placeAutocomplete.secondaryText);

                View spacer= new View(this);
                spacer.setMinimumHeight(5);
                llSearchResults.addView(spacer);
                view.setOnClickListener(new OnItemClick(position));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class OnItemClick implements View.OnClickListener {
        final int mPosition;
        OnItemClick(int position) {
            mPosition= position;
        }
        @Override
        public void onClick(View v) {
            try {
                tvLatLon.setText(R.string.loading_lat_lng);
                tvAddress.setText(R.string.loading_address);
                fabSelect.setVisibility(View.GONE);
                fabRetry.setVisibility(View.GONE);
                bAllowChanges= false;
                edSearch.setText(mAddressList.get(mPosition).primaryText);
                bAllowChanges= true;
                svSearchResult.setVisibility(View.GONE);
                pbProgress.setVisibility(View.VISIBLE);
                API_LocationByPlaceID.PlaceLatLngFinder placeLatLngFinder= new API_LocationByPlaceID.PlaceLatLngFinder(Activity_GoogleMap.this,
                        GMAP_API_KEY_APP, new API_LocationByPlaceID.OnPlaceLatLngListener() {
                    @Override
                    public void onResult(LatLng latlng) {
                        try {
                            pbProgress.setVisibility(View.GONE);
                            if (latlng != null) {
                                setTextFromSearch= true;
                                setLocation(latlng);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(String errorMessage) {
                        try {
                            pbProgress.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                placeLatLngFinder.find(mAddressList.get(mPosition).placeId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	@Override
	public void onLocationChanged(Location location) {
	    try {
            if (location == null) {
                return;
            }
            locationReceived = true;
            fusedLocation.stop();

            mLocation = new Location(location);
            setLocation(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        } catch (Exception e) {
	        e.printStackTrace();
        }
	}
	@Override
	public void onProviderDisabled(String arg0) {}
	@Override
	public void onProviderEnabled(String arg0) {}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
    //Location Handlers

    //User Events
    public void onClickMapMode(View view) {
        try {
            if(getMapMode()==GoogleMap.MAP_TYPE_NORMAL) {
                setMapMode(GoogleMap.MAP_TYPE_SATELLITE);
            } else {
                setMapMode(GoogleMap.MAP_TYPE_NORMAL);
            }
            googleMap.setMapType(getMapMode());
            btnMapMode.setImageResource(getMapMode()==GoogleMap.MAP_TYPE_NORMAL?R.drawable.map_sattelite:R.drawable.map_normal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void onClickClearSearch(View view) {
        try {
            if(mAddressSearch!=null) {
                mAddressSearch.abort();
            }
            bAllowChanges= false;
            edSearch.setText("");
            bAllowChanges= true;
            pbProgress.setVisibility(View.GONE);
            svSearchResult.setVisibility(View.GONE);
            btnClearSearch.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	public void onClickClose(View view) {
		setResult(RESULT_CANCELED, getIntent());
		finish();
	}
	public void onClickRetry(View view) {
		findAddress();
	}
	public void onClickSelect(View view) {
        try {
            Intent intent = new Intent();
            intent.putExtra("latitude", googleMap.getCameraPosition().target.latitude);
            intent.putExtra("longitude", googleMap.getCameraPosition().target.longitude);
            if(mAddress!=null) {
                if(mAddress.getSubLocality()==null) {
                    intent.putExtra("locality", "");
                } else {
                    intent.putExtra("locality", mAddress.getSubLocality());
                }
                intent.putExtra("address", mAddress.getAddressLine(0));
            }
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
    //User Events

    public int getMapMode() {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            return preferences.getInt("gmap_MAP_MODE", GoogleMap.MAP_TYPE_NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
            return GoogleMap.MAP_TYPE_NORMAL;
        }
    }
    public void setMapMode(int mode) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor= preferences.edit();
            editor.putInt("gmap_MAP_MODE", mode);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
