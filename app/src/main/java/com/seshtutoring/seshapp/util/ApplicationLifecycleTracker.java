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

    public static void applicationResumed(Context context) {
        User.fetchUserInfoFromServer(context);
        updateDeviceOnServer(context);
    }

    /**
     * Updates the Device row of the server database for user's phone.  Called everytime user
     * opens application (onResume).  Will not send update request if we do not have a GCM registration
     * token locally saved -- device row data is only used to push information (via GCM), so any
     * update would be irrelevant if user device does not have a valid GCM token.
     */
    private static void updateDeviceOnServer(Context context) {
        String savedToken = GCMRegistrationIntentService.getSavedGCMToken(context);
        if (savedToken != null) {
            SeshNetworking seshNetworking = new SeshNetworking(context);
            seshNetworking.updateDeviceToken(savedToken, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        if (jsonObject.getString("status").equals("SUCCESS")) {
                            Log.i(TAG, "Updated device on server");
                        } else {
                            Log.e(TAG, "Failed to update device on server: " + jsonObject.getString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to update device on server; response malformed: " + e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Failed to update device on server; network error: " + error);
                }
            });
        }
    }
}
