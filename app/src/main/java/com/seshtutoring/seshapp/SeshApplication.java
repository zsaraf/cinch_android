package com.seshtutoring.seshapp;

import android.app.Application;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.db.UserDbHelper;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by nadavhollander on 7/7/15.
 */
public class SeshApplication extends Application {
    public static final boolean IS_LIVE = true;

    private static final String TAG = SeshApplication.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();

        SeshNetworking networkAPIWrapper = new SeshNetworking(this);
        JSONObject params = new JSONObject();
        try {
            params.put("format", "json");
            params.put("email", "loetting@stanford.edu");
            params.put("password", "lco1430");
        } catch (JSONException e) {
            Log.e(TAG, "EWEKRJWK");
        }

        networkAPIWrapper.postWithRelativeUrl("login.php", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });
    }
}
