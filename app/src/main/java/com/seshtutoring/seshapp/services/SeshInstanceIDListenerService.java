package com.seshtutoring.seshapp.services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.seshtutoring.seshapp.SeshApplication;

/**
 * Created by nadavhollander on 7/29/15.
 */
public class SeshInstanceIDListenerService extends InstanceIDListenerService {
    private static final String TAG = SeshInstanceIDListenerService.class.getName();
    public static final String IS_TOKEN_STALE_KEY = "is_token_stale";

    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Log.i(TAG, "GCM requested token refresh");
        Intent intent = new Intent(this, GCMRegistrationIntentService.class);
        intent.putExtra(IS_TOKEN_STALE_KEY, true);
        startService(intent);
    }
}
