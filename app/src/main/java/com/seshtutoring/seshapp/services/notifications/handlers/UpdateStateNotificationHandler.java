package com.seshtutoring.seshapp.services.notifications.handlers;


import android.content.Context;

import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker.ApplicationResumeListener;
import com.seshtutoring.seshapp.view.SeshActivity;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class UpdateStateNotificationHandler extends NotificationHandler {
    public UpdateStateNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    public void handle() {
        SeshStateManager.sharedInstance(mContext).updateSeshState((String) mNotification.getDataObject("state"));

        ApplicationLifecycleTracker applicationLifecycleTracker
                = ApplicationLifecycleTracker.sharedInstance(mContext);
        final SeshStateManager seshStateManager = SeshStateManager.sharedInstance(mContext);
        if (!applicationLifecycleTracker.applicationInForeground()) {
            ApplicationLifecycleTracker.setApplicationResumeListener(new ApplicationResumeListener() {
                @Override
                public void onApplicationResume() {
                    seshStateManager.displayActivityForSeshStateUpdate();
                }
            });
        } else {
            seshStateManager.displayActivityForSeshStateUpdate();
        }

        if (SeshStateManager.getCurrentSeshState(mContext) == SeshStateManager.SeshState.NONE) {
            mNotification.handled(mContext, true);
        }
    }
}
