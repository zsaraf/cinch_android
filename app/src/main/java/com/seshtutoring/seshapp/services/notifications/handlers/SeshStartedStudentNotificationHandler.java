package com.seshtutoring.seshapp.services.notifications.handlers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.InSeshActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class SeshStartedStudentNotificationHandler extends NotificationHandler {
    public SeshStartedStudentNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    public void handle() {
        if (!ApplicationLifecycleTracker.sharedInstance(mContext).applicationInForeground()) {
            Intent intent;

            if (Sesh.getCurrentSesh() != null) {
                intent = new Intent(mContext, InSeshActivity.class);
            } else {
                intent = new Intent(mContext, MainContainerActivity.class);
            }

            showNotificationForIntent(intent);
        }

        mNotification.handled(mContext, true);
    }
}
