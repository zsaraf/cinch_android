package com.seshtutoring.seshapp.util.networking;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Request class that encapsulates most common request type for our usage of
 * Volley networking library.
 */
public class JsonObjectRequestWithAuth extends JsonObjectRequest {
    private final static String TAG = JsonObjectRequestWithAuth.class.getName();

    private final static String BASIC_AUTH_USERNAME = "teamsesh";
    private final static String BASIC_AUTH_PASSWORD = "Eabltf1!";

    public Map<String, String> mParams;
    public Context mContext;

    public JsonObjectRequestWithAuth(int method, String url, JSONObject params,
                                     Response.Listener<JSONObject> successListener,
                                     Response.ErrorListener errorListener) {
        super(method, url, params, successListener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
//        params.put("Authorization", String.format("Basic %s", Base64.encodeToString(
//                String.format("%s:%s", BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD).getBytes(),
//                Base64.NO_WRAP)));
        params.put("Authorization", "Basic dGVhbXNlc2g6RWFibHRmMSE=");
        params.put("X-Session-Id", SeshAuthManager.sharedManager(mContext).getAccessToken());
        Log.d(TAG, "USING HEADERS: " + params.toString());
        return params;
    }

    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String je = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            Object json = new JSONTokener(je).nextValue();
            if (json instanceof JSONObject) {
                return Response.success((JSONObject)json, null);
            } else {
                JSONObject object = new JSONObject();
                object.put("results", json);
                return Response.success(object, null);
            }
        } catch (UnsupportedEncodingException var3) {
            return Response.error(new ParseError(var3));
        } catch (JSONException var4) {
            String message = var4.getMessage();
            return Response.error(new ParseError(var4));
        }
    }
}
