package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 7/6/15.
 */
public class User extends SugarRecord<User> {
    @Ignore
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
    private Student student;
    private Tutor tutor;

    // empty constructor necessary for SugarORM to work
    public User() {}

    public User(int userId, String email, String sessionId, String fullName,
                String profilePictureUrl, String bio, String stripeCustomerId, String major,
                boolean tutorOfflinePing, boolean completedAppTour, boolean isVerified,
                String fullLegalName, String shareCode, Student student, Tutor tutor) {
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
        this.student = student;
        this.tutor = tutor;
    }

    public static User currentUser(Context context) {
        return User.findById(User.class, 1l);
    }

    public static void logoutUserLocally(Context context) {
        User.deleteAll(User.class);
        SeshAuthManager.sharedManager(context).clearSession();

        Log.i(TAG, "User logged out locally.");
    }

    public static User createOrUpdateUserWithObject(JSONObject userJson, Context context) {
        User user = null;
        try {
            JSONObject userRow = userJson.getJSONObject("user");
            JSONObject studentRow = userJson.getJSONObject("student");
            JSONObject tutorRow = userJson.getJSONObject("tutor");

            int userId = userRow.getInt("id");

            List<User> usersFound = User.find(User.class, "user_id = ?", Integer.toString(userId));

            if (usersFound.size() > 0) {
                user = usersFound.get(0);
            } else {
                user = new User();
            }

            user.userId = userId;
            user.email = userRow.getString("email");
            user.sessionId = userJson.getString("session_id");
            user.fullName = userRow.getString("full_name");
            user.profilePictureUrl = userRow.getString("profile_picture");
            user.bio = userRow.getString("bio");
            user.stripeCustomerId = !userRow.isNull("stripe_customer_id") ?
                    userRow.getString("stripe_customer_id") : "";
            user.major = userRow.getString("major");
            user.tutorOfflinePing = (userRow.getInt("tutor_offline_ping") == 1) ? true : false;
            user.completedAppTour = (userRow.getInt("completed_app_tour") == 1) ? true : false;
            user.isVerified = (userRow.getInt("is_verified") == 1) ? true : false;
            user.fullLegalName = userRow.getString("full_legal_name");
            user.shareCode = userRow.getString("share_code");
            user.save();

            user.tutor = Tutor.createOrUpdateTutorWithObject(tutorRow);
            user.student = Student.createOrUpdateStudentWithObject(studentRow);

            user.save();
            SeshAuthManager.sharedManager(context).foundSessionId(user.sessionId);

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed.");
            return null;
        }
        return user;
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
