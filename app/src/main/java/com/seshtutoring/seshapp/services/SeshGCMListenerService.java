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
import com.seshtutoring.seshapp.view.SplashActivity;

/**
 * Created by nadavhollander on 7/29/15.
 */
public class SeshGCMListenerService extends GcmListenerService {
    private static final String TAG = SeshGCMListenerService.class.getName();
    public static final String NOTIFICATION_ID_EXTRA = "opened_by_notification";
    private static final String IDENTIFIER_KEY = "identifier";
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
                    break;
                case "MESSAGE":
                    break;
                case "UPDATE_TUTOR_LOCATION":
                    break;
                case "NOTIFY_TUTOR":
                    break;
                case "FOUND_TUTOR":
                    break;
            default:
                PendingIntent pendingIntent = pendingIntentFromNotification(SplashActivity.class, DEFAULT_NOTIFICATION_ID);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.backpack_white)
                        .setContentTitle("Sesh")
                        .setContentText(data.getString(ALERT_KEY))
                        .setColor(getResources().getColor(R.color.seshorange))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
                break;
        }
    }

    private PendingIntent pendingIntentFromNotification(Class<?> activityClass, int id) {
        Intent resultIntent = new Intent(this, activityClass);
        resultIntent.putExtra(NOTIFICATION_ID_EXTRA, id);

        // ensures back button will lead to home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
    }
}
