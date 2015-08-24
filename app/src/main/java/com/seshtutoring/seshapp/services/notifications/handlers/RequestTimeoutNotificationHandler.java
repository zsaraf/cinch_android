package com.seshtutoring.seshapp.services.notifications.handlers;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.PastRequest;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class RequestTimeoutNotificationHandler extends NotificationHandler {
    private static final String TAG = RequestTimeoutNotificationHandler.class.getName();

    public RequestTimeoutNotificationHandler(Notification notification, SeshApplication application) {
        super(notification, application);
    }

    public void handle() {
        int pastRequestId = (int) mNotification.getDataObject("past_request_id");
        final int requestId = (int) mNotification.getDataObject("request_id");

        SeshNetworking seshNetworking = new SeshNetworking(mContext);
        seshNetworking.getPastRequestInformationForPastRequestId(pastRequestId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        LearnRequest currentLearnRequest =
                                LearnRequest.find(LearnRequest.class, "learn_request_id = ?", Integer.toString(requestId)).get(0);
                        currentLearnRequest.delete();

                        PastRequest.createOrUpdatePastRequest((JSONObject) mNotification.getDataObject("past_request"));

                        if (ApplicationLifecycleTracker.sharedInstance(mContext).applicationInForeground()) {
                            showDialog();
                        } else {
                            ApplicationLifecycleTracker.setApplicationResumeListener(new ApplicationLifecycleTracker.ApplicationResumeListener() {
                                @Override
                                public void onApplicationResume() {
                                    showDialog();
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "Failed to get past request information: " + jsonObject.getString("message"));
                        mNotification.handled(mContext, false);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to get past request information; json malformed: " + e);
                    mNotification.handled(mContext, false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to get past request information; network error: " + volleyError);
                mNotification.handled(mContext, false);
            }
        });
    }

    private void showDialog() {
        Log.e(TAG, "SHOWING DIALOG!");
        mNotification.handled(mContext, true);
    }
}