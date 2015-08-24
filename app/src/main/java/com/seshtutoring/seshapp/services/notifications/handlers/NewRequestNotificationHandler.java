package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.content.Intent;

import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class NewRequestNotificationHandler  extends NotificationHandler {
    public NewRequestNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    public void handle() {
        if (ApplicationLifecycleTracker.sharedInstance(mContext).applicationInForeground()) {
            displayBanner();
        } else {
            Intent intent = new Intent(MainContainerActivity.UPDATE_CONTAINER_STATE_ACTION, null,
                                mContext, MainContainerActivity.class);
            intent.putExtra(MainContainerActivity.MAIN_CONTAINER_STATE_INDEX, 0);
            intent.putExtra(MainContainerActivity.FRAGMENT_FLAG_KEY, HomeFragment.SHOW_AVAILABLE_JOBS_FLAG);
            showNotificationForIntent(intent);
        }

        mNotification.handled(mContext, true);
    }
}
