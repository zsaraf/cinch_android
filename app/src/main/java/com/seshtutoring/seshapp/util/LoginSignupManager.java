package com.seshtutoring.seshapp.util;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/9/15.
 */
public class LoginSignupManager {
    private Context mContext;
    private SeshNetworking seshNetworking;

    public LoginSignupManager(Context context) {
        this.mContext = context;
        this.seshNetworking = new SeshNetworking(mContext);
    }

    public void attemptLogin(String email, String password, Response.Listener<JSONObject> successListener,
                             final Response.ErrorListener errorListener) {
        seshNetworking.loginWithEmail(email, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                logUserInOnClient(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                errorListener.onErrorResponse(volleyError);
            }
        });
    }

    public void logUserInOnClient(JSONObject userJson) {

    }
}
