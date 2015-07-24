package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 7/24/15.
 */
public class Tutor extends SugarRecord<Tutor> {
    @Ignore
    private final static String TAG = Tutor.class.getName();

    private int tutorId;
    private int userId;
    private boolean enabled;
    private float cashAvailable;
    private int hoursTutored;
    private boolean didAcceptTerms;
    private User user;

    // empty constructor necessary for SugarORM to work
    public Tutor() {}

    public Tutor(int tutorId, int userId, boolean enabled, int cashAvailable, int hoursTutored,
                 boolean didAcceptTerms, User user) {
        this.tutorId = tutorId;
        this.userId = userId;
        this.enabled = enabled;
        this.cashAvailable = cashAvailable;
        this.hoursTutored = hoursTutored;
        this.didAcceptTerms = didAcceptTerms;

        this.user = User.find(User.class, "user_id = ?", Integer.toString(userId)).get(0);
    }

    public static Tutor createOrUpdateTutorWithObject(JSONObject tutorJson) {
        Tutor tutor;

        try {
            int tutorId = tutorJson.getInt("id");

            List<Tutor> tutorsFound = Tutor.find(Tutor.class, "tutor_id = ?", Integer.toString(tutorId));

            if (tutorsFound.size() > 0) {
                tutor = tutorsFound.get(0);
            } else {
                tutor = new Tutor();
            }

            tutor.tutorId = tutorId;
            tutor.userId = tutorJson.getInt("userId");
            tutor.enabled = (tutorJson.getInt("enabled") == 1) ? true : false;
            JSONObject stats = tutorJson.getJSONObject("stats");
            tutor.cashAvailable = (float) stats.getDouble("cash_available");
            tutor.hoursTutored = stats.getInt("hours_tutored");
            tutor.didAcceptTerms = (tutorJson.getInt("did_accept_terms") == 1) ? true : false;

            tutor.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update tutor in db; JSON user object from server is malformed.");
            return null;
        }

        return tutor;
    }
}
