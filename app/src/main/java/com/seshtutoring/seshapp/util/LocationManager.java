package com.seshtutoring.seshapp.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
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
import com.seshtutoring.seshapp.view.MainContainerActivity;

import java.util.ArrayList;

/**
 * Created by nadavhollander on 9/2/15.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = LocationManager.class.getName();
    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final String CONNECTION_RESULT = "connection_result";
    public static final String RESOLUTION_REQUEST = "resolution_request";

    private static LocationManager mInstance;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private Location currentBestLocation;
    public Boolean isConnected;

    public interface LocationManagerSetUpListener {
        void onLocationManagerSetUp();
    }

    public LocationManager(Context context) {
        this.mContext = context;
    }

    public void connectClient() {
        this.mGoogleApiClient.connect();
    }

    public void disconnectClient() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public static LocationManager sharedInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LocationManager(context);
            mInstance.isConnected = false;
            mInstance.mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(mInstance)
                    .addOnConnectionFailedListener(mInstance)
                    .addApi(LocationServices.API)
                    .build();
        }

        return mInstance;
    }


    public Location getCurrentLocation() {
        if (currentBestLocation != null) {
            return currentBestLocation;
        } else {
            currentBestLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (currentBestLocation != null) {
                return currentBestLocation;
            } else {
                Location defaultLocationCoordinates = new Location("");
                defaultLocationCoordinates.setLatitude(37.4300d);
                defaultLocationCoordinates.setLongitude(-122.1700d);
                return defaultLocationCoordinates;
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        currentBestLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every 10 seconds

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        isConnected = true;
        mContext.sendBroadcast(new Intent(MainContainerActivity.LOCATION_MANAGER_CONNECTED));
    }

    @Override
    public void onConnectionSuspended(int e) {
        isConnected = false;
        Log.e(TAG, "Location Services connection suspended; " + e);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentBestLocation = location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            // Start an Activity that tries to resolve the error
            Intent resultIntent = new Intent();
            resultIntent.setAction(MainContainerActivity.LOCATION_MANAGER_FAILED);
            resultIntent.putExtra(CONNECTION_RESULT, connectionResult);
            resultIntent.putExtra(RESOLUTION_REQUEST, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            mContext.sendBroadcast(resultIntent);

        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
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
}
