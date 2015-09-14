package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerStateManager;
import com.seshtutoring.seshapp.view.SeshActivity;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class SeshCancelledNotificationHandler extends SeshEndedNotificationHandler{
    private ApplicationLifecycleTracker applicationLifecycleTracker;
    public final static String SESH_CANCELLED_DIALOG_TITLE = "sesh_cancelled_dialog_title";
    public final static String SESH_CANCELLED_DIALOG_MESSAGE = "sesh_cancelled_dialog_message";

    public SeshCancelledNotificationHandler(Notification notification, Context context) {
        super(notification, context);
        applicationLifecycleTracker = ApplicationLifecycleTracker.sharedInstance(context);
    }

    @Override
    public void handleDisplayInsideApp() {
        replaceSeshWithPastSesh();
        SeshActivity foregroundActivity
                = (SeshActivity) applicationLifecycleTracker.getActivityInForeground();

        Intent intent;
        if (foregroundActivity.isMainContainerActivity()) {
             intent = new Intent(MainContainerActivity.SESH_CANCELLED_ACTION);
        } else {
            intent = new Intent(MainContainerActivity.SESH_CANCELLED_ACTION, null,
                    mContext, MainContainerActivity.class);
        }

        intent.putExtra(SESH_CANCELLED_DIALOG_TITLE, mNotification.title);
        intent.putExtra(SESH_CANCELLED_DIALOG_MESSAGE, mNotification.message);

        if (foregroundActivity.isMainContainerActivity()) {
            mContext.sendBroadcast(intent);
        } else {
            foregroundActivity.startActivity(intent);
        }
    }
}