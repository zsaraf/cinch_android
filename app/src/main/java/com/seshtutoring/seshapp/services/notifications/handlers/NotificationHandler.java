package com.seshtutoring.seshapp.services.notifications.handlers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.view.MainContainerActivity;

/**
 * Created by nadavhollander on 8/20/15.
 */
public abstract class NotificationHandler {
    public static final int DEFAULT_NOTIFICATION_ID = 0;
    public static final String NOTIFICATION_ID_EXTRA = "opened_by_notification";

    protected Context mContext;
    protected Notification mNotification;

    public NotificationHandler(Notification notification, Context context) {
        this.mNotification = notification;
        this.mContext = context;
    }

    protected void displayBanner() {
        // do nothing
    }

    protected void showNotificationForIntent(Intent intent) {
        intent.putExtra(NOTIFICATION_ID_EXTRA, DEFAULT_NOTIFICATION_ID);

        // ensures back button will lead to home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainContainerActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.backpack_white)
                .setContentTitle(mNotification.title)
                .setContentText(mNotification.message)
                .setColor(mContext.getResources().getColor(R.color.seshorange))
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
    }

    public abstract void handle();
}
