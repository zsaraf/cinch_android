package com.seshtutoring.seshapp.view.fragments;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
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
import com.seshtutoring.seshapp.util.LocationManager;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.StorageUtils;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class LearnViewFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = LearnViewFragment.class.getName();
    private static View view;

    private static GoogleMap mMap;
    private LocationManager locationManager;
    public static final String BLURRED_MAP_BITMAP_PATH_KEY = "blurred_map_bitmap";
    public static final String CHOSEN_LOCATION_LAT = "chosen_location_lat";
    public static final String CHOSEN_LOCATION_LONG = "chosen_location_long";

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

        LayoutUtils utils = new LayoutUtils(getActivity());

        final ImageButton currentLocationButton = (ImageButton) view.findViewById(R.id.current_location_button);
        LinearLayout.MarginLayoutParams margins = (LinearLayout.MarginLayoutParams) currentLocationButton.getLayoutParams();
        margins.topMargin += utils.getActionBarHeightPx();
        currentLocationButton.setLayoutParams(margins);

        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location currentLocation = locationManager.getCurrentLocation();
                if (currentLocation != null) {
                    moveCameraToLocation(currentLocation, true);
                }
            }
        });


        SeshButton requestButton = (SeshButton) view.findViewById(R.id.request_button);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRequestActivityWithBlurTransition();
            }
        });

        return view;
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
                getActivity().startActivityForResult(intent, RequestActivity.ENTER_LEARN_REQUEST_FLOW);
                getActivity().overridePendingTransition(R.anim.fade_in, 0);
            }
        });
    }

    public GoogleMap getMap() {
        return mMap;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == RequestActivity.ENTER_LEARN_REQUEST_FLOW) {
            if (resultCode == RequestActivity.LEARN_REQUEST_CREATE_SUCCESS) {

            } else if (resultCode == RequestActivity.LEARN_REQUEST_CREATE_FAILURE) {
                Toast.makeText(getActivity(), "Sick, didn't work.  :(", Toast.LENGTH_LONG).show();
            } else if (resultCode == RequestActivity.LEARN_REQUEST_CREATE_EXITED){
                // log to mixpanel -- user exited request flow
            }
        }
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
        // Do a null check to confirm that we have not already instantiated the map.
//        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
//        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
        ((HomeFragment)getParentFragment()).mapViewReady();
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        moveCameraToLocation(locationManager.getCurrentLocation(), false);
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
}
