package com.seshtutoring.seshapp.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/3/15.
 */
public class FetchSeshInfoBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = FetchSeshInfoBroadcastReceiver.class.getName();
    public static final String FETCH_SESH_INFO_ACTION = "com.seshtutoring.seshapp.services.FETCH_SESH_INFO";

    public interface SeshInfoUpdateListener {
        void onSeshInfoUpdate();
    }

    private static SeshInfoUpdateListener seshInfoUpdateListener;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (seshInfoUpdateListener == null) {
            Log.d(TAG, "SeshInfoUpdateListener is not yet set, delaying fetch until next alarm call.");
            return;
        }

        Log.d(TAG, "FIRING SESH INFO");
        SeshNetworking seshNetworking = new SeshNetworking(context);
        seshNetworking.getSeshInformation(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject json) {
                (new SaveSeshInfo()).execute(context, json);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to fetch user info from server; network error: " + volleyError);
            }
        });
    }

    private class SaveSeshInfo extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            Context context = (Context) params[0];
            JSONObject jsonObject = (JSONObject) params[1];
            Sesh.updateSeshInfoWithObject(context, jsonObject);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            seshInfoUpdateListener.onSeshInfoUpdate();
        }
    }

    public static void setSeshInfoUpdateListener(SeshInfoUpdateListener updateListener) {
        seshInfoUpdateListener = updateListener;
    }
}
