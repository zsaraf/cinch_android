package com.seshtutoring.seshapp.util.networking;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Response;
import com.seshtutoring.seshapp.SeshApplication;

import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Wrapper for any asynchronous interactions with the API.
 */
public class SeshNetworking {
    private static final String TAG = SeshNetworking.class.getName();

    private Context mContext;

    public SeshNetworking(Context context) {
        this.mContext = context;
    }

    public void postWithRelativeUrl(String relativeUrl, Map<String, String> params,
                                          Response.Listener<JSONObject> successListener,
                                          Response.ErrorListener errorListener) {
        String absoluteUrl = baseUrl() + relativeUrl;

        Log.i(TAG, "Issuing POST request to URL:  " + absoluteUrl + "with params: " +
                params.toString());
        JsonPostRequestWithAuth requestWithAuth = new JsonPostRequestWithAuth(absoluteUrl,
                params, successListener, errorListener);
        Log.i(TAG, "Params body looks like this:\n" +  requestWithAuth.getBody());


        VolleyNetworkingWrapper.getInstance(mContext).addToRequestQueue(requestWithAuth);
    }

    private String baseUrl() {
        String baseUrl;
        if (SeshApplication.IS_LIVE) {
            baseUrl = "https://www.seshtutoring.com/ios-php/";
        } else {
            baseUrl = "https://www.cinchtutoring.com/ios-php/";
        }
        return baseUrl;
    }
}
