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
import java.util.Map;

/**
 * Created by nadavhollander on 7/7/15.
 */
public class SeshApplication extends Application {
    public static final boolean IS_LIVE = false;
    public static final boolean IS_DEV = true;

    private static final String TAG = SeshApplication.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
