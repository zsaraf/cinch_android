package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nadavhollander on 7/6/15.
 */
public class User extends SugarRecord<User> {
    @Ignore
    private static final String TAG = User.class.getName();

    public int userId;
    public String email;
    public String sessionId;
    public String fullName;
    public String profilePictureUrl;
    public String bio;
    public String stripeCustomerId;
    public int classYear;
    public String major;
    public boolean notificationsEnabled = false;
    public boolean completedAppTour = false;
    public boolean isVerified = false;
    public String fullLegalName;
    public String shareCode;
    public Student student;
    public Tutor tutor;
    public School school;

//    @Ignore
//    public Set<Favorite> favorites;


    // empty constructor necessary for SugarORM to work
    public User() {}


    public static User currentUser(Context context) {
        return User.findAll(User.class).next();
    }

    public static void fetchUserInfoFromServer(final Context context) {
        SeshNetworking seshNetworking = new SeshNetworking(context);
        seshNetworking.getFullUserInfo(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.get("status").equals("SUCCESS")) {
                        createOrUpdateUserWithObject(jsonObject.getJSONObject(("data")), context);
                    } else {
                        Log.e(TAG, "Failed to fetch full user info from server.");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to fetch user info from server; response malformed");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to fetch user info from server; network error");
            }
        });
    }

    public static void logoutUserLocally(Context context) {
        User.deleteAll(User.class);
        SeshAuthManager.sharedManager(context).clearSession();
        GCMRegistrationIntentService.clearGCMRegistrationToken(context);
        Log.i(TAG, "User logged out locally.");
    }

    public static User createOrUpdateUserWithObject(JSONObject dataJson, Context context) {
        User user = null;
        try {
            JSONObject userRow = dataJson.getJSONObject("user");
            JSONObject studentRow = dataJson.getJSONObject("student");
            JSONObject tutorRow = dataJson.getJSONObject("tutor");
            JSONObject schoolRow = dataJson.getJSONObject("school");

            int userId = userRow.getInt("id");

            if (User.listAll(User.class).size() > 0) {
                List<User> usersFound = User.find(User.class, "user_id = ?", Integer.toString(userId));
                if (usersFound.size() > 0) {
                    user = usersFound.get(0);
                } else {
                    user = new User();
                }
            } else {
                user = new User();
            }

            user.userId = userId;
            user.email = userRow.getString("email");
            user.sessionId = dataJson.getString("session_id");
            user.fullName = userRow.getString("full_name");
            user.profilePictureUrl = userRow.getString("profile_picture");
            user.bio = userRow.getString("bio");
            user.stripeCustomerId = !userRow.isNull("stripe_customer_id") ?
                    userRow.getString("stripe_customer_id") : "";
            user.major = userRow.getString("major");
            user.notificationsEnabled = (userRow.getInt("notifications_enabled") == 1) ? true : false;
            user.completedAppTour = (userRow.getInt("completed_app_tour") == 1) ? true : false;
            user.isVerified = (userRow.getInt("is_verified") == 1) ? true : false;
            user.fullLegalName = userRow.getString("full_legal_name");
            user.shareCode = userRow.getString("share_code");
            user.school = School.createOrUpdateSchoolWithObject(schoolRow);
            user.tutor = Tutor.createOrUpdateTutorWithObject(tutorRow);
            user.student = Student.createOrUpdateStudentWithObject(studentRow);
            user.save();

            SeshAuthManager.sharedManager(context).foundSessionId(user.sessionId);

            if (dataJson.has("past_seshes")) {
                JSONArray pastSeshes = dataJson.getJSONArray("past_seshes");
                for (int i = 0; i < pastSeshes.length(); i++) {
                    JSONObject pastSeshJson = pastSeshes.getJSONObject(i);
                    PastSesh.createOrUpdatePastSesh(pastSeshJson);
                }
            }

            // Load the cards
            if (dataJson.has("cards")) {
                JSONArray cards = dataJson.getJSONArray("cards");
                for (int i = 0; i < cards.length(); i++) {
                    JSONObject cardsJson = cards.getJSONObject(i);
                    Card newCard = Card.createOrUpdateCardWithJSON(cardsJson, user);
                    newCard.save();
                }
            }

            Sesh.deleteAll(Sesh.class);
            if (dataJson.has("open_seshes")) {
                JSONArray openSeshes = dataJson.getJSONArray("open_seshes");
                for (int i = 0; i < openSeshes.length(); i++) {
                    JSONObject openSeshJson = openSeshes.getJSONObject(i);
                    Sesh.createOrUpdateSeshWithObject(openSeshJson, context);
                }
            }

            LearnRequest.deleteAll(LearnRequest.class);
            if (dataJson.has("open_requests")) {
                JSONArray openRequests = dataJson.getJSONArray("open_requests");
                for (int i = 0; i < openRequests.length(); i++) {
                    JSONObject openRequestJson = openRequests.getJSONObject(i);
                    LearnRequest.createOrUpdateLearnRequest(openRequestJson);
                }
            }

            Discount.deleteAll(Discount.class);
            if (dataJson.has("discounts")) {
                JSONArray discounts = dataJson.getJSONArray("discounts");
                for (int i = 0; i < discounts.length(); i++) {
                    JSONObject discountJson = discounts.getJSONObject(i);
                    Discount.createOrUpdateDiscountWithObject(context, discountJson);
                }
            }

            OutstandingCharge.deleteAll(OutstandingCharge.class);
            if (dataJson.has("outstanding_charges")) {
                JSONArray outstandingCharges = dataJson.getJSONArray("outstanding_charges");
                for (int i = 0; i < outstandingCharges.length(); i++) {
                    JSONObject outstandingChargesJSONObject = outstandingCharges.getJSONObject(i);
                    OutstandingCharge.createOrUpdateOutstandingChargeWithObject(context, outstandingChargesJSONObject);
                }
            }

//            Add hinting mechanism, eg:
//            if (!currentUser.hint_displays) {
//                currentUser.hint_displays = [HintDisplays createNewHintDisplays];
//            }


        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed: " + e);
            return null;
        }
        return user;
    }

    public float getCreditSum() {
        return student.credits + tutor.cashAvailable;
    }

    public List<PastSesh> getPastSeshes() {
       List<PastSesh> studentPastSeshes =
               PastSesh.find(PastSesh.class, "student_user_id = ?", Integer.toString(userId));
        List<PastSesh> tutorPastSeshes =
                PastSesh.find(PastSesh.class, "tutor_user_id = ?", Integer.toString(userId));
        studentPastSeshes.addAll(tutorPastSeshes);
        return studentPastSeshes;
    }

    public List<Card> getCards() {
        return Card.find(Card.class, "user = ?", Long.toString(this.getId()));
    }

    public List<Sesh> getOpenSeshes() {
        return Sesh.listAll(Sesh.class);
    }

    public List<LearnRequest> getOpenRequests() {
        return LearnRequest.listAll(LearnRequest.class);
    }

    public List<Discount> getDiscounts() {
        return Discount.find(Discount.class, "user = ?", Long.toString(getId()));
    }
}
