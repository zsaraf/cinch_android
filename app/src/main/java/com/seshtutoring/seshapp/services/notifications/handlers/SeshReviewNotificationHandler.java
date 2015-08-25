package com.seshtutoring.seshapp.services.notifications.handlers;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Notification.NotificationType;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.RatingActivity;
import com.seshtutoring.seshapp.view.TutorReviewActivity;

/**
 * Created by nadavhollander on 8/25/15.
 */
public class SeshReviewNotificationHandler extends SeshEndedNotificationHandler {
    private static final String TAG = SeshReviewNotificationHandler.class.getName();

    public SeshReviewNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    @Override
    public void handle() {
        replaceSeshWithPastSesh();
    }

    @Override
    public void onSeshReplacedWithPastSesh() {
        final ApplicationLifecycleTracker applicationLifecycleTracker
                = ApplicationLifecycleTracker.sharedInstance(mContext);
        if (applicationLifecycleTracker.applicationInForeground()) {
            startReviewActivity(applicationLifecycleTracker.getActivityInForeground());
        } else {
            showNotificationForIntent(new Intent(mContext, MainContainerActivity.class));
            applicationLifecycleTracker.setApplicationResumeListener(new ApplicationLifecycleTracker.ApplicationResumeListener() {
                @Override
                public void onApplicationResume() {
                    startReviewActivity(applicationLifecycleTracker.getActivityInForeground());
                }
            });
        }
    }

    private void startReviewActivity(Activity foregroundActivity) {
        Intent intent;
        if (mNotification.getNotificationType() == NotificationType.SESH_REVIEW_STUDENT) {
            intent = new Intent(mContext, RatingActivity.class);
        } else {
            intent = new Intent(mContext, TutorReviewActivity.class);
        }
        foregroundActivity.startActivity(intent);
        foregroundActivity.overridePendingTransition(R.anim.slide_up, R.anim.hold);
    }
}
