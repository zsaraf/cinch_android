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
import com.seshtutoring.seshapp.view.SplashActivity;

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

                    break;
                case "FOUND_TUTOR":
                    break;
            default:
                Intent intent = new Intent(this, SplashActivity.class);
                showNotification(data.getString(ALERT_KEY),
                        pendingIntentForIntent(intent, DEFAULT_NOTIFICATION_ID));
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

    private PendingIntent pendingIntentForIntent(Intent intent, int notificationId) {
        Intent resultIntent = new Intent(this, intent.getClass());
        resultIntent.putExtra(NOTIFICATION_ID_EXTRA, notificationId);

        // ensures back button will lead to home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(intent.getClass());
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
    }
}
