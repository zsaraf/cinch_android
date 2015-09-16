package com.seshtutoring.seshapp.util;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by nadavhollander on 9/2/15.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = LocationManager.class.getName();

    private static LocationManager mInstance;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private Location currentBestLocation;
    private LocationManagerSetUpListener listener;
    private boolean hasBeenSetUp;

    public interface LocationManagerSetUpListener {
        void onLocationManagerSetUp();
    }

    public LocationManager(Context context) {
        this.mContext = context;
    }

    public void setUp(LocationManagerSetUpListener listener) {
        this.listener = listener;

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        hasBeenSetUp = true;
    }

    public void registerLocationCallbacks() {
        if (!verifyHasBeenSetup()) return;
        mGoogleApiClient.connect();
    }

    public void unregisterLocationCallbacks() {
        if (!verifyHasBeenSetup()) return;
        mGoogleApiClient.disconnect();
    }

    public static LocationManager sharedInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LocationManager(context);
        }

        return mInstance;
    }


    public Location getCurrentLocation() {
        if (!verifyHasBeenSetup()) return null;

        if (currentBestLocation != null) {
            return currentBestLocation;
        } else {
            Log.e(TAG, "Unable to retrieve last known coordinates, resorting to default current location");
            Location defaultLocationCoordinates = new Location("");
            defaultLocationCoordinates.setLatitude(37.4300d);
            defaultLocationCoordinates.setLongitude(-122.1700d);
            return defaultLocationCoordinates;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        currentBestLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        if (listener != null) {
            listener.onLocationManagerSetUp();
        }
    }

    @Override
    public void onConnectionSuspended(int e) {
        Log.e(TAG, "Location Services connection suspended; " + e);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentBestLocation = location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Location Services connection failed; " + connectionResult);

        if (listener != null) {
            listener.onLocationManagerSetUp();
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private boolean verifyHasBeenSetup() {
        if (!hasBeenSetUp) {
            Log.e(TAG, "Cannot perform location operation; setUp() has not yet been called on this " +
                    "LocationManager instance.");
            return false;
        } else {
            return true;
        }
    }
}
