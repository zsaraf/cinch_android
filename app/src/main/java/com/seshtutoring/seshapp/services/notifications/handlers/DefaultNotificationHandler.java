package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;

import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Notification;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class DefaultNotificationHandler extends NotificationHandler {
    public DefaultNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    @Override
    public void handleDisplayInsideApp() {
        Notification.currentNotificationHandled(mContext, true);
    }
}