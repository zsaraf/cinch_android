package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/23/15.
 */
public class Rate {
    private static final String TAG = Rate.class.getName();

    private static final String RATE_SHARED_PREFERENCES = "rate_shared_prefs";
    private static final String HOURLY_RATE_KEY = "hourly_rate";

    private float hourlyRate;

    public Rate(float hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public float getHourlyRate() {
        return hourlyRate;
    }

    public static Rate getCurrentHourlyRate(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(RATE_SHARED_PREFERENCES, 0);
        float hourlyRate = sharedPreferences.getFloat(HOURLY_RATE_KEY, -1.0f);
        if (hourlyRate < 0) {
            Log.e(TAG, "Hourly rate is not saved locally!");
            return null;
        }

        return new Rate(hourlyRate);
    }

    private static void setCurrentHourlyRate(Context context, float rate) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(RATE_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(HOURLY_RATE_KEY, rate);
        editor.commit();
    }

    public static void fetchHourlyRateFromServer(final Context context) {
        SeshNetworking seshNetworking = new SeshNetworking(context);
        seshNetworking.getCurrentRate(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        float hourlyRate = (float) jsonObject.getDouble("rate");
                        setCurrentHourlyRate(context, hourlyRate);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.getMessage());
                Log.e(TAG, "Failed to fetch hourly rate from server, check network.");
            }
        });
    }
}
