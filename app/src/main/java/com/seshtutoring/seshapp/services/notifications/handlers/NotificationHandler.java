package com.seshtutoring.seshapp.services.notifications.handlers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.ImageView;
import android.widget.Toast;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshBanner;
import com.squareup.picasso.Callback;

/**
 * Created by nadavhollander on 8/20/15.
 */
public abstract class NotificationHandler {
    public static final int DEFAULT_NOTIFICATION_ID = 0;
    public static final String NOTIFICATION_ID_EXTRA = "opened_by_notification";
    public static final String NOTIFICATION_INTENT = "notification_intent";

    protected Context mContext;
    protected Notification mNotification;

    public NotificationHandler(Notification notification, Context context) {
        this.mNotification = notification;
        this.mContext = context;
    }

    protected void showNotificationForIntent(Intent intent) {

        intent.putExtra(NOTIFICATION_ID_EXTRA, DEFAULT_NOTIFICATION_ID);

        PackageManager pm = mContext.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage("com.seshtutoring.seshapp");
        launchIntent.putExtra(NOTIFICATION_INTENT, intent);

        // ensures back button will lead to home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainContainerActivity.class);
        stackBuilder.addNextIntent(launchIntent);

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

        mNotification.wasDisplayedOutsideApp = true;
        mNotification.save();
    }

    protected void loadImage(final ImageView imageView, final Callback callback) {
        final SeshNetworking seshNetworking = new SeshNetworking(mContext);

        // execute on main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                seshNetworking.downloadProfilePictureAsync(mNotification.correspondingSesh().userImageUrl,
                        imageView,
                        callback);
            }
        });
    }

    public abstract void handleDisplayInsideApp();

    public void handleDisplayOutsideApp() {
        showNotificationForIntent(new Intent(mContext, MainContainerActivity.class));
    }
}
