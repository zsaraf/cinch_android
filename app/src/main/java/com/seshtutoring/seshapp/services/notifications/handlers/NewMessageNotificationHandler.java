package com.seshtutoring.seshapp.services.notifications.handlers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.ImageView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.squareup.picasso.Callback;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class NewMessageNotificationHandler extends BannerNotificationHandler {
    public NewMessageNotificationHandler(Notification notification, Context context) {
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

    public Runnable bannerTapCallback() {
        return new Runnable() {
            @Override
            public void run() {
                Notification.currentNotificationHandled(mContext, true);
            }
        }; // todo implement when relevant
    }
}