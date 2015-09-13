package com.seshtutoring.seshapp.util.networking;

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
    protected Map<String, String> headers;
    protected Map<String, String> mParams;

    public JsonMultipartRequest(String url, String sessionId, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String imageUri) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        yourImageFile = new File(imageUri);
        mBuilder.addTextBody("session_id", sessionId, ContentType.TEXT_PLAIN);
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
        headers.put("Authorization", String.format("Basic %s", Base64.encodeToString(
                String.format("%s:%s", BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD).getBytes(),
                Base64.NO_WRAP)));
        return headers;
    }

    private void addImageEntity() {
        //mBuilder.addBinaryBody("file", yourImageFile, ContentType.create("image/jpeg"), yourImageFile.getName());
        mBuilder.addBinaryBody("file", yourImageFile, ContentType.create("image/jpeg"), "file");
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
            //JSONObject jsonObj = new JSONObject(json);
            return Response.success((T)json, HttpHeaderParser.parseCacheHeaders(response));
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
