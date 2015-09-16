package com.seshtutoring.seshapp.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.services.notifications.handlers.DiscountAvailableNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.RefreshNotificationsNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.services.notifications.handlers.DefaultNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.LocationNotesUpdatedNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.NewMessageNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.NewRequestNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.NotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.RequestSentNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.RequestTimeoutNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.SeshApproachingNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.SeshCancelledNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.SeshCreatedNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.SeshReviewNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.SeshStartedStudentNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.SetTimeUpdatedNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.UpdateStateNotificationHandler;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.SeshNetworking.SynchronousRequest;
import com.seshtutoring.seshapp.view.ContainerState;
import com.seshtutoring.seshapp.view.MainContainerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nadavhollander on 8/18/15.
 */
public class Notification extends SugarRecord<Notification> {
    @Ignore
    public enum NotificationType {
        NEW_REQUEST, NEW_MESSAGE, LOCATION_NOTES_UPDATED, SESH_STARTED_STUDENT,
        REQUEST_TIMEOUT, SESH_CANCELLED_TUTOR, SESH_CANCELLED_STUDENT, SET_TIME_UPDATED,
        SESH_APPROACHING_STUDENT, SESH_APPROACHING_TUTOR, SESH_REVIEW_STUDENT, SESH_REVIEW_TUTOR,
        SESH_CREATED_STUDENT, SESH_CREATED_TUTOR, UPDATE_STATE, REFRESH_NOTIFICATIONS, DISCOUNT_AVAILABLE,
        REQUEST_SENT
    }

    @Ignore
    private static final String TAG = Notification.class.getName();

    public String data;
    public String identifier;
    public String message;
    public int notificationId;
    public boolean pendingDeletion;
    public int priority;
    public String title;
    public User user;
    public boolean wasDisplayedOutsideApp;

    public Notification() {}

    public synchronized static Notification createOrUpdateNotification(JSONObject bundleNotification, Context context) {
        Notification notification = null;

        try {
            int notification_id = bundleNotification.getInt("id");

            List<Notification> notificationsFound =
                    Notification.find(Notification.class, "notification_id = ?",
                                Integer.toString(notification_id));
            if (notificationsFound.size() > 0) {
                notification = notificationsFound.get(0);
            } else {
                notification = new Notification();
            }

            notification.notificationId = notification_id;
            notification.identifier = bundleNotification.getString("identifier");
            notification.title = bundleNotification.getString("title");
            notification.message = bundleNotification.getString("message");
            notification.data = bundleNotification.getString("data");
            notification.priority = bundleNotification.getInt("priority");
            notification.pendingDeletion = false;
            notification.wasDisplayedOutsideApp = false;

            if (SeshAuthManager.sharedManager(context).isValidSession()) {
                notification.user = User.currentUser(context);
            }

            notification.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update Notification object; " + e);
        }
        return notification;
    }

    public synchronized static Notification getTopPriorityNotification() {
        Notification topPriorityNotification = null;

        List<Notification> notificationsFound =
                Notification.find(Notification.class, "pending_deletion = ?", new String[]{"0"}, null, "priority ASC", "1");
        if (notificationsFound.size() > 0) {
            topPriorityNotification = notificationsFound.get(0);
        }

        return topPriorityNotification;
    }

    public synchronized static Notification createRefreshNotification() {
        Notification refreshNotification = null;

        int refreshNotificationId = -1;

        List<Notification> refreshNotificationsFound
                = Notification.find(Notification.class, "notification_id = ?",
                Integer.toString(refreshNotificationId));
        if (refreshNotificationsFound.size() > 0) {
            refreshNotification = refreshNotificationsFound.get(0);
        } else {
            refreshNotification = new Notification();
        }

        refreshNotification.notificationId = refreshNotificationId;
        refreshNotification.identifier = "REFRESH_NOTIFICATIONS";
        refreshNotification.priority = 1;
        refreshNotification.pendingDeletion = false;
        refreshNotification.wasDisplayedOutsideApp = false;

        refreshNotification.save();

        return refreshNotification;
    }

    public static Notification createDiscountNotification(String bannerTitle, String bannerMessage) {
        Notification discountNotification = null;

        int discountNotificationId = -2;

        List<Notification> discountNotificationsFound
                = Notification.find(Notification.class, "notification_id = ?",
                Integer.toString(discountNotificationId));
        if (discountNotificationsFound.size() > 0) {
            discountNotification = discountNotificationsFound.get(0);
        } else {
            discountNotification = new Notification();
        }

        discountNotification.notificationId = discountNotificationId;
        discountNotification.identifier = "DISCOUNT_AVAILABLE";
        discountNotification.priority = 5;
        discountNotification.title = bannerTitle;
        discountNotification.message = bannerMessage;
        discountNotification.pendingDeletion = false;
        discountNotification.wasDisplayedOutsideApp = false;

        discountNotification.save();

        return discountNotification;
    }

    public synchronized static Notification createRequestSentNotification() {
        Notification requestSentNotification = null;

        int requestSentNotificationId = -3;

        List<Notification> requestSentNotificationsFound
                = Notification.find(Notification.class, "notification_id = ?",
                Integer.toString(requestSentNotificationId));
        if (requestSentNotificationsFound.size() > 0) {
            requestSentNotification = requestSentNotificationsFound.get(0);
        } else {
            requestSentNotification = new Notification();
        }

        requestSentNotification.notificationId = requestSentNotificationId;
        requestSentNotification.identifier = "REQUEST_SENT";
        requestSentNotification.priority = 4;
        requestSentNotification.pendingDeletion = false;
        requestSentNotification.wasDisplayedOutsideApp = false;

        requestSentNotification.save();

        return requestSentNotification;
    }

    public synchronized static void refreshNotifications(final Notification refreshNotification, final Context context) {
        final List<Notification> handledNotifications
                = Notification.find(Notification.class, "pending_deletion = ?", "1");
        final SeshNetworking seshNetworking = new SeshNetworking(context);

        SynchronousRequest request = new SynchronousRequest() {
            @Override
            public void request(RequestFuture<JSONObject> blocker) {
                seshNetworking.refreshNotifications(handledNotifications, blocker, blocker);
            }

            @Override
            public void onErrorException(Exception e) {
                Notification.currentNotificationHandled(context, false);
                Log.e(TAG, "Failed to refresh notifications: " + e);
            }
        };

        JSONObject jsonObject = request.execute();

        if (jsonObject != null) {
            Set<Notification> notifications = new HashSet<Notification>();
            try {
                if (jsonObject.getString("status").equals("SUCCESS")) {
                    JSONArray notificationsFromServer = jsonObject.getJSONArray("notifications");
                    for (int i = 0; i < notificationsFromServer.length(); i++) {
                        notifications.add(Notification.createOrUpdateNotification(
                                notificationsFromServer.getJSONObject(i), context));
                    }

                    for (Notification notification : Notification.listAll(Notification.class)) {
                        if (!notifications.contains(notification)) {
                            notification.delete();
                        }
                    }

                    Notification.currentNotificationHandled(context, true);
                } else {
                    Log.e(TAG, "Failed to refresh notifications; " + jsonObject.getString("message"));
                    Notification.currentNotificationHandled(context, false);
                }
            } catch (JSONException e) {
                Notification.currentNotificationHandled(context, false);
                Log.e(TAG, "Failed to refresh notifications; JSON response malformed : " + e);
            }
        }
    }

    public Object getDataObject(String key) {
        try {
            JSONObject data = new JSONObject(this.data);
            return data.get(key);
        } catch (JSONException e) {
            Log.e(TAG, "Couldn't get data object, JSON malformed; " + e);
            return null;
        }
    }

    public static void currentNotificationHandled(Context context, boolean deleteNotification) {
        Intent notificationHandled = new Intent(SeshNotificationManagerService.CURRENT_NOTIFICATION_HAS_BEEN_HANDLED, null,
                context, SeshNotificationManagerService.class);

        if (deleteNotification) {
            notificationHandled.putExtra(SeshNotificationManagerService.SET_NOTIFICATION_PENDING_DELETION_KEY, true);
        }

        context.startService(notificationHandled);
    }

    public NotificationType getNotificationType() {
        if (identifier.equals("NEW_REQUEST")) {
            return NotificationType.NEW_REQUEST;
        } else if (identifier.equals("NEW_MESSAGE")) {
            return NotificationType.NEW_MESSAGE;
        } else if (identifier.equals("SESH_STARTED_STUDENT")) {
            return NotificationType.SESH_STARTED_STUDENT;
        } else if (identifier.equals("LOCATION_NOTES_UPDATED")) {
            return NotificationType.LOCATION_NOTES_UPDATED;
        } else if (identifier.equals("REQUEST_TIMEOUT")) {
            return NotificationType.REQUEST_TIMEOUT;
        } else if (identifier.equals("SESH_CANCELLED_TUTOR")) {
            return NotificationType.SESH_CANCELLED_TUTOR;
        } else if (identifier.equals("SESH_CANCELLED_STUDENT")) {
            return NotificationType.SESH_CANCELLED_STUDENT;
        } else if (identifier.equals("SET_TIME_UPDATED")) {
            return NotificationType.SET_TIME_UPDATED;
        } else if (identifier.equals("SESH_APPROACHING_STUDENT")) {
            return NotificationType.SESH_APPROACHING_STUDENT;
        } else if (identifier.equals("SESH_APPROACHING_TUTOR")) {
            return NotificationType.SESH_APPROACHING_TUTOR;
        } else if (identifier.equals("SESH_REVIEW_STUDENT")) {
            return NotificationType.SESH_REVIEW_STUDENT;
        } else if (identifier.equals("SESH_REVIEW_TUTOR")) {
            return NotificationType.SESH_REVIEW_TUTOR;
        } else if (identifier.equals("SESH_CREATED_STUDENT")) {
            return NotificationType.SESH_CREATED_STUDENT;
        } else if (identifier.equals("SESH_CREATED_TUTOR")) {
            return NotificationType.SESH_CREATED_TUTOR;
        } else if (identifier.equals("UPDATE_STATE")) {
            return NotificationType.UPDATE_STATE;
        } else if (identifier.equals("REFRESH_NOTIFICATIONS")) {
            return NotificationType.REFRESH_NOTIFICATIONS;
        } else if (identifier.equals("DISCOUNT_AVAILABLE")) {
            return NotificationType.DISCOUNT_AVAILABLE;
        } else if (identifier.equals("REQUEST_SENT")) {
            return NotificationType.REQUEST_SENT;
        } else {
                Log.e(TAG, "Notification has no type....; identifier: " + identifier);
                return null;
        }
    }

    public NotificationHandler getNotificationHandler(Context context) {
        NotificationType notificationType = getNotificationType();
        switch (notificationType) {
            case SESH_CREATED_STUDENT:
                return new SeshCreatedNotificationHandler(this, context); // tested (NEEDS VIEW SESH TESTING)
            case SESH_CREATED_TUTOR:
                return new SeshCreatedNotificationHandler(this, context); // tested (NEEDS VIEW SESH TESTING)
            case NEW_REQUEST:
                return new NewRequestNotificationHandler(this, context); // tested
            case SESH_STARTED_STUDENT:
                return new SeshStartedStudentNotificationHandler(this, context); // tested
            case UPDATE_STATE:
                return new UpdateStateNotificationHandler(this, context); // tested
            case NEW_MESSAGE:
                return new NewMessageNotificationHandler(this, context); // tested
            case REFRESH_NOTIFICATIONS:
                return new RefreshNotificationsNotificationHandler(this, context); // tested
            case REQUEST_TIMEOUT:
                return new RequestTimeoutNotificationHandler(this, context); // tested
            case SESH_CANCELLED_TUTOR:
                return new SeshCancelledNotificationHandler(this, context); // tested
            case SESH_CANCELLED_STUDENT:
                return new SeshCancelledNotificationHandler(this, context); // tested
            case LOCATION_NOTES_UPDATED:
                return new LocationNotesUpdatedNotificationHandler(this, context); // tested (NEEDS VIEW SESH TESTING)
            case SET_TIME_UPDATED:
                return new SetTimeUpdatedNotificationHandler(this, context); // relevant when scheduling (NEEDS VIEW SESH TESTING)
            case SESH_APPROACHING_STUDENT:
                return new SeshApproachingNotificationHandler(this, context); //  relevant when scheduling  (NEEDS VIEW SESH TESTING)
            case SESH_APPROACHING_TUTOR:
                return new SeshApproachingNotificationHandler(this, context); // relevant when scheduling  (NEEDS VIEW SESH TESTING)
            case SESH_REVIEW_STUDENT:
                return new SeshReviewNotificationHandler(this, context); // tested
            case SESH_REVIEW_TUTOR:
                return new SeshReviewNotificationHandler(this, context); // tested
            case REQUEST_SENT:
                return new RequestSentNotificationHandler(this, context);
            case DISCOUNT_AVAILABLE:
                return new DiscountAvailableNotificationHandler(this, context);
            default:
                return new DefaultNotificationHandler(this, context);
        }
    }

    public Sesh correspondingSesh() {
        NotificationType notificationType = getNotificationType();
        if (notificationType == NotificationType.NEW_MESSAGE ||
                notificationType == NotificationType.LOCATION_NOTES_UPDATED ||
                notificationType == NotificationType.SET_TIME_UPDATED ||
                notificationType == NotificationType.SESH_APPROACHING_TUTOR ||
                notificationType == NotificationType.SESH_APPROACHING_STUDENT ||
                notificationType == NotificationType.SESH_CREATED_TUTOR ||
                notificationType == NotificationType.SESH_CREATED_STUDENT) {
            int seshId = (int) getDataObject("sesh_id");
            return Sesh.findSeshWithId(seshId);
        } else {
            return null;
        }
    }

    public boolean viewSeshFragmentIsVisible(Sesh correspondingSesh, Context mContext) {
        Activity activity = ApplicationLifecycleTracker.sharedInstance(mContext).activityInForeground;
        if (activity instanceof MainContainerActivity) {
            MainContainerActivity mainContainerActivity = (MainContainerActivity)activity;
            ContainerState containerState = (mainContainerActivity).getContainerStateManager().getMainContainerState();
            if (containerState.tag.equals(correspondingSesh.getContainerStateTag())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object notification) {
        return ((Notification)notification).notificationId == notificationId;
    }

    @Override
    public int hashCode() {
        return notificationId;
    }
}
