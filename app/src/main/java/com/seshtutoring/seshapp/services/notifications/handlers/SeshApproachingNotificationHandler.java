package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.content.Intent;

import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragment;
import com.squareup.picasso.Callback;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class SeshApproachingNotificationHandler  extends BannerNotificationHandler {
    public SeshApproachingNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    @Override
    public void handleDisplayInsideApp() {
        loadImage(profilePicture, new Callback() {
            @Override
            public void onSuccess() {
                    displayBanner();
                }
            @Override
            public void onError() {
                    displayBanner();
                }
        });
    }

    @Override
    public void handleDisplayOutsideApp() {
        showNotificationForIntent(viewSeshActionIntent(false));
    }

    public Runnable bannerTapCallback() {
        return new Runnable() {
            @Override
            public void run() {
                mContext.sendBroadcast(viewSeshActionIntent(true));
                Notification.currentNotificationHandled(mContext, true);
            }
        };
    }

    private Intent viewSeshActionIntent(boolean forBroadcast) {
        Intent intent;
        if (forBroadcast) {
            intent = new Intent(MainContainerActivity.VIEW_SESH_ACTION);
        } else {
            intent = new Intent(MainContainerActivity.VIEW_SESH_ACTION,
                    null, mContext, MainContainerActivity.class);
        }
        intent.putExtra(ViewSeshFragment.SESH_KEY, mNotification.correspondingSesh().seshId);
        return intent;
    }
}
