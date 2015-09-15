package com.seshtutoring.seshapp.services.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.services.notifications.handlers.NotificationHandler;

/**
 * Created by nadavhollander on 8/26/15.
 */
public class InAppNotificationDisplayQueue {
    private static final String TAG = InAppNotificationDisplayQueue.class.getName();
    private static final int REFRESH_NOTIFICATIONS_REQUEST_CODE = 21;
    private static final int FIFTEEN_SECONDS_MILLIS = 15 * 1000;

    private static InAppNotificationDisplayQueue mInstance;

    public Notification currentNotification;
    private boolean paused;
    private Context mContext;
    private AlarmManager refreshNotificationsAlarm;
    private PendingIntent refreshNotificationsPendingIntent;

    public static InAppNotificationDisplayQueue sharedInstance(Context context) {
        if (mInstance == null) {
            mInstance = new InAppNotificationDisplayQueue(context);
        }

        return mInstance;
    }

    public InAppNotificationDisplayQueue(Context context) {
        this.mContext = context;
        this.paused = true;
        this.currentNotification = null;
        this.refreshNotificationsAlarm
                = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(SeshNotificationManagerService.REFRESH_NOTIFICATIONS_ACTION);
        this.refreshNotificationsPendingIntent = PendingIntent.getService(mContext,
                REFRESH_NOTIFICATIONS_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void resumeHandling() {
        paused = false;
        refreshNotificationsAlarm.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), FIFTEEN_SECONDS_MILLIS, refreshNotificationsPendingIntent);
        handleNext();
    }

    public void pauseHandling() {
        paused = true;
        refreshNotificationsAlarm.cancel(refreshNotificationsPendingIntent);
    }

    public boolean isPaused() {
        return paused;
    }

    public void currentNotificationHasBeenHandled() {
        currentNotification = null;
    }

    public Notification getCurrentNotification() {
        return currentNotification;
    }

    public synchronized void handleNext() {
        Notification notification = Notification.getTopPriorityNotification();

        if (notification != null) {
            Log.d(TAG, "Next Notification: " + notification.identifier);
        }

        if (shouldHandleNotification(notification)) {

            currentNotification = notification;
            Log.d(TAG, "HANDLING NOTIFICATION: " + currentNotification.identifier + " w/ Priority: " + currentNotification.priority);

            NotificationHandler notificationHandler =
                    currentNotification.getNotificationHandler(mContext);
            notificationHandler.handleDisplayInsideApp();
        }
    }

    private boolean shouldHandleNotification(Notification notification) {
        if (notification == null || paused) return false;

        if (notificationHandlingInProgress()) {
            return false;
        }

        if (SeshStateManager.getCurrentSeshState(mContext) == SeshStateManager.SeshState.IN_SESH) {
            return notification.priority <= 2;
        } else {
            return true;
        }
    }

    public boolean notificationHandlingInProgress() {
        return currentNotification != null;
    }
}
