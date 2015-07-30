package com.seshtutoring.seshapp.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/29/15.
 */
public class GCMRegistrationIntentService extends IntentService {

    private static final String TAG = GCMRegistrationIntentService.class.getName();

    public static final String GCM_STATUS_SHARED_PREFS = "gcm_status_shared_prefs";
    public static final String GCM_TOKEN_KEY = "gcm_token";
    public static final String GCM_REGISTRATION_ID_KEY = "registration_id";
    public static final String RETRY_INTERVAL_KEY = "retry_interval";
    public static final String TOKEN_REGISTRATION_COMPLETE = "registration_complete";

    private static final int MAX_RETRY_INTERVAL = 1000 * 32;

    private AlarmManager alarmManager;
    private int retryInterval;

    public GCMRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences gcmSharedPreferences = getSharedPreferences(GCM_STATUS_SHARED_PREFS, 0);

        // Device token cannot be refreshed without user being logged in
        if (!SeshAuthManager.sharedManager(getApplicationContext()).isValidSession()) return;

        if (intent.hasExtra(RETRY_INTERVAL_KEY)) {
            retryInterval = intent.getIntExtra(RETRY_INTERVAL_KEY, -1);
        } else {
            retryInterval = 1000;
        }

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.i(TAG, "GCM Registration Token: " + token);

                tokenFound(token, gcmSharedPreferences);
            }
        } catch (Exception e) {
            if (intent.hasExtra(GCM_REGISTRATION_ID_KEY)) {
//                fix for GCM bug where exception is thrown every time on certain devices
                tokenFound(intent.getStringExtra(GCM_REGISTRATION_ID_KEY), gcmSharedPreferences);
            } else {
                Log.d(TAG, "Failed to complete token refresh", e);
                // If an exception happens while fetching the new token or updating our registration data
                // on a third-party server, this ensures that we'll attempt the update at a later time.
                retryWithExponentialBackoff();
            }
        }
    }

    private void tokenFound(String token, SharedPreferences gcmSharedPreferences) {
        gcmSharedPreferences.edit().putString(GCM_TOKEN_KEY, token).apply();
        sendRegistrationToServer(token);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        SeshNetworking seshNetworking = new SeshNetworking(this);
        seshNetworking.updateDeviceToken(token, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        Log.i(TAG, "REGISTERED TOKEN TO SERVER");
                        Intent registrationComplete = new Intent(TOKEN_REGISTRATION_COMPLETE);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(registrationComplete);
                    } else {
                        Log.e(TAG, "GCM ERROR: Failed to update device token on server: " + jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "GCM ERROR: Failed to update device token on server; response malformed: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                retryWithExponentialBackoff();
            }
        });
    }

    private void retryWithExponentialBackoff() {
        if (retryInterval > MAX_RETRY_INTERVAL) return;

        Context context = getApplicationContext();

        Intent intent = new Intent(context, this.getClass());
        intent.putExtra(RETRY_INTERVAL_KEY, retryInterval * 2);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager  alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + retryInterval, alarmIntent);
    }

    public static void clearGCMRegistrationToken(Context context) {
        SharedPreferences gcmSharedPreferences = context.getSharedPreferences(GCM_STATUS_SHARED_PREFS, 0);
        gcmSharedPreferences.edit().clear().apply();
    }

    public static String getSavedGCMToken(Context context) {
        SharedPreferences gcmSharedPreferences = context.getSharedPreferences(GCM_STATUS_SHARED_PREFS, 0);
        if (gcmSharedPreferences.contains(GCM_TOKEN_KEY)) {
            return gcmSharedPreferences.getString(GCM_TOKEN_KEY, null);
        } else {
            return null;
        }
    }
}
