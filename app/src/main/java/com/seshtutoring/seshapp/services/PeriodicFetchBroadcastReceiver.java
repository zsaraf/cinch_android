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

    public interface FetchUpdateListener {
        void onFetchUpdate(String type);
    }

    private static FetchUpdateListener seshInfoUpdateListener;
    private static FetchUpdateListener seshStateUpdateListener;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (seshInfoUpdateListener == null || seshStateUpdateListener == null) {
            Log.d(TAG, "At least one updateListener is not yet set, delaying fetch until next alarm call.");
            return;
        }

        SeshStateFetcher stateFetcher = new SeshStateFetcher(context);
        SeshInfoFetcher infoFetcher = new SeshInfoFetcher(context);
        stateFetcher.fetch(seshStateUpdateListener);
        infoFetcher.fetch(seshInfoUpdateListener);
    }

    public static void setSeshInfoUpdateListener(FetchUpdateListener updateListener) {
        seshInfoUpdateListener = updateListener;
    }

    public static void setSeshStateUpdateListener(FetchUpdateListener updateListener) {
        seshStateUpdateListener = updateListener;
    }
}
