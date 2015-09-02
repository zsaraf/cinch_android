package com.seshtutoring.seshapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/3/15.
 */
public class PeriodicFetchBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = PeriodicFetchBroadcastReceiver.class.getName();
    public static final String PERIODIC_FETCH_ACTION = "com.seshtutoring.seshapp.services.PERIODIC_FETCH";
    public static final int FETCH_INTERVAL = 1000 * 15;




    @Override
    public void onReceive(final Context context, Intent intent) {
//        if (seshInfoUpdateListener == null) {
//            Log.d(TAG, "At least one updateListener is not yet set, delaying fetch until next alarm call.");
//            return;
//        }

//        SeshInfoFetcher infoFetcher = new SeshInfoFetcher(context);
//        infoFetcher.fetch(seshInfoUpdateListener);
    }

}
