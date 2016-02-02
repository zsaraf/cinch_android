package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.content.Intent;

import com.seshtutoring.seshapp.model.Chatroom;
import com.seshtutoring.seshapp.model.ChatroomActivity;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MessagingActivity;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragment;
import com.squareup.picasso.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class NewMessageNotificationHandler extends BannerNotificationHandler {
    public NewMessageNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    private boolean saveNewMessage() {
        JSONObject messageJSON = (JSONObject) mNotification.getDataObject("chatroom_activity");

        Chatroom chatroom = null;

        try {
            List<Chatroom> list = Chatroom.find(Chatroom.class, "chatroom_id = ?", messageJSON.getInt("chatroom") + "");
            if (list.size() >= 1) {
                chatroom = list.get(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (chatroom != null) {
            ChatroomActivity chatroomActivity = ChatroomActivity.createOrUpdateChatroomActivityWithJSON(messageJSON, chatroom, mContext);
            if (chatroomActivity != null) {
                chatroomActivity.save();
                mContext.sendBroadcast(refreshMessagesIntent());
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void handleDisplayInsideApp() {
        boolean messageSaved = saveNewMessage();
        if (messageSaved && !(ApplicationLifecycleTracker.sharedInstance(mContext).activityInForeground instanceof MessagingActivity)) {
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
        } else {
            Notification.currentNotificationHandled(mContext, true);
        }
    }

    @Override
    public void handleDisplayOutsideApp() {
        boolean messageSaved = saveNewMessage();
        if (messageSaved) {
            showNotificationForIntent(viewSeshActionIntent(false, mNotification.correspondingSesh()));
        }
    }

    public Runnable bannerTapCallback() {
        return new Runnable() {
            @Override
            public void run() {

                Sesh correspondingSesh = mNotification.correspondingSesh();
                if (correspondingSesh != null) {
                    if (!mNotification.viewSeshFragmentIsVisible(correspondingSesh, mContext)) {
                        mContext.sendBroadcast(viewSeshActionIntent(true, mNotification.correspondingSesh()));
                    } else {
                        MainContainerActivity mainContainerActivity = (MainContainerActivity) ApplicationLifecycleTracker.sharedInstance(mContext).activityInForeground;
                        ViewSeshFragment viewSeshFragment = (ViewSeshFragment)mainContainerActivity.getContainerStateManager().getMainContainerState().fragment;
                        viewSeshFragment.openMessaging();
                    }
                }
            }
        };
    }

    private Intent refreshMessagesIntent() {
        return new Intent(MessagingActivity.REFRESH_MESSAGES);
    }

    private Intent viewSeshActionIntent(boolean forBroadcast, Sesh sesh) {
        Intent intent;
        if (forBroadcast) {
            intent = new Intent(MainContainerActivity.NEW_MESSAGE_ACTION);
        } else {
            intent = new Intent(MainContainerActivity.NEW_MESSAGE_ACTION,
                    null, mContext, MainContainerActivity.class);
        }
        intent.putExtra(ViewSeshFragment.SESH_KEY, sesh.seshId);
        intent.putExtra(ViewSeshFragment.OPEN_MESSAGING, true);
        return intent;
    }
}