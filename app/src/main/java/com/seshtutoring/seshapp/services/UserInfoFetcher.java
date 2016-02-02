package com.seshtutoring.seshapp.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/5/15.
 */
public class UserInfoFetcher {
    private static final String TAG = UserInfoFetcher.class.getName();
    private Context mContext;

    public UserInfoFetcher(Context context) {
        this.mContext = context;
    }

    public static abstract class UserInfoSavedListener {
        public abstract void onUserInfoSaved(User user);
    }

    public void fetch(final UserInfoSavedListener listener) {
        SeshNetworking seshNetworking = new SeshNetworking(mContext);
        seshNetworking.getFullUserInfo(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject json) {
                (new SaveUserInfoAsyncTask()).execute(mContext, json, listener);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to fetch user info from server; network error: " + volleyError);
            }
        });
    }

    /**
     * Convenience method for fetching without an update listener
     */
    public void fetch() {
        fetch(new UserInfoSavedListener() {
            @Override
            public void onUserInfoSaved(User user) {
                // do nothing
            }
        });
    }

    public static class SaveUserInfoAsyncTask extends AsyncTask<Object, Void, Void> {
        private UserInfoSavedListener updateListener;
        private User user;

        @Override
        protected Void doInBackground(Object... params) {
            Context context = (Context) params[0];
            JSONObject jsonObject = (JSONObject) params[1];
            updateListener = (UserInfoSavedListener) params[2];
            user = User.createOrUpdateUserWithObject(jsonObject, context);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateListener.onUserInfoSaved(user);
        }
    }
}
