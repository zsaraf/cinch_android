package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class NewRequestNotificationHandler extends BannerNotificationHandler {
    public NewRequestNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    @Override
    public void handleDisplayInsideApp() {
        displayBanner();
    }

    @Override
    public void handleDisplayOutsideApp() {
        showNotificationForIntent(showAvailableJobsIntent(false));
        Notification.currentNotificationHandled(mContext, true);
    }

    @Override
    protected Runnable bannerTapCallback() {
        return new Runnable() {
            @Override
            public void run() {
                mContext.sendBroadcast(showAvailableJobsIntent(true));
            }
        };
    }

    private Intent showAvailableJobsIntent(boolean forBroadcast) {
        Intent intent;
        if (forBroadcast) {
            intent = new Intent(MainContainerActivity.UPDATE_CONTAINER_STATE_ACTION);
        } else {
            intent = new Intent(MainContainerActivity.UPDATE_CONTAINER_STATE_ACTION, null,
                    mContext, MainContainerActivity.class);
        }
        intent.putExtra(MainContainerActivity.MAIN_CONTAINER_STATE_INDEX, 0);
        intent.putExtra(MainContainerActivity.FRAGMENT_FLAG_KEY, HomeFragment.SHOW_AVAILABLE_JOBS_FLAG);
        return intent;
    }

    @Override
    protected ImageView bannerImageView() {
        ImageView imageView = new ImageView(mContext);
        if (Build.VERSION.SDK_INT < 21) {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.moneysign_big));
        } else {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.moneysign_big, null));
        }

        LayoutUtils utils = new LayoutUtils(mContext);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(utils.dpToPixels(30), utils.dpToPixels(30)));

        return imageView;
    }
}
