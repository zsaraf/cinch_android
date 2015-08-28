package com.seshtutoring.seshapp.services.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.SeshStateManager.SeshState;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.services.SeshGCMListenerService;
import com.seshtutoring.seshapp.services.notifications.handlers.NotificationHandler;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 8/18/15.
 */
public class SeshNotificationManagerService extends IntentService {
    private static final String TAG = SeshNotificationManagerService.class.getName();
    public static final String ENQUEUE_NEW_NOTIFICATION
            = "com.seshtutoring.seshapp.services.notifications.ENQUEUE_NEW_NOTIFICATION";
    public static final String START_IN_APP_DISPLAY_QUEUE_HANDLING
            = "com.seshtutoring.seshapp.services.notifications.START_QUEUE_HANDLING";
    public static final String PAUSE_IN_APP_DISPLAY_QUEUE_HANDLING
            = "com.seshtutoring.seshapp.services.notifications.PAUSE_QUEUE_HANDLING";
    public static final String CURRENT_NOTIFICATION_HAS_BEEN_HANDLED
            = "com.seshtutoring.seshapp.services.notifications.CURRENT_NOTIFICATION_HAS_BEEN_HANDLED";
    public static final String REFRESH_NOTIFICATIONS_ACTION
            = "com.seshtutoring.seshapp.services.notifications.REFRESH_NOTIFICATIONS";
    public static final String NOTIFICATION_PENDING_DELETION_KEY = "pending_deletion";
    private boolean paused;
    private ApplicationLifecycleTracker applicationLifecycleTracker;
    private InAppNotificationDisplayQueue displayQueue;

    public SeshNotificationManagerService() {
        super(TAG);
    }

    public void onHandleIntent(Intent intent) {
        this.applicationLifecycleTracker = ApplicationLifecycleTracker.sharedInstance(getApplicationContext());
        this.displayQueue = InAppNotificationDisplayQueue.sharedInstance(getApplicationContext());

        switch(intent.getAction()) {
            case START_IN_APP_DISPLAY_QUEUE_HANDLING:
                displayQueue.resumeHandling();
                break;
            case PAUSE_IN_APP_DISPLAY_QUEUE_HANDLING:
                displayQueue.pauseHandling();
                break;
            case ENQUEUE_NEW_NOTIFICATION:
                Log.d(TAG, "ENQUEUEING NEW NOTIFICATION");
                try {
                    JSONObject notificationObj =
                            new JSONObject(intent.getStringExtra(SeshGCMListenerService.NOTIFICATION_OBJ_KEY));
                    Notification notification
                            = Notification.createOrUpdateNotification(notificationObj, getApplicationContext());
                    NotificationHandler notificationHandler = notification.getNotificationHandler(this);

                    if (!applicationLifecycleTracker.applicationInForeground()) {
                        notificationHandler.handleDisplayOutsideApp();
                    } else {
                        if (!displayQueue.notificationHandlingInProgress()) {
                            displayQueue.handleNext();
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to enqueue / handle new notification; json response malformed: " + e);
                }
                break;
            case REFRESH_NOTIFICATIONS_ACTION:
                Notification.createRefreshNotification();
                if (!displayQueue.notificationHandlingInProgress()) {
                    displayQueue.handleNext();
                }
                break;
            case CURRENT_NOTIFICATION_HAS_BEEN_HANDLED:
                if (intent.getBooleanExtra(NOTIFICATION_PENDING_DELETION_KEY, false)) {
                    Notification currentNotification = displayQueue.getCurrentNotification();
                    currentNotification.pendingDeletion = true;
                    currentNotification.save();
                }
                displayQueue.currentNotificationHasBeenHandled();
                displayQueue.handleNext();
                break;
        }
    }
}
