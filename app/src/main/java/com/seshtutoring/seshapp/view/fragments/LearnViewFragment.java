package com.seshtutoring.seshapp.view.fragments;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.enrique.stackblur.StackBlurManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.stats.ConnectionEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.OutstandingCharge;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LocationManager;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.SeshMixpanelAPI;
import com.seshtutoring.seshapp.util.StorageUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class LearnViewFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = LearnViewFragment.class.getName();
    private static View view;

    private static GoogleMap mMap;
    private LocationManager locationManager;
    private SeshButton requestButton;
    private ImageButton currentLocationButton;
    private boolean currentLocationButtonFilled;
    private SeshMixpanelAPI seshMixpanelAPI;
    public static final String BLURRED_MAP_BITMAP_PATH_KEY = "blurred_map_bitmap";
    public static final String CHOSEN_LOCATION_LAT = "chosen_location_lat";
    public static final String CHOSEN_LOCATION_LONG = "chosen_location_long";
    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.locationManager = LocationManager.sharedInstance(getActivity());
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }

        try {
            view = layoutInflater.inflate(R.layout.learn_view_fragment, container, false);
        } catch (InflateException e) {
            return view;
        }

        setUpMapIfNeeded();

        currentLocationButton = (ImageButton) view.findViewById(R.id.current_location_button);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationManager.isConnected) {
                    Location currentLocation = locationManager.getCurrentLocation();
                    if (currentLocation != null) {
                        moveCameraToLocation(currentLocation, true);
                        setCurrentLocationButtonFilled();
                    }
                }

            }
        });

        this.requestButton = (SeshButton) view.findViewById(R.id.request_button);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for outstanding charges
                Iterator<OutstandingCharge> outstandingChargesList = OutstandingCharge.findAll(OutstandingCharge.class);
                if (outstandingChargesList.hasNext()) {
                    handleOutstandingCharges(outstandingChargesList);
                } else {
                    (new StartRequestActivityAsyncTask()).execute();
                }
            }
        });

        LayoutUtils utils = new LayoutUtils(getActivity());

        ImageView marker = (ImageView) view.findViewById(R.id.location_marker);
        marker.setY(marker.getY() - (utils.getDimensionPx(R.dimen.learn_view_map_marker_height) / 2));

        this.seshMixpanelAPI = ((SeshApplication)getActivity().getApplication()).getSeshMixpanelAPI();

        broadcastReceiver = actionBroadcastReceiver;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.LOCATION_MANAGER_CONNECTED);
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            moveCameraToLocation(locationManager.getCurrentLocation(), true);
        }
    };

    private void setCurrentLocationButtonFilled() {
        if (currentLocationButtonFilled) return;

        if (isAdded()) {
            if (Build.VERSION.SDK_INT < 16) {
                currentLocationButton.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.jump_to_my_location_filled));
            } else if (Build.VERSION.SDK_INT < 21) {
                currentLocationButton.setBackground(getResources()
                        .getDrawable(R.drawable.jump_to_my_location_filled));
            } else {
                currentLocationButton.setBackground(getResources()
                        .getDrawable(R.drawable.jump_to_my_location_filled, null));
            }
        }

        currentLocationButtonFilled = true;
    }

    private void setCurrentLocationButtonUnfilled() {
        if (!currentLocationButtonFilled) return;

        if (Build.VERSION.SDK_INT < 16) {
            currentLocationButton.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.jump_to_my_location));
        } else if (Build.VERSION.SDK_INT < 21) {
            currentLocationButton.setBackground(getResources()
                    .getDrawable(R.drawable.jump_to_my_location));
        } else  {
            currentLocationButton.setBackground(getResources()
                    .getDrawable(R.drawable.jump_to_my_location, null));
        }

        currentLocationButtonFilled = false;
    }

    private void checkIfMarkerOnCurrentLocation() {
        if (locationManager.isConnected) {
            Location currentLocation = locationManager.getCurrentLocation();
            LatLng markerTarget = mMap.getCameraPosition().target;
            Location markerLocation = new Location("");
            markerLocation.setLatitude(markerTarget.latitude);
            markerLocation.setLongitude(markerTarget.longitude);

            if (currentLocation.distanceTo(markerLocation) < 5) {
                setCurrentLocationButtonFilled();
            } else {
                setCurrentLocationButtonUnfilled();
            }
        }
    }

    private void handleOutstandingCharges(Iterator<OutstandingCharge> outstandingChargeIterator) {
        Double finalAmount = 0.0;
        while (outstandingChargeIterator.hasNext()) {
            OutstandingCharge charge = outstandingChargeIterator.next();
            finalAmount += charge.amount;
        }

        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setDialogType(SeshDialog.SeshDialogType.TWO_BUTTON);
        seshDialog.setTitle("Outstanding Charge");
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        seshDialog.setMessage("You owe us " + formatter.format(finalAmount) + " from unpaid past Seshes. Pay now?");
        seshDialog.setFirstChoice("PAY");
        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seshDialog.setNetworking(true);
                SeshNetworking seshNetworking = new SeshNetworking(getActivity());
                seshNetworking.payOutstandingCharges(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject.getString("status").equals("SUCCESS")) {
                                OutstandingCharge.deleteAll(OutstandingCharge.class);
                                seshDialog.setNetworking(false);
                                (new StartRequestActivityAsyncTask()).execute();
                            } else {
                                Log.e(TAG, "Failed to pay outstanding charges: " + jsonObject.getString("message"));
                                seshDialog.networkOperationFailed("Error!", jsonObject.getString("message"), "OKAY",
                                        null);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to pay outstanding charges. Json malformed" + e);
                            seshDialog.networkOperationFailed("Error!", "Something went wrong.  Try again later.", "OKAY", null);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, "Failed to recreate Learn request; network error: " + volleyError);
                        seshDialog.networkOperationFailed("Network Error", "We couldn't reach the server.  Try again later.", "OKAY", null);
                    }
                });
            }
        });
        seshDialog.setSecondChoice("CANCEL");
        seshDialog.setSecondButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seshDialog.dismiss();
            }
        });
        seshDialog.setType("OUTSTANDING_CHARGE");

        seshDialog.show(getActivity().getFragmentManager(), "OUTSTANDING_CHARGE");
    }

    private void startRequestActivityWithBlurTransition() {
        Log.d(TAG, "REQUEST BUTTON HIT");
        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                Log.d(TAG, "SNAPSHOT READY");
                LayoutUtils utils = new LayoutUtils(getActivity());
                Bitmap blurredMap = utils.blurScreenshot(bitmap);

                File tmpFile = StorageUtils.storeTempImage(getActivity(),
                        blurredMap, "blurred_map");

                Log.d(TAG, "BLURRED IMAGE READY");
                Intent intent = new Intent(getActivity(), RequestActivity.class);
                intent.putExtra(BLURRED_MAP_BITMAP_PATH_KEY, tmpFile.getPath());
                intent.putExtra(CHOSEN_LOCATION_LAT, mMap.getCameraPosition().target.latitude);
                intent.putExtra(CHOSEN_LOCATION_LONG, mMap.getCameraPosition().target.longitude);
                getActivity().startActivityForResult(intent, RequestActivity.CREATE_LEARN_REQUEST_REQUEST_CODE);
                getActivity().overridePendingTransition(R.anim.fade_in, 0);
            }
        });
    }

    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
    }

    private void setUpMap() {
        int topMapPadding = getResources().getDimensionPixelOffset(R.dimen.learn_view_map_padding_top);

        mMap.setPadding(0, topMapPadding, 0, 0);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                checkIfMarkerOnCurrentLocation();
            }
        });
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                checkIfMarkerOnCurrentLocation();
            }
        });
        if (locationManager.isConnected) {
            moveCameraToLocation(locationManager.getCurrentLocation(), false);
        }
    }

    private void moveCameraToLocation(Location location, boolean animated) {
        if (animated) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 18), 500, null);
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 18));
        }
    }

    /**
     * Blocking SQL call that determines if an instant request is already open
     * @return Boolean instantRequestExists
     */
    private Boolean instantRequestOrSeshAlreadyExists() {
        return LearnRequest.find(LearnRequest.class, "is_instant = ?", "1").size() > 0 ||
                Sesh.find(Sesh.class, "is_student = ?", "1").size() > 0;
    }

    /**
     * Task checks to see if any instant requests already exist before launching the request flow.
     */
    private class StartRequestActivityAsyncTask extends AsyncTask<Void, Void, Boolean> {
        public Boolean doInBackground(Void... params) {
            return instantRequestOrSeshAlreadyExists();
        }

        public void onPostExecute(Boolean instantRequestExists) {
            if (instantRequestExists) {
                SeshDialog.showDialog(getActivity().getFragmentManager(), "Whoops!", "You can't have multiple requests or Seshes out" +
                        " at once!  Cancel your current request or Sesh if you wish to create a new Sesh request",
                        "OKAY", null, "only_one_instant");
            } else {
                seshMixpanelAPI.track("Entered Request Flow");
                requestButton.setEnabled(false);
                startRequestActivityWithBlurTransition();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       requestButton.setEnabled(true);
                    }
                }, 3000);
            }
        }
    }
}
