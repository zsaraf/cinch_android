package com.seshtutoring.seshapp.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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

import java.io.IOException;

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
    public static final String ANONYMOUS_TOKEN_REFRESH = "anonymous_token_refresh";

    private static final int MAX_RETRY_INTERVAL = 1000 * 32;

    private SharedPreferences gcmSharedPreferences;
    private AlarmManager alarmManager;
    private int retryInterval;
    private Intent intent;

    public GCMRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        gcmSharedPreferences = getSharedPreferences(GCM_STATUS_SHARED_PREFS, 0);

        // Device token cannot be refreshed without user being logged in
        if (!SeshAuthManager.sharedManager(getApplicationContext()).isValidSession()
                && !(intent.getBooleanExtra(ANONYMOUS_TOKEN_REFRESH, false))) return;

        this.intent = intent;

        if (intent.hasExtra(RETRY_INTERVAL_KEY)) {
            this.retryInterval = intent.getIntExtra(RETRY_INTERVAL_KEY, 1000);

            if (retryInterval > MAX_RETRY_INTERVAL) {
                return;
            }

            SystemClock.sleep(retryInterval);
        } else {
            this.retryInterval = 1000;
        }

        // Multiple sources in the application can try to refresh our server's current token at the same time
        // We ensure such requests only happen sequentially
        synchronized (TAG) {

            // IS_TOKEN_STALE determines if we need to refresh a registration token from Google
            // before sending it to the server.  If not stale and we have a cached token, we send the cached token.
            // For instance, at Login, we refresh our server's token with IS_TOKEN_STALE true, to account
            // for any tokenRefresh() calls from Google that might have been triggered while the user
            // was logged out (and didn't follow through because our implementation cannot refresh server
            // tokens when user's logged out).  On the other hand, every time MainContainer resumes,
            // we refresh with IS_TOKEN_STALE false, so that we can send a cached token, if it's available.
            if (!intent.getBooleanExtra(SeshInstanceIDListenerService.IS_TOKEN_STALE_KEY, true)) {
                if (gcmSharedPreferences.contains(GCM_TOKEN_KEY)) {
                    sendRegistrationToServer(gcmSharedPreferences.getString(GCM_TOKEN_KEY, null),
                            intent.getBooleanExtra(ANONYMOUS_TOKEN_REFRESH, false));
                    return;
                }
            }

            gcmSharedPreferences.edit().clear().apply();
            pullGCMRegistrationToken();
        }
    }

    private void pullGCMRegistrationToken() {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            tokenFound(token, gcmSharedPreferences);
        }  catch (Exception e) {
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
        if (intent.hasExtra(ANONYMOUS_TOKEN_REFRESH) && intent.getBooleanExtra(ANONYMOUS_TOKEN_REFRESH, false)) {
            sendRegistrationToServer(token, true);
        } else {
            sendRegistrationToServer(token, false);
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token, boolean anonymous) {
        SeshNetworking seshNetworking = new SeshNetworking(this);
        if (!anonymous) {
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
        } else {
            seshNetworking.updateAnonymousToken(token, new Response.Listener<JSONObject>() {
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
    }

    private void retryWithExponentialBackoff() {
        Log.d(TAG, "RETRY INTERVAL: " + retryInterval);

        retryInterval *= 2;

        intent.putExtra(RETRY_INTERVAL_KEY, retryInterval);
        startService(intent);
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
