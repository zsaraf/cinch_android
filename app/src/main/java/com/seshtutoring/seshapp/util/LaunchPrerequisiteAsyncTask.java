package com.seshtutoring.seshapp.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.model.Constants;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.SeshNetworking.SynchronousRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
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
    private SeshStateManager seshStateManager;
    private Object lock = new Object();

    public enum LaunchPrerequisiteFlag {
        LOCATION_MANAGER_LOADED, CONSTANTS_FETCHED, USER_INFORMATION_FETCHED, SESH_STATE_VERIFIED
    }

    private boolean locationManagerLoaded;
    private boolean constantsFetched;
    private boolean userInformationFetched;
    private boolean seshStateVerified;

    private boolean launchIntoSesh;

    public static abstract class PrereqsFulfilledListener {
        public abstract void onPrereqsFulfilled();
    }

    public LaunchPrerequisiteAsyncTask(Context context, PrereqsFulfilledListener listener) {
        this.mContext = context;
        this.listener = listener;
        locationManagerLoaded = false;
        constantsFetched = false;
        userInformationFetched = false;
        seshStateVerified = false;
        seshStateManager = SeshStateManager.sharedInstance(mContext);
    }

    public LaunchPrerequisiteAsyncTask(Context context, Set<LaunchPrerequisiteFlag> launchPrerequisitesFulfilled,
                                       PrereqsFulfilledListener listener) {
        this.mContext = context;
        this.listener = listener;
        locationManagerLoaded = false;
        constantsFetched = false;
        userInformationFetched = false;
        seshStateVerified = false;
        seshStateManager = SeshStateManager.sharedInstance(mContext);

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

        if (launchPrerequisitesFulfilled.contains(LaunchPrerequisiteFlag.USER_INFORMATION_FETCHED)) {
            userInformationFetched = true;
        } else {
            userInformationFetched = false;
        }

        if (launchPrerequisitesFulfilled.contains(LaunchPrerequisiteFlag.SESH_STATE_VERIFIED)) {
            seshStateVerified = true;
        } else {
            seshStateVerified = false;
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

        if (!userInformationFetched) {
            final SeshNetworking seshNetworking = new SeshNetworking(mContext);
            SynchronousRequest request = new SynchronousRequest() {
                @Override
                public void request(RequestFuture<JSONObject> blocker) {
                    seshNetworking.getFullUserInfo(blocker, blocker);
                }

                @Override
                public void onErrorException(Exception e) {
                    Log.e(TAG, "Failed to fetch user info from server; network error: " + e);
                }
            };

            JSONObject json = request.execute();

            if (json == null) return null;

            try  {
                User.createOrUpdateUserWithObject(json.getJSONObject("data"), mContext);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to fetch user info; json malformed: " + e);
            }
        }

        if (!seshStateVerified) {
            launchIntoSesh = false;

            User user = User.currentUser(mContext);

            List<Sesh> openSeshes = user.getOpenSeshes();
            for (Sesh sesh : openSeshes) {
                if (sesh.hasStarted) {
                    launchIntoSesh = true;
                    if (SeshStateManager.getCurrentSeshState(mContext) != SeshStateManager.SeshState.IN_SESH) {
                        seshStateManager.updateSeshState(SeshStateManager.SeshState.IN_SESH);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void onPostExecute(Void result) {
        if (!launchIntoSesh) {
            listener.onPrereqsFulfilled();
        } else {
            seshStateManager.displayActivityForSeshStateUpdate();
        }
    }

    @Override
    public void onLocationManagerSetUp() {
        synchronized (lock) {
            locationManagerLoaded = true;
            lock.notify();
        }
    }
}
