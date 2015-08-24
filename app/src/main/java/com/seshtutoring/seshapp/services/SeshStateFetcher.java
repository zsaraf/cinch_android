package com.seshtutoring.seshapp.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.SeshStateManager.SeshState;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver.FetchUpdateListener;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

public class SeshStateFetcher {
    private static final String TAG = SeshStateFetcher.class.getName();
    private Context mContext;
    private SeshState currState;

    public static final String FETCH_TYPE_STATE = "state_fetch";

    public SeshStateFetcher(Context context) {
        this.mContext = context;
        this.currState = SeshStateManager.getCurrentSeshState(context);
    }

    public void fetch(final FetchUpdateListener listener) {
        SeshNetworking seshNetworking = new SeshNetworking(mContext);
        seshNetworking.verifySeshStateWithSeshStateIdentifier(currState.identifier, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        boolean invalidated = (currState != SeshStateManager.getCurrentSeshState(mContext));
                        boolean changeNeeded = (jsonObject.getInt("change_needed") == 1);
                        if (changeNeeded && !invalidated) {
                            String updatedState = jsonObject.getString("state");
                            SeshStateManager.sharedInstance(mContext).updateSeshState(updatedState);
                            currState = SeshStateManager.getCurrentSeshState(mContext);

                            if (jsonObject.has("sesh_information")) {
                                (new SaveSeshInfo()).execute(mContext, jsonObject.getString("sesh_information"), listener);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

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
            Sesh.createOrUpdateSeshWithObject(jsonObject, context);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateListener.onFetchUpdate();
        }
    }
}
