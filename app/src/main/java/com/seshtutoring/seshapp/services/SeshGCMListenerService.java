package com.seshtutoring.seshapp.services;

import android.app.Notification;
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
import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.SplashActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment.MenuOption;
import com.seshtutoring.seshapp.view.fragments.TeachViewFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nadavhollander on 7/29/15.
 */
public class SeshGCMListenerService extends GcmListenerService {
    private static final String TAG = SeshGCMListenerService.class.getName();
    public static final String NOTIFICATION_ID_EXTRA = "opened_by_notification";
    private static final String IDENTIFIER_KEY = "identifier";
    private static final String CUSTOM_KEY = "custom";
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
    public void onMessageReceived(String from, Bundle data) {
        String identifier = data.getString(IDENTIFIER_KEY);
        Intent intent;

        switch (identifier) {
                case "UPDATE_STATE":
                    String stateIdentifier = null;
                    try {
                        JSONObject stateObj = new JSONObject(data.getString(CUSTOM_KEY));
                        stateIdentifier = stateObj.getString(STATE_KEY);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to update Sesh State; push data malformed:" + e);
                    }
                    if (stateIdentifier != null) {
                        SeshStateManager.updateSeshState(stateIdentifier);
                    }
                    break;
                case "MESSAGE":
                    break;
                case "NOTIFY_TUTOR":
                    // ignore silent push
                    if (data.getString(ALERT_KEY).equals("")) return;

                    if (ApplicationLifecycleTracker.applicationInForeground()) {
                        intent = new Intent(MainContainerActivity.UPDATE_CONTAINER_STATE_ACTION);
                    } else {
                        intent = new Intent(this, MainContainerActivity.class);
                    }

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(MainContainerActivity.MAIN_CONTAINER_STATE_KEY, MenuOption.HOME);
                    bundle.putString(MainContainerActivity.FRAGMENT_FLAG_KEY, HomeFragment.SHOW_AVAILABLE_JOBS_FLAG);
                    bundle.putInt(NOTIFICATION_ID_EXTRA, DEFAULT_NOTIFICATION_ID);
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent pendingIntent;
                    if (ApplicationLifecycleTracker.applicationInForeground()) {
                        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    } else {
                        pendingIntent = pendingIntentForIntent(intent, MainContainerActivity.class, DEFAULT_NOTIFICATION_ID);
                    }
                    
                    showNotification(data.getString(ALERT_KEY), pendingIntent);
                    break;
            case "FOUND_TUTOR":
                    break;
            default:
                intent = new Intent(this, SplashActivity.class);
                showNotification(data.getString(ALERT_KEY),
                        pendingIntentForIntent(intent, SplashActivity.class, DEFAULT_NOTIFICATION_ID));
                break;
        }
    }

    private void showNotification(String subtitle, PendingIntent pendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.backpack_white)
                .setContentTitle("Sesh")
                .setContentText(subtitle)
                .setColor(getResources().getColor(R.color.seshorange))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
    }

    private PendingIntent pendingIntentForIntent(Intent intent, Class<?> activityClass, int notificationId) {
        intent.putExtra(NOTIFICATION_ID_EXTRA, notificationId);

        // ensures back button will lead to home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(intent);

        return stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
    }
}
