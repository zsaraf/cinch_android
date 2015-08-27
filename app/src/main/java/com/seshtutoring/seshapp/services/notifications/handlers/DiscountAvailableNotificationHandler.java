package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.LayoutUtils;

/**
 * Created by nadavhollander on 8/26/15.
 */
public class DiscountAvailableNotificationHandler extends BannerNotificationHandler {
    public DiscountAvailableNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    @Override
    public void handleDisplayInsideApp() {
        displayBanner();
    }

    @Override
    protected Runnable bannerTapCallback() {
        return new Runnable() {
            @Override
            public void run() {
                // do nothing
            }
        };
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
