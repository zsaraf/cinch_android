package com.seshtutoring.seshapp.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/30/15.
 */
public class ApplicationLifecycleTracker  {
    private static final String TAG = ApplicationLifecycleTracker.class.getName();
    private static boolean someActivityInForeground;

    public static void activityResumed(Context context) {
        someActivityInForeground = true;
    }

    public static void activityPaused(Context context) {
        someActivityInForeground = false;
    }

    public static boolean applicationInForeground() {
        return someActivityInForeground;
    }
}
