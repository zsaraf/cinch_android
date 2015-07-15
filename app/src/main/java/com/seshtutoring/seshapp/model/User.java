package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.seshtutoring.seshapp.util.db.UserDbHelper;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/6/15.
 */
public class User {
    private static final String TAG = User.class.getName();

    private int userId;
    private String email;
    private String sessionId;
    private String fullName;
    private String profilePictureUrl;
    private String bio;
    private String stripeCustomerId;
    private int classYear;
    private String major;
    private boolean tutorOfflinePing = false;
    private boolean completedAppTour = false;
    private boolean isVerified = false;
    private String fullLegalName;
    private String shareCode;
//    private boolean completedOnBoarding = false;
//    private int schoolId;
//    private Student student;
//    private Tutor tutor;

    public User(int userId, String email, String sessionId, String fullName,
                String profilePictureUrl, String bio, String stripeCustomerId, String major,
                boolean tutorOfflinePing, boolean completedAppTour, boolean isVerified,
                String fullLegalName, String shareCode) {
        this.userId = userId;
        this.email = email;
        this.sessionId = sessionId;
        this.fullName = fullName;
        this.profilePictureUrl = profilePictureUrl;
        this.bio = bio;
        this.stripeCustomerId = stripeCustomerId;
        this.major = major;
        this.tutorOfflinePing = tutorOfflinePing;
        this.completedAppTour = completedAppTour;
        this.isVerified = isVerified;
        this.fullLegalName = fullLegalName;
        this.shareCode = shareCode;
    }

    public static User currentUser(Context context) {
        UserDbHelper userDbHelper = new UserDbHelper(context);
        return userDbHelper.getCurrentUser();
    }

    public static void logoutUserLocally(Context context) {
        UserDbHelper userDbHelper = new UserDbHelper(context);
        userDbHelper.deleteAllUsers();

        SeshAuthManager.sharedManager(context).clearSession();

        Log.i(TAG, "User logged out locally.");
    }

    public static void createOrUpdateUserWithObject(JSONObject userJson, Context context) {
        User user;
        String sessionId;

        try {
            JSONObject userRow = userJson.getJSONObject("user");
            int userId = userRow.getInt("id");
            String email = userRow.getString("email");
            sessionId = userJson.getString("session_id");
            String fullName = userRow.getString("full_name");
            String profilePictureUrl = userRow.getString("profile_picture");
            String bio = userRow.getString("bio");
            String stripeCustomerId = !userRow.isNull("stripe_customer_id") ?
                    userRow.getString("stripe_customer_id") : "";
            String major = userRow.getString("major");
            boolean tutorOfflinePing = (userRow.getInt("tutor_offline_ping") == 1) ? true : false;
            boolean completedAppTour = (userRow.getInt("completed_app_tour") == 1) ? true : false;
            boolean isVerified = (userRow.getInt("is_verified") == 1) ? true : false;
            String fullLegalName = userRow.getString("full_legal_name");
            String shareCode = userRow.getString("share_code");
            user = new User(userId, email, sessionId, fullName, profilePictureUrl, bio,
                    stripeCustomerId, major, tutorOfflinePing, completedAppTour,
                    isVerified, fullLegalName, shareCode);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create user in db; JSON user object from server is malformed.");
            return;
        }

        Log.i(TAG, "Creating or updating user in db.");

        UserDbHelper userDbHelper = new UserDbHelper(context);
        long id = userDbHelper.createOrUpdateUser(user);

        if (id < 0) {
            Log.e(TAG, "Failed to create or update user in db.");
        } else {
            Log.i(TAG, "User succesfully created or updated in db.");
        }

        // Log user in on client
        SeshAuthManager.sharedManager(context).foundSessionId(sessionId);
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getBio() {
        return bio;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public String getMajor() {
        return major;
    }

    public boolean isTutorOfflinePing() {
        return tutorOfflinePing;
    }

    public boolean completedAppTour() {
        return completedAppTour;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getFullLegalName() {
        return fullLegalName;
    }

    public String getShareCode() {
        return shareCode;
    }
}
