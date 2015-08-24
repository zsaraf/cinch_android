package com.seshtutoring.seshapp.services.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.services.SeshGCMListenerService;
import com.seshtutoring.seshapp.services.notifications.handlers.NotificationHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/18/15.
 */
public class SeshNotificationManagerService extends IntentService {
    private static final String TAG = SeshNotificationManagerService.class.getName();
    public static final String ENQUEUE_NEW_NOTIFICATION
            = "com.seshtutoring.seshapp.services.notifications.ENQUEUE_NEW_NOTIFICATION";
    public static final String CURRENT_NOTIFICATION_HAS_BEEN_HANDLED
            = "com.seshtutoring.seshapp.services.notifications.CURRENT_NOTIFICATION_HAS_BEEN_HANDLED";
    public static final String REFRESH_NOTIFICATIONS_ACTION
            = "com.seshtutoring.seshapp.services.notifications.REFRESH_NOTIFICATIONS";
    public static Notification currentNotification = null;

    public SeshNotificationManagerService() {
        super(TAG);
    }

    public void onHandleIntent(Intent intent) {
        if (intent.getAction() ==  ENQUEUE_NEW_NOTIFICATION) {
            Log.d(TAG, "ENQUEUEING NEW NOTIFICATION");
            try {
                JSONObject notificationObj =
                        new JSONObject(intent.getStringExtra(SeshGCMListenerService.NOTIFICATION_OBJ_KEY));
                Notification.createOrUpdateNotification(notificationObj, getApplicationContext());
                handleTopPriorityNotification();
            } catch (JSONException e) {
                Log.e(TAG, "Failed to enqueue / handle new notification; json response malformed: " + e);
            }
        } else if (intent.getAction() == REFRESH_NOTIFICATIONS_ACTION) {
            Log.d(TAG, "ENQUEUING REFRESH NOTIFICATION");
            Notification.createRefreshNotification();
            handleTopPriorityNotification();
        } else if (intent.getAction() == CURRENT_NOTIFICATION_HAS_BEEN_HANDLED) {
            currentNotification = null;
            handleTopPriorityNotification();
        }
    }

    private synchronized void handleTopPriorityNotification() {
        Notification notification = Notification.getTopPriorityNotification();

        if (shouldHandleNotification(notification)) {

            currentNotification = notification;
            Log.d(TAG, "HANDLING NOTIFICATION: " + currentNotification.identifier + " w/ Priority: " + currentNotification.priority);

            NotificationHandler notificationHandler =
                    currentNotification.getNotificationHandler((SeshApplication)getApplication());

            notificationHandler.handle();
        }
    }

    private boolean shouldHandleNotification(Notification notification) {
        if (notification == null) return false;

        if (notificationHandlingInProgress()) {
            if (SeshStateManager.getCurrentSeshState(this) == SeshStateManager.SeshState.IN_SESH
                    && notification.priority <= 2) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean notificationHandlingInProgress() {
        return currentNotification != null;
    }
}
