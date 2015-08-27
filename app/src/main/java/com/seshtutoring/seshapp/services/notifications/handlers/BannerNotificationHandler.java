package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshBanner;
import com.squareup.picasso.Callback;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nadavhollander on 8/25/15.
 */
public abstract class BannerNotificationHandler extends NotificationHandler {
    protected CircleImageView profilePicture;

    public BannerNotificationHandler(Notification notification, Context context) {
        super(notification, context);

        LayoutUtils utils = new LayoutUtils(mContext);

        profilePicture = new CircleImageView(mContext);
        profilePicture.setBorderColorResource(R.color.seshorange);
        profilePicture.setBorderWidth(2);
        profilePicture.setLayoutParams(new ViewGroup.LayoutParams(utils.dpToPixels(50),
                utils.dpToPixels(50)));
    }

    @Override
    public void handleDisplayOutsideApp() {
        showNotificationForIntent(new Intent(mContext, MainContainerActivity.class));
        mNotification.handled(mContext, true);
    }

    // IF USING PROFILE PICTURE, MAKE SURE TO LOAD IMAGE INTO profilePicture FIRST
    protected void displayBanner() {
        SeshBanner seshBanner = SeshBanner.createBanner(6000,
                mNotification.title,
                mNotification.message,
                bannerImageView(),
                bannerTapCallback(),
                mNotification);

        seshBanner.show(
                ApplicationLifecycleTracker
                        .sharedInstance(mContext)
                        .getActivityInForeground()
                        .getFragmentManager(), null);
    }

    protected abstract Runnable bannerTapCallback();

    protected ImageView bannerImageView() {
        return profilePicture;
    }
}
