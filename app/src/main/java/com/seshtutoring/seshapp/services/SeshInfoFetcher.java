package com.seshtutoring.seshapp.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver.FetchUpdateListener;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/5/15.
 */
public class SeshInfoFetcher {
    private static final String TAG = SeshInfoFetcher.class.getName();
    private Context mContext;

    public static final String FETCH_TYPE_INFO = "info_fetch";

    public SeshInfoFetcher(Context context) {
        this.mContext = context;
    }

    public void fetch(final FetchUpdateListener listener) {
        SeshNetworking seshNetworking = new SeshNetworking(mContext);
        seshNetworking.getSeshInformation(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject json) {
                (new SaveSeshInfo()).execute(mContext, json, listener);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to fetch sesh info from server; network error: " + volleyError);
            }
        });
    }

    private class SaveSeshInfo extends AsyncTask<Object, Void, Void> {
       private FetchUpdateListener updateListener;

        @Override
        protected Void doInBackground(Object... params) {
            Context context = (Context) params[0];
            JSONObject jsonObject = (JSONObject) params[1];
            updateListener = (FetchUpdateListener) params[2];
            Sesh.updateSeshInfoWithObject(context, jsonObject);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateListener.onFetchUpdate();
        }
    }
}
