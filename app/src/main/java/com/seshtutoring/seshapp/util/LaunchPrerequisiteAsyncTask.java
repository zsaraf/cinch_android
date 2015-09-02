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

/**
 * Created by nadavhollander on 8/3/15.
 */
public class LaunchPrerequisiteAsyncTask extends AsyncTask<Object, Void, Void>
        implements LocationManager.LocationManagerSetUpListener {
    private static final String TAG = LaunchPrerequisiteAsyncTask.class.getName();

    private Context mContext;
    private Runnable callback;
    private Object lock = new Object();
    private boolean locationManagerSetUp = false;

    /**
     *
     * @param params (Context context, Runnable runnable)
     * @return
     */
    @Override
    public Void doInBackground(Object... params) {
        this.mContext = (Context) params[0];
        this.callback = (Runnable) params[1];

        Constants.fetchConstantsFromServer(mContext);

        LocationManager locationManager = LocationManager.sharedInstance(mContext);
        locationManager.setUp(this);

        synchronized (lock) {
            if (!locationManagerSetUp) {
                try {
                    lock.wait(); // block until location manager is ready in onLocationManagerSetUp()
                } catch (InterruptedException e) {
                    return null;
                }
            }
        }

        final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        final SeshNetworking seshNetworking = new SeshNetworking(mContext);
        SynchronousRequest request = new SynchronousRequest() {
            @Override
            public void request(RequestFuture<JSONObject> blocker) {
                seshNetworking.getSeshInformation(blocker, blocker);
            }

            @Override
            public void onErrorException(Exception e) {
                Log.e(TAG, "Failed to fetch user info from server; network error: " + e);
                mainThreadHandler.post(callback);
            }
        };

        JSONObject json = request.execute();

        if (json == null) return null;
        Sesh.updateSeshInfoWithObject(mContext, json);

        return null;
    }

    @Override
    public void onPostExecute(Void result) {
        callback.run();
    }

    @Override
    public void onLocationManagerSetUp() {
        synchronized (lock) {
            locationManagerSetUp = true;
            lock.notify();
        }
    }
}
