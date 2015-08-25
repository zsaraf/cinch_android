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
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class SeshCancelledNotificationHandler extends SeshEndedNotificationHandler{
    private ApplicationLifecycleTracker applicationLifecycleTracker;
    public final static String DIALOG_TYPE_SESH_CANCELLED = "sesh_cancelled";

    public SeshCancelledNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    @Override
    public void handle() {
        replaceSeshWithPastSesh();
    }

    @Override
    public void onSeshReplacedWithPastSesh() {
        this.applicationLifecycleTracker =
                ApplicationLifecycleTracker.sharedInstance(mContext);
        if (applicationLifecycleTracker.applicationInForeground()) {
            Intent intent = new Intent(MainContainerActivity.SESH_CANCELLED_ACTION);
            mContext.sendBroadcast(intent);
            showDialog();
        } else {
            showNotificationForIntent(new Intent(mContext, MainContainerActivity.class));
            applicationLifecycleTracker.setApplicationResumeListener(
                    new ApplicationLifecycleTracker.ApplicationResumeListener() {
                @Override
                public void onApplicationResume() {
                    showDialog();
                }
            });
        }
    }

    private void showDialog() {
        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setTitle(mNotification.title);
        seshDialog.setMessage(mNotification.message);
        seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
        seshDialog.setFirstChoice("OKAY");
        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seshDialog.dismiss();
                mNotification.handled(mContext, true);
            }
        });
        seshDialog.setType(DIALOG_TYPE_SESH_CANCELLED);

        seshDialog.show(
                applicationLifecycleTracker.getActivityInForeground().getFragmentManager(), DIALOG_TYPE_SESH_CANCELLED
        );
    }
}