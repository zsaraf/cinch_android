package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 8/24/15.
 */
public abstract class SeshEndedNotificationHandler extends NotificationHandler {
    private static final String TAG = SeshEndedNotificationHandler.class.getName();

    public SeshEndedNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    protected void replaceSeshWithPastSesh() {
        int pastSeshId = (int) mNotification.getDataObject("past_sesh_id");
        SeshNetworking seshNetworking = new SeshNetworking(mContext);
        seshNetworking.getPastSeshInformationForPastSeshId(pastSeshId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        PastSesh pastSesh = PastSesh.createOrUpdatePastSesh(jsonObject.getJSONObject("past_sesh"));
                        List<Sesh> seshesFound = Sesh.find(Sesh.class, "past_request_id = ?",
                                Integer.toString(pastSesh.pastRequestId));
                        if (seshesFound.size() > 1) {
                            seshesFound.get(0).delete();
                        }
                        onSeshReplacedWithPastSesh();
                    } else {
                        Log.e(TAG, "Failed to replace sesh with PastSesh; " + jsonObject.getString("message"));
                        mNotification.handled(mContext, false);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to replace sesh with PastSesh; json malformed: " + e);
                    mNotification.handled(mContext, false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to replace sesh with PastSesh; network error: " + volleyError);
                mNotification.handled(mContext, false);
            }
        });
    }

    protected abstract void onSeshReplacedWithPastSesh();
}
