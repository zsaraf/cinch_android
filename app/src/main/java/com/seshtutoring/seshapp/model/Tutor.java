package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.seshtutoring.seshapp.util.db.TutorDbHelper;
import com.seshtutoring.seshapp.util.db.UserDbHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/24/15.
 */
public class Tutor {
    private final static String TAG = Tutor.class.getName();

    private int tutorId;
    private int userId;
    private boolean enabled;
    private float cashAvailable;
    private int hoursTutored;
    private boolean didAcceptTerms;
    private User user;

    public Tutor(int tutorId, int userId, boolean enabled, int cashAvailable, int hoursTutored,
                 boolean didAcceptTerms) {
        this.tutorId = tutorId;
        this.userId = userId;
        this.enabled = enabled;
        this.cashAvailable = cashAvailable;
        this.hoursTutored = hoursTutored;
        this.didAcceptTerms = didAcceptTerms;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static void createOrUpdateTutorWithObject(JSONObject tutorJson) {
        Tutor tutor;

        try {
            int tutorId = tutorJson.getInt("id");
            int userId = tutorJson.getInt("userId");
            boolean enabled = (tutorJson.getInt("enabled") == 1) ? true : false;
            JSONObject stats = tutorJson.getJSONObject("stats");
            double cashAvailable = stats.getDouble("cash_available");
            int hoursTutored = stats.getInt("hours_tutored");
            boolean didAcceptTerms = (tutorJson.getInt("did_accept_terms") == 1) ? true : false;
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create tutor in db; JSON user object from server is malformed.");
            return;
        }

        Log.i(TAG, "Creating or updating user in db.");

        TutorDbHelper tutorDbHelper = new TutorDbHelper(context);
        long id = tutorDbHelper.createOrUpdateTutor(tutor);

        if (id < 0) {
            Log.e(TAG, "Failed to create or update user in db.");
        } else {
            Log.i(TAG, "User succesfully created or updated in db.");
        }
    }
}
