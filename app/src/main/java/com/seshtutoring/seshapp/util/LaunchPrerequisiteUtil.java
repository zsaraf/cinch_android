package com.seshtutoring.seshapp.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.model.Constants;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/3/15.
 */
public class LaunchPrerequisiteUtil {
    private static final String TAG = LaunchPrerequisiteUtil.class.getName();

    public static void asyncPrepareForLaunch(final Context context, final Runnable callback) {
        Constants.fetchConstantsFromServer(context);
        final Handler handler = new Handler(Looper.getMainLooper());

        SeshNetworking seshNetworking = new SeshNetworking(context);
        seshNetworking.getSeshInformation(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject json) {
                Sesh.updateSeshInfoWithObject(context, json);
                handler.post(callback);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to fetch user info from server; network error: " + volleyError);
                handler.post(callback);
            }
        });
    }
}
