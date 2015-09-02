package com.seshtutoring.seshapp.services.notifications.handlers;


import android.content.Context;

import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.model.Notification;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class UpdateStateNotificationHandler extends NotificationHandler {
    public UpdateStateNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    @Override
    public void handleDisplayInsideApp() {
        SeshStateManager.sharedInstance(mContext).updateSeshState((String) mNotification.getDataObject("state"));
        SeshStateManager seshStateManager = SeshStateManager.sharedInstance(mContext);
        seshStateManager.displayActivityForSeshStateUpdate();

        Notification.currentNotificationHandled(mContext, true);
    }

    @Override
    public void handleDisplayOutsideApp() {
        // don't display external notification
    }
}
