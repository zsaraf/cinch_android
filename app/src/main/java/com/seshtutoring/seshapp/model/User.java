package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.util.SeshMixpanelAPI;
import com.seshtutoring.seshapp.util.StorageUtils;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
        Iterator<User> iterator = User.findAll(User.class);
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public static void fetchUserInfoFromServer(final Context context) {
        SeshNetworking seshNetworking = new SeshNetworking(context);
        seshNetworking.getFullUserInfo(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                createOrUpdateUserWithObject(jsonObject, context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to fetch user info from server; network error");
            }
        });
    }

    public static void logoutUserLocally(Context context) {
        StorageUtils.clearAllSugarRecords();
        SeshAuthManager.sharedManager(context).clearSession();
        GCMRegistrationIntentService.clearGCMRegistrationToken(context);

        Intent pauseNotificationHandling
                = new Intent(SeshNotificationManagerService.PAUSE_IN_APP_DISPLAY_QUEUE_HANDLING,
                    null, context, SeshNotificationManagerService.class);
        context.startService(pauseNotificationHandling);

        Log.i(TAG, "User logged out locally.");
    }

    public static User createOrUpdateUserWithObject(JSONObject dataJson, Context context) {
        User user = null;
        try {
            JSONObject userRow = (dataJson.has("data")) ? dataJson.getJSONObject("data") : dataJson;
            JSONObject studentRow = userRow.getJSONObject("student");
            JSONObject tutorRow = userRow.getJSONObject("tutor");
            JSONObject schoolRow = userRow.getJSONObject("school");

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
            if (dataJson.has("session_id")) {
                user.sessionId = dataJson.getString("session_id");
            }
            user.fullName = userRow.getString("full_name");
            user.profilePictureUrl = userRow.getString("profile_picture");
            user.bio = userRow.getString("bio");
            user.stripeCustomerId = !userRow.isNull("stripe_customer_id") ?
                    userRow.getString("stripe_customer_id") : "";

            user.major = userRow.getString("major");
            user.notificationsEnabled = userRow.getBoolean("notifications_enabled");
            user.completedAppTour = userRow.getBoolean("completed_app_tour");
            user.isVerified = userRow.getBoolean("is_verified");
            user.fullLegalName = userRow.getString("full_legal_name");
            user.shareCode = userRow.getString("share_code");
            user.school = School.createOrUpdateSchoolWithObject(schoolRow);
            user.tutor = Tutor.createOrUpdateTutorWithObject(tutorRow);
            user.student = Student.createOrUpdateStudentWithObject(studentRow);
            user.save();

            if (studentRow.has("past_seshes")) {
                addPastSeshes(studentRow.getJSONArray("past_seshes"));
            }
            if (tutorRow.has("past_seshes")) {
                addPastSeshes(tutorRow.getJSONArray("past_seshes"));
            }

            // Load the cards
            Card.deleteAll(Card.class);
            if (dataJson.has("cards")) {
                JSONArray cards = dataJson.getJSONArray("cards");
                for (int i = 0; i < cards.length(); i++) {
                    JSONObject cardsJson = cards.getJSONObject(i);
                    Card newCard = Card.createOrUpdateCardWithJSON(cardsJson, user);
                    newCard.save();
                }
            }

            List<Sesh> currentSeshes = new ArrayList<>();
            if (studentRow.has("open_seshes")) {
                currentSeshes.addAll(addOpenSeshes(studentRow.getJSONArray("open_seshes"), context));
            }
            if (tutorRow.has("open_seshes")) {
                currentSeshes.addAll(addOpenSeshes(tutorRow.getJSONArray("open_seshes"), context));
            }
            deleteSeshesNotInArray(currentSeshes);

            LearnRequest.deleteAll(LearnRequest.class);
            if (studentRow.has("requests")) {
                JSONArray openRequests = studentRow.getJSONArray("requests");
                for (int i = 0; i < openRequests.length(); i++) {
                    JSONObject openRequestJson = openRequests.getJSONObject(i);
                    LearnRequest.createOrUpdateLearnRequest(openRequestJson);
                }
            }

            Discount.deleteAll(Discount.class);
            if (userRow.has("discounts")) {
                JSONArray discounts = userRow.getJSONArray("discounts");
                for (int i = 0; i < discounts.length(); i++) {
                    JSONObject discountJson = discounts.getJSONObject(i);
                    Discount.createOrUpdateDiscountWithObject(context, discountJson);
                }
            }

            if (tutorRow.has("courses")) {
                user.refreshTutorCoursesWithArray(tutorRow.getJSONArray("courses"), tutorRow.getJSONArray("departments"));
            } else {
                user.tutor.clearTutorCourses();
            }

            OutstandingCharge.deleteAll(OutstandingCharge.class);
            if (dataJson.has("outstanding_charges")) {
                JSONArray outstandingCharges = dataJson.getJSONArray("outstanding_charges");
                for (int i = 0; i < outstandingCharges.length(); i++) {
                    JSONObject outstandingChargesJSONObject = outstandingCharges.getJSONObject(i);
                    OutstandingCharge.createOrUpdateOutstandingChargeWithObject(context, outstandingChargesJSONObject);
                }
            }

            SeshAuthManager.sharedManager(context).foundSessionId(user.sessionId);

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

    public static void addPastSeshes(JSONArray pastSeshes) throws JSONException {
        for (int i = 0; i < pastSeshes.length(); i++) {
            JSONObject pastSeshJson = pastSeshes.getJSONObject(i);
            PastSesh.createOrUpdatePastSesh(pastSeshJson);
        }
    }

    public static List<Sesh> addOpenSeshes(JSONArray openSeshes, Context context) throws JSONException {
        ArrayList<Sesh> currentSeshes = new ArrayList<Sesh>();
        for (int i = 0; i < openSeshes.length(); i++) {
            JSONObject openSeshJson = openSeshes.getJSONObject(i);
            Sesh newSesh = Sesh.createOrUpdateSeshWithObject(openSeshJson, context);
            currentSeshes.add(newSesh);
        }
        return currentSeshes;
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

    public static void deleteSeshesNotInArray(List<Sesh> seshes) {
        List<Sesh> allSeshes = Sesh.listAll(Sesh.class);
        for (Sesh sesh : allSeshes) {
            boolean containsSesh = false;
            for (int i = 0; i < seshes.size(); i++) {
                Sesh currentSesh = seshes.get(i);
                if (currentSesh.seshId == sesh.seshId) {
                    containsSesh = true;
                }
            }
            if (!containsSesh) {
                sesh.delete();
            }
        }
    }

    public void refreshTutorCoursesWithArray(JSONArray courses, JSONArray departments) {
        tutor.clearTutorCourses();
        for (int i = 0; i < courses.length(); i++) {
            JSONObject courseJSON = null;
            try {
                courseJSON = courses.getJSONObject(i).getJSONObject("course");
                Course newCourse = Course.createOrUpdateCourseWithJSON(courseJSON, tutor, false);
                newCourse.save();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        tutor.clearTutorDepartments();
        for (int i = 0; i < departments.length(); i++) {
            JSONObject departmentJSON = null;
            try {
                departmentJSON = departments.getJSONObject(i);
                Department department = Department.createOrUpdateDepartmentWithJSON(departmentJSON, false);
                department.tutor = tutor;
                department.save();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
