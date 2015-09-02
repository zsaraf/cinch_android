package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.SeshNetworking.SynchronousRequest;

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
        final int pastSeshId = (int) mNotification.getDataObject("past_sesh_id");
        final SeshNetworking seshNetworking = new SeshNetworking(mContext);

        SynchronousRequest request = new SynchronousRequest() {
            @Override
            public void request(RequestFuture<JSONObject> blocker) {
                seshNetworking.getPastSeshInformationForPastSeshId(pastSeshId, blocker, blocker);
            }

            @Override
            public void onErrorException(Exception e) {
                Log.e(TAG, "Failed to replace sesh with PastSesh: " + e);
                Notification.currentNotificationHandled(mContext, false);
            }
        };

        JSONObject jsonObject = request.execute();
        Log.d(TAG, "REPLACE SESH CALLED");
        if (jsonObject != null) {
            try {
                if (jsonObject.getString("status").equals("SUCCESS")) {
                    PastSesh pastSesh = PastSesh.createOrUpdatePastSesh(jsonObject.getJSONObject("past_sesh"));
                    List<Sesh> seshesFound = Sesh.find(Sesh.class, "past_request_id = ?",
                            Integer.toString(pastSesh.pastRequestId));
                    if (seshesFound.size() > 0) {
                        Log.d(TAG, "SESH HAS BEEN DELETED");
                        seshesFound.get(0).delete();
                    } else {
                        Log.d(TAG, "NO SESHES FOUND");
                        List<Sesh> seshesInGeneral = Sesh.listAll(Sesh.class);
                        Log.d(TAG, "seshes in DB: " + seshesInGeneral.size());
                    }
                    onSeshReplacedWithPastSesh(pastSesh);
                } else {
                    Log.e(TAG, "Failed to replace sesh with PastSesh; " + jsonObject.getString("message"));
                    Notification.currentNotificationHandled(mContext, false);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to replace sesh with PastSesh; json malformed: " + e);
                Notification.currentNotificationHandled(mContext, false);
            }
        }
    }

    protected void onSeshReplacedWithPastSesh(PastSesh pastSesh) {
        // do nothing
    }
}
