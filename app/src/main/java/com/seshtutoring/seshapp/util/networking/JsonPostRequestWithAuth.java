package com.seshtutoring.seshapp.util.networking;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Request class that encapsulates most common request type for our usage of
 * Volley networking library.
 */
public class JsonPostRequestWithAuth extends JsonObjectRequest {
    private final static String TAG = JsonPostRequestWithAuth.class.getName();

    private final static String BASIC_AUTH_USERNAME = "teamsesh";
    private final static String BASIC_AUTH_PASSWORD = "Eabltf1!";

    public Map<String, String> mParams;

    public JsonPostRequestWithAuth(String url, JSONObject params,
                                   Response.Listener<JSONObject> successListener,
                                   Response.ErrorListener errorListener) {
        super(Method.POST, url, params, successListener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Authorization", String.format("Basic %s", Base64.encodeToString(
                String.format("%s:%s", BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD).getBytes(),
                Base64.NO_WRAP)));
        Log.d(TAG, "USING HEADERS: " + params.toString());
        return params;
    }

//    @Override
//    public byte[] getBody() {
//        Map<String, String> params = mParams;
//        if (mParams != null && mParams.size() > 0) {
//            return encodeParameters(params, getParamsEncoding());
//        }
//        return null;
//    }
//
//    protected byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
//        StringBuilder encodedParams = new StringBuilder();
//        try {
//            for (Map.Entry<String, String> entry : params.entrySet()) {
//                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
//                encodedParams.append('=');
//                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
//                encodedParams.append('&');
//            }
//            return encodedParams.toString().getBytes(paramsEncoding);
//        } catch (UnsupportedEncodingException uee) {
//            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
//        }
//    }
}
