package com.seshtutoring.seshapp.services.notifications.handlers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.ImageView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Message;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragment;
import com.squareup.picasso.Callback;

import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class NewMessageNotificationHandler extends BannerNotificationHandler {
    public NewMessageNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    private void saveNewMessage() {
        Sesh sesh = mNotification.correspondingSesh();
        JSONObject messageJSON = (JSONObject) mNotification.getDataObject("message");
        Message message = Message.createOrUpdateMessageWithJSON(messageJSON, sesh, mContext);
        message.save();
    }

    @Override
    public void handleDisplayInsideApp() {
        saveNewMessage();
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
        saveNewMessage();
        showNotificationForIntent(viewSeshActionIntent(false, mNotification.correspondingSesh()));
        Notification.currentNotificationHandled(mContext, true);
    }

    public Runnable bannerTapCallback() {
        return new Runnable() {
            @Override
            public void run() {
                mContext.sendBroadcast(viewSeshActionIntent(true, mNotification.correspondingSesh()));
            }
        };
    }

    private Intent viewSeshActionIntent(boolean forBroadcast, Sesh sesh) {
        Intent intent;
        if (forBroadcast) {
            intent = new Intent(MainContainerActivity.VIEW_SESH_ACTION);
        } else {
            intent = new Intent(MainContainerActivity.VIEW_SESH_ACTION,
                    null, mContext, MainContainerActivity.class);
        }
        intent.putExtra(ViewSeshFragment.SESH_KEY, sesh.seshId);
        return intent;
    }
}