package com.seshtutoring.seshapp.util.networking;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton wrapper for convenient use with the Volley networking library
 */
public class VolleyNetworkingWrapper {
    private static VolleyNetworkingWrapper mInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    private VolleyNetworkingWrapper(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyNetworkingWrapper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyNetworkingWrapper(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
