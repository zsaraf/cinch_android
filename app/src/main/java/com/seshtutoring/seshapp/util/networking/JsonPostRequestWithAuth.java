package com.seshtutoring.seshapp.util.networking;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Request class that encapsulates most common request type for our usage of
 * Volley networking library.
 */
public class JsonPostRequestWithAuth extends StringRequest {
    private final static String TAG = JsonPostRequestWithAuth.class.getName();

    private final static String BASIC_AUTH_USERNAME = "teamsesh";
    private final static String BASIC_AUTH_PASSWORD = "Eabltf1!";

    public Map<String, String> mParams;

    public JsonPostRequestWithAuth(String url, Map<String, String> params,
                                   final Response.Listener<JSONObject> successListener,
                                   final Response.ErrorListener errorListener) {
        super(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject parsedJSON;
                    Log.i(TAG, "RAW JSON response: " + response);
                    parsedJSON = new JSONObject(response);
                    successListener.onResponse(parsedJSON);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON response.");
                    errorListener.onErrorResponse(new VolleyError(e));
                }
            }
        }, errorListener);

        this.mParams = params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Authorization", String.format("Basic %s", Base64.encodeToString(
                String.format("%s:%s", BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD).getBytes(),
                Base64.NO_WRAP)));
        params.put("Content-Type", "application/x-www-form-urlencoded");
        Log.d(TAG, "USING HEADERS: " + params.toString());
        return params;
    }

    @Override
    public Map<String, String> getParams() throws AuthFailureError {
        return this.mParams;
    }
}
