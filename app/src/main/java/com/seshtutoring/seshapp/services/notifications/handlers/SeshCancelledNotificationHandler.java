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
        applicationLifecycleTracker = ApplicationLifecycleTracker.sharedInstance(context);
    }

    @Override
    public void handleDisplayInsideApp() {
        replaceSeshWithPastSesh();
        Intent intent = new Intent(MainContainerActivity.SESH_CANCELLED_ACTION);
        mContext.sendBroadcast(intent);
        showDialog(false);
    }

    private void showDialog(boolean withOpenDelay) {
        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setTitle(mNotification.title);
        seshDialog.setMessage(mNotification.message);
        seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
        seshDialog.setFirstChoice("OKAY");
        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seshDialog.dismiss();
                Notification.currentNotificationHandled(mContext, true);
            }
        });
        seshDialog.setType(DIALOG_TYPE_SESH_CANCELLED);

        if (withOpenDelay) {
            seshDialog.showWithDelay(
                    applicationLifecycleTracker.getActivityInForeground().getFragmentManager(),
                    DIALOG_TYPE_SESH_CANCELLED, 2000
            );
        } else {
            seshDialog.show(
                    applicationLifecycleTracker.getActivityInForeground().getFragmentManager(), DIALOG_TYPE_SESH_CANCELLED
            );
        }
    }
}