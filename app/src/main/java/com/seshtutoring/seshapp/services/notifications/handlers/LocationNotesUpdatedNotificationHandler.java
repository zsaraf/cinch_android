package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.content.Intent;

import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragment;
import com.squareup.picasso.Callback;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class LocationNotesUpdatedNotificationHandler extends BannerNotificationHandler {
    public LocationNotesUpdatedNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    private void saveNewLocationNotes() {
        Sesh sesh = mNotification.correspondingSesh();
        sesh.locationNotes = (String) mNotification.getDataObject("location_notes");
        sesh.save();
    }

    @Override
    public void handleDisplayInsideApp() {
        saveNewLocationNotes();
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
        saveNewLocationNotes();
        showNotificationForIntent(viewSeshActionIntent(false, mNotification.correspondingSesh()));
        mNotification.handled(mContext, true);
    }

    public Runnable bannerTapCallback() {
        return new Runnable() {
            @Override
            public void run() {
                mContext.sendBroadcast(viewSeshActionIntent(true, mNotification.correspondingSesh()));
                mNotification.handled(mContext, true);
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
