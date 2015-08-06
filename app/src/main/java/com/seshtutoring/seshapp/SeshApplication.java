package com.seshtutoring.seshapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orm.SugarApp;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by nadavhollander on 7/7/15.
 */
public class SeshApplication extends SugarApp {
    private static final String TAG = SeshApplication.class.getName();
    private ApplicationLifecycleTracker applicationLifecycleTracker;

    public static final boolean IS_LIVE = false;
    public static final boolean IS_DEV = true;

    @Override
    public void onCreate() {
        super.onCreate();

        this.applicationLifecycleTracker = new ApplicationLifecycleTracker(this);

        // initialize default font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Gotham-Light.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    public ApplicationLifecycleTracker getApplicationLifecycleTracker() {
        return applicationLifecycleTracker;
    }
}
