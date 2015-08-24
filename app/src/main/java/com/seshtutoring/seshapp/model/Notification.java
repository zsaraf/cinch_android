package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.services.notifications.RefreshNotificationsNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.services.notifications.handlers.DefaultNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.NewMessageNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.NewRequestNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.NotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.RequestTimeoutNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.SeshCreatedNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.SeshStartedStudentNotificationHandler;
import com.seshtutoring.seshapp.services.notifications.handlers.UpdateStateNotificationHandler;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

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
        SESH_CREATED_STUDENT, SESH_CREATED_TUTOR, UPDATE_STATE, REFRESH_NOTIFICATIONS, DISCOUNT_AVAILABLE
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
    public boolean dataUpdateHandled;
    public boolean displayHandled;

    public Notification() {}

    public Notification(String data, String identifier, String message, int notification_id,
                        boolean pending_deletion, int priority, String title, boolean updated_with_data,
                        User user, boolean dataUpdateHandled, boolean displayHandled) {
        this.data = data;
        this.identifier = identifier;
        this.message = message;
        this.notificationId = notification_id;
        this.pendingDeletion = pending_deletion;
        this.priority = priority;
        this.title = title;
        this.dataUpdateHandled = dataUpdateHandled;
        this.displayHandled = displayHandled;
    }

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
            notification.displayHandled = false;
            notification.dataUpdateHandled = false;

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
        refreshNotification.dataUpdateHandled = false;
        refreshNotification.displayHandled = false;

        refreshNotification.save();

        return refreshNotification;
    }

    public synchronized static void refreshNotifications(final Notification refreshNotification, final Context context) {
        List<Notification> handledNotifications = null;

        handledNotifications = Notification.find(Notification.class, "pending_deletion = ?", "1");

        SeshNetworking seshNetworking = new SeshNetworking(context);
        seshNetworking.refreshNotifications(handledNotifications, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
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

                        refreshNotification.handled(context, true);
                    } else {
                        Log.e(TAG, "Failed to refresh notifications; " + jsonObject.getString("message"));
                        refreshNotification.handled(context, false);
                    }

                } catch (JSONException e) {
                    refreshNotification.handled(context, false);
                    Log.e(TAG, "Failed to refresh notifications; JSON response malformed : " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                refreshNotification.handled(context, false);
                Log.e(TAG, "Failed to refresh notifications; network error : " + volleyError);
            }
        });
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

    public void handled(Context context, boolean deleteNotification) {
        if (deleteNotification) {
            pendingDeletion = true;
            save();
        }

        Intent intent = new Intent(SeshNotificationManagerService.CURRENT_NOTIFICATION_HAS_BEEN_HANDLED,
                null, context, SeshNotificationManagerService.class);
        context.startService(intent);

        Log.d(TAG, identifier + " has been handled.");
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
        } else {
            Log.e(TAG, "Notification has no type....; identifier: " + identifier);
            return null;
        }
    }

    public NotificationHandler getNotificationHandler(SeshApplication seshApplication) {
        NotificationType notificationType = getNotificationType();
        switch (notificationType) {
            case SESH_CREATED_STUDENT:
                return new SeshCreatedNotificationHandler(this, seshApplication); // tested
            case SESH_CREATED_TUTOR:
                return new SeshCreatedNotificationHandler(this, seshApplication); // tested
            case NEW_REQUEST:
                return new NewRequestNotificationHandler(this, seshApplication); // tested
            case SESH_STARTED_STUDENT:
                return new SeshStartedStudentNotificationHandler(this, seshApplication); // tested
            case UPDATE_STATE:
                return new UpdateStateNotificationHandler(this, seshApplication); // tested
            case NEW_MESSAGE:
                return new NewMessageNotificationHandler(this, seshApplication);
            case REFRESH_NOTIFICATIONS:
                return new RefreshNotificationsNotificationHandler(this, seshApplication); // tested
            case REQUEST_TIMEOUT:
                return new DefaultNotificationHandler(this, seshApplication);
            case SESH_CANCELLED_TUTOR:
                return new DefaultNotificationHandler(this, seshApplication);
            case SESH_CANCELLED_STUDENT:
                return new DefaultNotificationHandler(this, seshApplication);
            case LOCATION_NOTES_UPDATED:
                return new DefaultNotificationHandler(this, seshApplication);
            case SET_TIME_UPDATED:
                return new DefaultNotificationHandler(this, seshApplication);
            case SESH_APPROACHING_STUDENT:
                return new DefaultNotificationHandler(this, seshApplication);
            case SESH_APPROACHING_TUTOR:
                return new DefaultNotificationHandler(this, seshApplication);
            case SESH_REVIEW_STUDENT:
                return new DefaultNotificationHandler(this, seshApplication);
            case SESH_REVIEW_TUTOR:
                return new DefaultNotificationHandler(this, seshApplication);
            case DISCOUNT_AVAILABLE:
                return new DefaultNotificationHandler(this, seshApplication);
            default:
                return null;
        }
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
