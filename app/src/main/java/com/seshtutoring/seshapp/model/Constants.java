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
 * Created by nadavhollander on 8/3/15.
 */
public class Constants {
    private static final String TAG = Constants.class.getName();
    private static final String CONSTANTS_SHARED_PREFS = "constants";
    private static final String HOURLY_RATE_KEY = "hourly_rate";
    private static final String USER_SHARE_KEY = "user_share";
    private static final String FRIEND_SHARE_KEY = "friend_share";
    private static final String INSTANT_REQUEST_TIMEOUT_KEY = "instant_timeout";


    public static void fetchConstantsFromServer(final Context context) {
        SeshNetworking seshNetworking = new SeshNetworking(context);
        seshNetworking.getConstants(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        float hourlyRate = (float) jsonObject.getDouble("rate");
                        float userShareRate = (float) jsonObject.getDouble("user_share_rate");
                        float friendShareRate = (float) jsonObject.getDouble("friend_share_rate");
                        int instantRequestTimeout = jsonObject.getInt("instant_request_timeout");

                        SharedPreferences sharedPreferences =
                                context.getSharedPreferences(CONSTANTS_SHARED_PREFS, 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putFloat(HOURLY_RATE_KEY, hourlyRate);
                        editor.putFloat(USER_SHARE_KEY, userShareRate);
                        editor.putFloat(FRIEND_SHARE_KEY, friendShareRate);
                        editor.putInt(INSTANT_REQUEST_TIMEOUT_KEY, instantRequestTimeout);
                        editor.apply();
                    } else {
                        Log.e(TAG, "Failed to fetch constants from server: " + jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to fetch constants from server; response malformed: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.toString());
            }
        });
    }

    public static float getHourlyRate(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(CONSTANTS_SHARED_PREFS, 0);
        return sharedPreferences.getFloat(HOURLY_RATE_KEY, -1f);
    }

    public static float getUserShareRate(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(CONSTANTS_SHARED_PREFS, 0);
        return sharedPreferences.getFloat(USER_SHARE_KEY, -1f);
    }

    public static float getFriendShareRate(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(CONSTANTS_SHARED_PREFS, 0);
        return sharedPreferences.getFloat(FRIEND_SHARE_KEY, -1f);
    }

    public static int getInstantRequestTimeout(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(CONSTANTS_SHARED_PREFS, 0);
        return sharedPreferences.getInt(INSTANT_REQUEST_TIMEOUT_KEY, -1);
    }
}
