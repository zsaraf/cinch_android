package com.seshtutoring.seshapp.services.notifications.handlers;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Notification.NotificationType;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.services.UserInfoFetcher;
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
    public void handleDisplayInsideApp() {
        replaceSeshWithPastSesh();
        UserInfoFetcher userInfoFetcher = new UserInfoFetcher(mContext);
        userInfoFetcher.fetch();
    }

    @Override
    public void handleDisplayOutsideApp() {
        if (mNotification.getNotificationType() == NotificationType.SESH_REVIEW_STUDENT) {
            super.handleDisplayOutsideApp();
        }
    }

    @Override
    public void onSeshReplacedWithPastSesh(PastSesh pastSesh) {
        ApplicationLifecycleTracker applicationLifecycleTracker
                = ApplicationLifecycleTracker.sharedInstance(mContext);
        startReviewActivity(applicationLifecycleTracker.getActivityInForeground(), pastSesh);
    }

    private void startReviewActivity(Activity foregroundActivity, PastSesh pastSesh) {
        Intent intent;
        if (mNotification.getNotificationType() == NotificationType.SESH_REVIEW_STUDENT) {
            intent = new Intent(mContext, RatingActivity.class);
            intent.putExtra(TutorReviewActivity.PAST_SESH_ID, pastSesh.pastSeshId);
        } else {
            intent = new Intent(mContext, TutorReviewActivity.class);
            intent.putExtra(TutorReviewActivity.PAST_SESH_ID, pastSesh.pastSeshId);
        }
        foregroundActivity.startActivity(intent);
        foregroundActivity.overridePendingTransition(R.anim.slide_up, R.anim.hold);
    }
}
