package com.seshtutoring.seshapp.services.notifications;

import android.content.Context;

import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.services.notifications.handlers.NotificationHandler;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class RefreshNotificationsNotificationHandler extends NotificationHandler {
    public RefreshNotificationsNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    public void handle() {
        // refresh notifications calls handled(true|false) upon success|failure
        Notification.refreshNotifications(mNotification, mContext);
    }
}
