package com.seshtutoring.seshapp.util.networking;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonObject;
import com.stripe.android.Stripe;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lillioetting on 9/11/15.
 */
public class JsonMultipartRequest<T> extends Request<T> {

    private final static String BASIC_AUTH_USERNAME = "teamsesh";
    private final static String BASIC_AUTH_PASSWORD = "Eabltf1!";

    private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();
    private final Response.Listener<JSONObject> mListener;
    private final File yourImageFile;
    public Context mContext;
    protected Map<String, String> headers;
    protected Map<String, String> mParams;

    public JsonMultipartRequest(String url, Context mContext, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, File image) {
        super(Method.POST, url, errorListener);
        this.mContext = mContext;
        mListener = listener;
        yourImageFile = image;
        addImageEntity();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if (headers == null
                || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }
        //headers.put("Accept", "application/json");
        headers.put("Authorization", "Basic dGVhbXNlc2g6RWFibHRmMSE=");
        headers.put("X-Session-Id", SeshAuthManager.sharedManager(mContext).getAccessToken());
        return headers;
    }

    private void addImageEntity() {
        //mBuilder.addBinaryBody("file", yourImageFile, ContentType.create("image/jpeg"), yourImageFile.getName());
        mBuilder.addBinaryBody("profile_picture", yourImageFile, ContentType.create("image/jpeg"), "profile_picture");
        mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        mBuilder.setLaxMode().setBoundary("xx").setCharset(Charset.forName("UTF-8"));
    }

    @Override
    public String getBodyContentType() {
        String content = mBuilder.build().getContentType().getValue();
        return content;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mBuilder.build().writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream bos, building the multipart request.");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JSONObject jsonObj = new JSONObject(json);
            return Response.success((T)jsonObj, HttpHeaderParser.parseCacheHeaders(response));
        }catch (Exception e) {
            //do something
            return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        JSONObject jsonResponse = (JSONObject)response;
        mListener.onResponse(jsonResponse);
    }

}
