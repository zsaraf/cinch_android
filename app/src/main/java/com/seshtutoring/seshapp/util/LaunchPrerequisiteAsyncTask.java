package com.seshtutoring.seshapp.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.seshtutoring.seshapp.model.Constants;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.SeshNetworking.SynchronousRequest;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by nadavhollander on 8/3/15.
 */
public class LaunchPrerequisiteAsyncTask extends AsyncTask<Void, Void, Void>
        implements LocationManager.LocationManagerSetUpListener {
    private static final String TAG = LaunchPrerequisiteAsyncTask.class.getName();

    private Context mContext;
    private PrereqsFulfilledListener listener;
    private Object lock = new Object();

    public enum LaunchPrerequisiteFlag {
        LOCATION_MANAGER_LOADED, CONSTANTS_FETCHED, SESH_INFORMATION_FETCHED
    }

    private boolean locationManagerLoaded;
    private boolean constantsFetched;
    private boolean seshInformationFetched;

    public static abstract class PrereqsFulfilledListener {
        public abstract void onPrereqsFulfilled();
    }

    public LaunchPrerequisiteAsyncTask(Context context, PrereqsFulfilledListener listener) {
        this.mContext = context;
        this.listener = listener;
        locationManagerLoaded = false;
        constantsFetched = false;
        seshInformationFetched = false;
    }

    public LaunchPrerequisiteAsyncTask(Context context, Set<LaunchPrerequisiteFlag> launchPrerequisitesFulfilled,
                                       PrereqsFulfilledListener listener) {
        this.mContext = context;
        this.listener = listener;
        locationManagerLoaded = false;
        constantsFetched = false;
        seshInformationFetched = false;

        if (launchPrerequisitesFulfilled.contains(LaunchPrerequisiteFlag.CONSTANTS_FETCHED)) {
            constantsFetched = true;
        } else {
            constantsFetched = false;
        }

        if (launchPrerequisitesFulfilled.contains(LaunchPrerequisiteFlag.LOCATION_MANAGER_LOADED)) {
            locationManagerLoaded = true;
        } else {
            locationManagerLoaded = false;
        }

        if (launchPrerequisitesFulfilled.contains(LaunchPrerequisiteFlag.SESH_INFORMATION_FETCHED)) {
            seshInformationFetched = true;
        } else {
            seshInformationFetched = false;
        }
    }

    /**
     *
     * @param params (Context context, Runnable runnable)
     * @return
     */
    @Override
    public Void doInBackground(Void... params) {
        if (!constantsFetched) {
            Constants.fetchConstantsFromServer(mContext);
        }

        if (!locationManagerLoaded) {
            LocationManager locationManager = LocationManager.sharedInstance(mContext);
            locationManager.setUp(this);

            synchronized (lock) {
                if (!locationManagerLoaded) {
                    try {
                        lock.wait(); // block until location manager is ready in onLocationManagerSetUp()
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            }
        }

        if (!seshInformationFetched) {
            final SeshNetworking seshNetworking = new SeshNetworking(mContext);
            SynchronousRequest request = new SynchronousRequest() {
                @Override
                public void request(RequestFuture<JSONObject> blocker) {
                    seshNetworking.getSeshInformation(blocker, blocker);
                }

                @Override
                public void onErrorException(Exception e) {
                    Log.e(TAG, "Failed to fetch user info from server; network error: " + e);
                }
            };

            JSONObject json = request.execute();

            if (json == null) return null;
            Sesh.updateSeshInfoWithObject(mContext, json);
        }

        return null;
    }

    @Override
    public void onPostExecute(Void result) {
        listener.onPrereqsFulfilled();
    }

    @Override
    public void onLocationManagerSetUp() {
        synchronized (lock) {
            locationManagerLoaded = true;
            lock.notify();
        }
    }
}
