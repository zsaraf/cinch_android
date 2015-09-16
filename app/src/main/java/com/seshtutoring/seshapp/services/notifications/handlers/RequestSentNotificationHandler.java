package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.content.Intent;

import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.SeshActivity;

/**
 * Created by nadavhollander on 9/15/15.
 */
public class RequestSentNotificationHandler extends NotificationHandler {
    public RequestSentNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    @Override
    public void handleDisplayInsideApp() {
        SeshActivity foregroundActivity
                = (SeshActivity) ApplicationLifecycleTracker.sharedInstance(mContext).getActivityInForeground();

        Intent intent;
        if (foregroundActivity.isMainContainerActivity()) {
            intent = new Intent(MainContainerActivity.REQUEST_SENT_ACTION);
            mContext.sendBroadcast(intent);
        } else {
            intent = new Intent(MainContainerActivity.REQUEST_SENT_ACTION, null,
                    mContext, MainContainerActivity.class);
            foregroundActivity.startActivity(intent);
        }
    }
}
