package com.seshtutoring.seshapp.services.notifications.handlers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class NewMessageNotificationHandler  extends NotificationHandler {
    public NewMessageNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    public void handle() {
        // @TODO: Implement when messaging is relevant

        if (!ApplicationLifecycleTracker.sharedInstance(mContext).applicationInForeground()) {
            showNotificationForIntent(new Intent(mContext, MainContainerActivity.class));
        } else {
            displayBanner();
        }

        mNotification.handled(mContext, true);
    }
}