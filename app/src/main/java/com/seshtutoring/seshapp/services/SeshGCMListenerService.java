package com.seshtutoring.seshapp.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.SeshActivity;
import com.seshtutoring.seshapp.view.SplashActivity;
import com.seshtutoring.seshapp.view.WarmWelcomeActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.TeachViewFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by nadavhollander on 7/29/15.
 */
public class SeshGCMListenerService extends GcmListenerService {
    private static final String TAG = SeshGCMListenerService.class.getName();
    public static final String NOTIFICATION_ID_EXTRA = "opened_by_notification";
    public static final String NOTIFICATION_OBJ_KEY = "notification";
    private static final String IDENTIFIER_KEY = "identifier";
    private static final String TITLE_KEY = "title";
    private static final String MESSAGE_KEY = "message";
    private static final String DATA_KEY = "data";
    private static final String STATE_KEY = "state";
    private static final String ALERT_KEY = "alert";
    private static final int DEFAULT_NOTIFICATION_ID = 0;


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, final Bundle data) {
        String identifier;

        Intent intent = new Intent(SeshNotificationManagerService.ENQUEUE_NEW_NOTIFICATION,
                null, getApplicationContext(), SeshNotificationManagerService.class);
        intent.putExtra(NOTIFICATION_OBJ_KEY, data.getString(NOTIFICATION_OBJ_KEY));
        startService(intent);
//            identifier = notificationObj.getString(IDENTIFIER_KEY);
//
//            Intent intent;
//
//            boolean appInForeground =
//                    ((SeshApplication)getApplication()).getApplicationLifecycleTracker().applicationInForeground();
//
//            switch (identifier) {
//                case "UPDATE_STATE":
//                    String stateIdentifier = null;
//
//                    JSONObject stateObj = new JSONObject(notificationObj.getString(DATA_KEY));
//                    stateIdentifier = stateObj.getString(STATE_KEY);
//
//                    if (stateIdentifier != null) {
//                        SeshStateManager.sharedInstance(getApplicationContext()).updateSeshState(stateIdentifier);
//                    }
//                    break;
//                case "MESSAGE":
//                    break;
//                case "NOTIFY_TUTOR":
//                    if (appInForeground) {
//                        intent = new Intent(MainContainerActivity.UPDATE_CONTAINER_STATE_ACTION);
//                    } else {
//                        intent = new Intent(MainContainerActivity.UPDATE_CONTAINER_STATE_ACTION, null,
//                                this, MainContainerActivity.class);
//                    }
//
//                    Bundle bundle = new Bundle();
//                    bundle.putInt(MainContainerActivity.MAIN_CONTAINER_STATE_INDEX, 0);
//                    bundle.putString(MainContainerActivity.FRAGMENT_FLAG_KEY, HomeFragment.SHOW_AVAILABLE_JOBS_FLAG);
//                    bundle.putInt(NOTIFICATION_ID_EXTRA, DEFAULT_NOTIFICATION_ID);
//                    intent.putExtras(bundle);
//
//                    PendingIntent pendingIntent;
//                    if (appInForeground) {
//                        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    } else {
//                        pendingIntent = pendingIntentForIntent(intent, MainContainerActivity.class, DEFAULT_NOTIFICATION_ID);
//                    }
//
//                    showNotification(notificationObj.getString(TITLE_KEY), notificationObj.getString(MESSAGE_KEY), pendingIntent);
//                    break;
//                case "FOUND_TUTOR":
//                    if (appInForeground) {
//                        intent = new Intent(MainContainerActivity.FOUND_TUTOR_ACTION);
//                        sendBroadcast(intent);
//                    } else {
//                        intent = new Intent(MainContainerActivity.FOUND_TUTOR_ACTION, null,
//                                this, MainContainerActivity.class);
//
//                        showNotification(notificationObj.getString(TITLE_KEY), notificationObj.getString(MESSAGE_KEY),
//                                pendingIntentForIntent(intent, MainContainerActivity.class, DEFAULT_NOTIFICATION_ID));
//                    }
//                    break;
//                case "ANDROID_IS_LIVE":
//                        if (appInForeground) {
//                            intent = new Intent(SeshActivity.APP_IS_LIVE_ACTION);
//                            sendBroadcast(intent);
//                        } else {
//                            intent = new Intent(SeshActivity.APP_IS_LIVE_ACTION, null,
//                                    this, WarmWelcomeActivity.class);
//
//                            showNotification(notificationObj.getString(TITLE_KEY), notificationObj.getString(MESSAGE_KEY),
//                                    pendingIntentForIntent(intent, MainContainerActivity.class, DEFAULT_NOTIFICATION_ID));
//                        }
//                    break;
//                default:
//                    intent = new Intent(this, SplashActivity.class);
//                    showNotification(notificationObj.getString(TITLE_KEY), notificationObj.getString(MESSAGE_KEY),
//                            pendingIntentForIntent(intent, SplashActivity.class, DEFAULT_NOTIFICATION_ID));
//                    break;
//            }


    }

//    private void showNotification(String title, String subtitle, PendingIntent pendingIntent) {
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.backpack_white)
//                .setContentTitle(title)
//                .setContentText(subtitle)
//                .setColor(getResources().getColor(R.color.seshorange))
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
//    }

}
