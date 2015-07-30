package com.seshtutoring.seshapp.services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by nadavhollander on 7/29/15.
 */
public class SeshInstanceIDListenerService extends InstanceIDListenerService {
    private static final String TAG = SeshInstanceIDListenerService.class.getName();

    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Log.i(TAG, "GCM requested token refresh");
        Intent intent = new Intent(this, SeshGCMListenerService.class);
        startService(intent);
    }
}
