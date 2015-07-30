package com.seshtutoring.seshapp.util.networking;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Rate;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Rate;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.tz.DateTimeZoneBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Wrapper for any asynchronous interactions with the API.
 */
public class SeshNetworking {
    private static final String TAG = SeshNetworking.class.getName();
    private static final String FORMAT_PARAM = "format";
    private static final String EMAIL_PARAM = "email";
    private static final String PASSWORD_PARAM = "password";
    private static final String FULL_NAME_PARAM = "full_name";
    private static final String SESSION_ID_PARAM = "session_id";
    private static final String SEARCH_QUERY_PARAM = "search_query";
    private static final String DEVICE_TOKEN_PARAM = "device_token";
    private static final String DEVICE_TYPE_PARAM = "type";
    private static final String IS_DEV_PARAM = "is_dev";
    private static final String MAJOR_PARAM = "major";
    private static final String BIO_PARAM = "bio";
    private static final String CLASS_YEAR_PARAM = "class_year";
    private static final String SESH_STATE_IDENTIFIER_PARAM = "sesh_state_identifier";
    private static final String TUTOR_ID_PARAM = "tutor_id";
    private static final String LATITUDE_PARAM = "latitude";
    private static final String LONGITUDE_PARAM = "longitude";
    private static final String HELPFUL_RATING_PARAM = "rating_1";
    private static final String KNOWLEDGE_RATING_PARAM = "rating_2";
    private static final String FRIENDLINESS_RATING_PARAM = "rating_3";
    private static final String FAVORITED_PARAM = "favorited";
    private static final String OLD_PASSWORD_PARAM = "old_password";
    private static final String NEW_PASSWORD_PARAM = "new_password";
    private static final String SCHOOL_NAME_PARAM = "school_name";
    private static final String FULL_LEGAL_NAME_PARAM = "full_legal_name";
    private static final String CLASS_ID_PARAM = "class_id";
    private static final String CODE_PARAM = "code";
    private static final String CUSTOMER_TOKEN_PARAM = "customer_token";
    private static final String RECIPIENT_TOKEN_PARAM = "recipient_token";
    private static final String IS_RECIPIENT_PARAM = "is_recipient";
    private static final String NUM_PEOPLE_PARAM = "num_people";
    private static final String DESCRIPTION_PARAM = "description";
    private static final String EST_TIME_PARAM = "est_time";
    private static final String RATE_PARAM = "rate";
    private static final String FAVORITES_PARAM = "favorites";
    private static final String AVAILABLE_BLOCKS_PARAM = "available_blocks";
    private static final String IS_INSTANT_PARAM = "is_instant";
    private static final String EXPIRATION_TIME_PARAM = "expiration_time";
    private static final String TIMEZONE_OFFSET_PARAM = "timezone_offset";

    private Context mContext;

    public SeshNetworking(Context context) {
        this.mContext = context;
    }

    public void postWithRelativeUrl(String relativeUrl, Map<String, String> params,
                                    Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        String absoluteUrl = baseUrl() + relativeUrl;

        Log.i(TAG, "Issuing POST request to URL:  " + absoluteUrl + " with params: " +
                params.toString());
        JsonPostRequestWithAuth requestWithAuth = new JsonPostRequestWithAuth(absoluteUrl,
                params, successListener, errorListener);

        VolleyNetworkingWrapper.getInstance(mContext).addToRequestQueue(requestWithAuth);
    }

    public void postWithLongTimeout(String relativeUrl, Map<String, String> params,
                                          Response.Listener<JSONObject> successListener,
                                          Response.ErrorListener errorListener) {
        String absoluteUrl = baseUrl() + relativeUrl;

        Log.i(TAG, "Issuing POST request to URL:  " + absoluteUrl + " with params: " +
                params.toString());
        JsonPostRequestWithAuth requestWithAuth = new JsonPostRequestWithAuth(absoluteUrl,
                params, successListener, errorListener);
        requestWithAuth.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyNetworkingWrapper.getInstance(mContext).addToRequestQueue(requestWithAuth);
    }

    public void signUpWithName(String name, String email, String password,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(FORMAT_PARAM, "json");
        params.put(EMAIL_PARAM, email);
        params.put(FULL_NAME_PARAM, name);
        params.put(PASSWORD_PARAM, password);


        postWithRelativeUrl("create_user.php", params, successListener, errorListener);
    }

    public void loginWithEmail(String email, String password,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(FORMAT_PARAM, "json");
        params.put(EMAIL_PARAM, email);
        params.put(PASSWORD_PARAM, password);

        postWithRelativeUrl("login.php", params, successListener, errorListener);
    }

    public void forgotPasswordWithEmail(String email,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(EMAIL_PARAM, email);

        postWithRelativeUrl("forgot_password.php", params, successListener, errorListener);
    }

    public void searchForClassName(String className,
                                        Response.Listener<JSONObject> successListener,
                                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(FORMAT_PARAM, "json");
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SEARCH_QUERY_PARAM, className);

        postWithRelativeUrl("search_classes.php", params, successListener, errorListener);
    }

    public void updateDeviceToken(String deviceToken,
                                   Response.Listener<JSONObject> successListener,
                                   Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(DEVICE_TOKEN_PARAM, deviceToken);
        params.put(DEVICE_TYPE_PARAM, "android");
        params.put(IS_DEV_PARAM, SeshApplication.IS_DEV ? "1" : "0");

        TimeZone timezone = TimeZone.getDefault();
        int timeZoneOffset = timezone.getOffset(new Date().getTime()) / 1000 / 60 / 60;

        params.put(TIMEZONE_OFFSET_PARAM, Integer.toString(timeZoneOffset));

        postWithRelativeUrl("update_device_token.php", params, successListener, errorListener);
    }

    public void updateUserInformationWithMajorAndBio(String major, String bio,
                                  Response.Listener<JSONObject> successListener,
                                  Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(MAJOR_PARAM, major);
        params.put(BIO_PARAM, bio);
        params.put(CLASS_YEAR_PARAM, "2015"); // @TODO this is sketchy

        postWithRelativeUrl("update_user_info.php", params, successListener, errorListener);
    }

    public void sendBecomeATutorEmail(Response.Listener<JSONObject> successListener,
                                  Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("send_become_a_tutor_email.php", params, successListener, errorListener);
    }

    public void getFullUserInfo(Response.Listener<JSONObject> successListener,
                                  Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("get_user_info.php", params, successListener, errorListener);
    }

    public void makeTutorUnavailable(Response.Listener<JSONObject> successListener,
                                Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("make_tutor_unavailable.php", params, successListener, errorListener);
    }

    public void getSeshInformation(Response.Listener<JSONObject> successListener,
                                     Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("get_sesh_info.php", params, successListener, errorListener);
    }

    public void verifySeshStateWithSeshStateIdentifier(String identifier,
                                                       Response.Listener<JSONObject> successListener,
                                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_STATE_IDENTIFIER_PARAM, identifier);

        postWithRelativeUrl("verify_sesh_state.php", params, successListener, errorListener);
    }

    public void favoriteTutor(String tutorId, Response.Listener<JSONObject> successListener,
                              Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(TUTOR_ID_PARAM, tutorId);

        postWithRelativeUrl("favorite_tutor.php", params, successListener, errorListener);
    }

    public void unfavoriteTutor(String tutorId, Response.Listener<JSONObject> successListener,
                                Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(TUTOR_ID_PARAM, tutorId);

        postWithRelativeUrl("unfavorite_tutor.php", params, successListener, errorListener);
    }

    public void updateTutorLatLong(float latitude, float longitude,
                                   Response.Listener<JSONObject> successListener,
                                   Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(LATITUDE_PARAM, Float.toString(latitude));
        params.put(LONGITUDE_PARAM, Float.toString(longitude));

        postWithRelativeUrl("update_tutor_location.php", params, successListener, errorListener);
    }

    public void notifyThatTutorDoneReviewingSesh(String identifier,
                                                       Response.Listener<JSONObject> successListener,
                                                       Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("tutor_done_reviewing_sesh.php", params, successListener, errorListener);
    }

    public void submitSeshRating(int helpfulRating, int knowledgeRating, int friendlinessRating,
                                 boolean favorited, Response.Listener<JSONObject> successListener,
                                 Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(HELPFUL_RATING_PARAM, Integer.toString(helpfulRating));
        params.put(KNOWLEDGE_RATING_PARAM, Integer.toString(knowledgeRating));
        params.put(FRIENDLINESS_RATING_PARAM, Integer.toString(friendlinessRating));
        params.put(FAVORITED_PARAM, favorited ? "1" : "0");

        postWithRelativeUrl("submit_rating.php", params, successListener, errorListener);
    }

    public void changePassword(String oldPassword, String newPassword,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(OLD_PASSWORD_PARAM, oldPassword);
        params.put(NEW_PASSWORD_PARAM, newPassword);

        postWithRelativeUrl("change_password.php", params, successListener, errorListener);
    }

    public void completeOnboarding(Response.Listener<JSONObject> successListener,
                                   Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("complete_onboarding.php", params, successListener, errorListener);
    }

    public void completeAppTour(Response.Listener<JSONObject> successListener,
                                Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("complete_app_tour.php", params, successListener, errorListener);
    }

    public void toggleTutorOfflinePing(Response.Listener<JSONObject> successListener,
                                       Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("toggle_tutor_offline_ping.php", params, successListener, errorListener);
    }

    public void deleteRequest(Response.Listener<JSONObject> successListener,
                              Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("delete_request.php", params, successListener, errorListener);
    }

    public void becomeCampusRepAtSchool(String school,
                                        Response.Listener<JSONObject> successListener,
                                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SCHOOL_NAME_PARAM, school);

        postWithRelativeUrl("become_campus_rep.php", params, successListener, errorListener);
    }

    public void hasSeenSeshCancellationNotice(Response.Listener<JSONObject> successListener,
                                Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("has_seen_sesh_cancellation_notice.php", params, successListener, errorListener);
    }

    public void updateFullLegalName(String fullLegalName,
                                    Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(FULL_LEGAL_NAME_PARAM, fullLegalName);

        postWithRelativeUrl("update_full_legal_name.php", params, successListener, errorListener);
    }

    public void getCurrentRate(Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("get_rate.php", params, successListener, errorListener);
    }

    public void getPastSeshes(Response.Listener<JSONObject> successListener,
                              Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("get_past_sesh_info.php", params, successListener, errorListener);
    }

    public void resendVerificationEmail(String email, Response.Listener<JSONObject> successListener,
                                Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(EMAIL_PARAM, email);

        postWithRelativeUrl("resend_verification_email.php", params, successListener, errorListener);
    }

    public void cashout(Response.Listener<JSONObject> successListener,
                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("cash_out.php", params, successListener, errorListener);
    }

    public void logout(Response.Listener<JSONObject> successListener,
                       Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("logout.php", params, successListener, errorListener);
    }

    public void getFavoritesEligibleForClass(String classId,
                                             Response.Listener<JSONObject> successListener,
                                             Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(CLASS_ID_PARAM, classId);

        postWithRelativeUrl("get_favorites_eligible_for_class.php", params, successListener,
                errorListener);
    }

    public void acceptTutorTerms(Response.Listener<JSONObject> successListener,
                                 Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("accept_terms.php", params, successListener,
                errorListener);
    }

    public void redeemCode(String code, Response.Listener<JSONObject> successListener,
                           Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(CODE_PARAM, code);

        postWithRelativeUrl("redeem_code.php", params, successListener,
                errorListener);
    }

    public void getCards(Response.Listener<JSONObject> successListener,
                           Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("get_cards.php", params, successListener,
                errorListener);
    }

    public void addCard(String customerToken, String recipientToken, boolean isRecipient, Response.Listener<JSONObject> successListener,
                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(CUSTOMER_TOKEN_PARAM, customerToken);
        params.put(RECIPIENT_TOKEN_PARAM, recipientToken);
        params.put(IS_RECIPIENT_PARAM, isRecipient ? "1" : "0");

        postWithLongTimeout("add_card.php", params, successListener,
                errorListener);
    }

    private String baseUrl() {
        String baseUrl;
        if (SeshApplication.IS_LIVE) {
            baseUrl = "https://www.seshtutoring.com/ios-php/";
        } else {
            baseUrl = "https://www.cinchtutoring.com/ios-php/";
        }
        return baseUrl;
    }

    public void createRequestWithLearnRequest(LearnRequest learnRequest, Response.Listener<JSONObject> successListener,
                                              Response.ErrorListener errorListener) {
        Date expirationTime = new Date();
        for (AvailableBlock block : learnRequest.availableBlocks) {
            if (block.endTime.getTime() - expirationTime.getTime() > 0) {
                expirationTime = block.endTime;
            }
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ssZ");

        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(LATITUDE_PARAM, Double.toString(learnRequest.latitude));
        params.put(LONGITUDE_PARAM, Double.toString(learnRequest.latitude));
        params.put(NUM_PEOPLE_PARAM, Integer.toString(learnRequest.numPeople));
        params.put(CLASS_ID_PARAM, learnRequest.classId);
        params.put(DESCRIPTION_PARAM, learnRequest.descr);
        params.put(EST_TIME_PARAM, Integer.toString(learnRequest.estTime));
        params.put(RATE_PARAM, Float.toString(Rate.getCurrentHourlyRate(mContext).getHourlyRate()));
        params.put(FAVORITES_PARAM, "[]");  // until Favorites implemented....
        params.put(IS_INSTANT_PARAM, learnRequest.isInstant() ? "1" : "0");
        params.put(EXPIRATION_TIME_PARAM, formatter.print(new DateTime(expirationTime)));

        // hacky fix for adding nested hashmap params to POST request
        Iterator<AvailableBlock> blockIterator = learnRequest.availableBlocks.iterator();
        for (int counter = 0; blockIterator.hasNext(); counter++) {
            AvailableBlock block = blockIterator.next();
            params.put(AVAILABLE_BLOCKS_PARAM + "[" + counter + "][startTime]",
                    formatter.print(new DateTime(block.startTime)));
            params.put(AVAILABLE_BLOCKS_PARAM + "[" + counter + "][endTime]",
                    formatter.print(new DateTime(block.endTime)));
        }

        postWithRelativeUrl("create_request.php", params, successListener, errorListener);
    }


//    @TODO: implement once Stripe functionality in place
//    public void addCardWithCustomerToken(...)
//    public void getCardsForCurrentUserWithSuccess(...)
//    public void deleteCardWithId(...)
//    public void makeDefaultCard(...)

//    @TODO: Implement when relevant.
//    public void createBidForRequest(...)
//    public void getPossibleJobsForCourses(...)
//    public void uploadProfilePicture(...)
//    public void startSesh(...)
//    public void endSesh(...)
//    public void cancelSesh(...)
//    public void sendMessage(...)
//    public void hasReadCurrentMessage(...)
//    public void getAllMessagesForSesh(...)
//    public void reportProblem(...)
//    public void payOutstandingCharges(...)
}
