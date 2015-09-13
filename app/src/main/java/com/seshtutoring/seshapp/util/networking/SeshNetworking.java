package com.seshtutoring.seshapp.util.networking;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.RequestFuture;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.Constants;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.SeshImageCache;
import com.seshtutoring.seshapp.view.SeshActivity;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.stripe.android.Stripe;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private static final String TIMEZONE_NAME_PARAM = "timezone_name";
    private static final String TUTOR_COURSES_PARAM = "courses";
    private static final String REQUEST_ID_PARAM = "request_id";
    private static final String VERSION_NUMBER_PARAM = "version_number";
    private static final String SESH_ID_PARAM = "sesh_id";
    private static final String LOCATION_NOTES_PARAM = "location_notes";
    private static final String IS_PAST_PARAM = "is_past";
    private static final String PAST_SESH_ID_PARAM = "past_sesh_id";
    private static final String CONTENT_PARAM = "content";
    private static final String SET_TIME_PARAM = "start_time";
    private static final String HANDLED_NOTIFICATIONS_PARAM = "handled_notifications";
    private static final String MESSAGE_ID_PARAM = "message_id";
    private static final String DISCOUNT_ID = "discount_id";

    private Context mContext;

    public SeshNetworking(Context context) {
        this.mContext = context;
    }

    public Bitmap downloadProfilePictureSynchronous(String profilePictureUrl) {

        Bitmap bitmap = null;
        try {
            bitmap = PicassoWrapper.getInstance(mContext).picasso.load(profilePictureUrl).get();
        } catch (IOException e) {
            Log.e(TAG, "Failed to get profile picture; " + e);
        }

        return bitmap;
    }

    public void downloadProfilePictureAsync(String profilePictureUrl, ImageView imageView, Callback callback) {

        profilePictureUrl = baseUrl() + "resources/images/profile_pictures/" + profilePictureUrl;
        PicassoWrapper.getInstance(mContext).picasso.load(profilePictureUrl).placeholder(R.drawable.default_profile_picture).into(imageView, callback);

    }

    public void postWithRelativeUrl(String relativeUrl, Map<String, String> params,
                                    final Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        postWithRelativeUrl(relativeUrl, new JSONObject(params), successListener, errorListener);
    }

    public void postWithRelativeUrl(String relativeUrl, JSONObject jsonParams,
                                    final Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        String absoluteUrl = baseUrl() + apiUrl() + relativeUrl;

        Log.i(TAG, "Issuing POST request to URL:  " + absoluteUrl + " with params: " +
                jsonParams.toString());

        Response.Listener<JSONObject> successListenerWrapper = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                checkForInvalidSessionId(jsonObject);
                successListener.onResponse(jsonObject);
            }
        };

        JsonPostRequestWithAuth requestWithAuth = new JsonPostRequestWithAuth(absoluteUrl,
                jsonParams, successListenerWrapper, errorListener);

        VolleyNetworkingWrapper.getInstance(mContext).addToRequestQueue(requestWithAuth);
    }

    private void checkForInvalidSessionId(JSONObject jsonResponse) {
        if (jsonResponse.has("status")) {
            try {
                if(jsonResponse.getString("status").equals("INVALID_SESSION_ID")) {
                    SeshAuthManager.sharedManager(mContext).invalidateSession();
                }
            } catch (JSONException e) {
                // do nothing; let specific networking call's error handling deal with malformed JSON
            }
        }
    }

    public void postWithLongTimeout(String relativeUrl, Map<String, String> params,
                                          Response.Listener<JSONObject> successListener,
                                          Response.ErrorListener errorListener) {
        String absoluteUrl = baseUrl() + apiUrl() + relativeUrl;

        Log.i(TAG, "Issuing POST request to URL:  " + absoluteUrl + " with params: " +
                params.toString());
        JsonPostRequestWithAuth requestWithAuth = new JsonPostRequestWithAuth(absoluteUrl,
                new JSONObject(params), successListener, errorListener);
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
        params.put(VERSION_NUMBER_PARAM, "2.0");

        postWithRelativeUrl("create_user.php", params, successListener, errorListener);
    }

    public void loginWithEmail(String email, String password,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(FORMAT_PARAM, "json");
        params.put(EMAIL_PARAM, email);
        params.put(PASSWORD_PARAM, password);
        params.put(VERSION_NUMBER_PARAM, "2.0");

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

        DateTimeZone timezone = DateTimeZone.getDefault();

        params.put(TIMEZONE_NAME_PARAM, timezone.getID());

        postWithRelativeUrl("update_device_token.php", params, successListener, errorListener);
    }

    public void updateAnonymousToken(String deviceToken,
                                  Response.Listener<JSONObject> successListener,
                                  Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("token", deviceToken);
        params.put(DEVICE_TYPE_PARAM, "android");

        postWithRelativeUrl("update_anonymous_device_token.php", params, successListener, errorListener);
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

    public void getConstants(Response.Listener<JSONObject> successListener,
                             Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("get_constants.php", params, successListener, errorListener);
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

    private String loadUserJSON() {
        String json = null;
        try {
            InputStream is = mContext.getResources().getAssets().open("json/user.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.e(TAG, "Couldn't open user.json file -- make sure you've added this asset! " + ex.getMessage());
            return null;
        }
        return json;
    }

    private String baseUrl() {
        String baseUrl;
        if (SeshApplication.IS_DEV) {
            baseUrl = "https://www.cinchtutoring.com/";
        } else {
            baseUrl = "https://www.seshtutoring.com/";
        }
        return baseUrl;
    }

    private String apiUrl() {
        String apiUrl;
        String user = "";
        try {
            JSONObject obj = new JSONObject(loadUserJSON());
            user = obj.getString("user");
        } catch (JSONException ex) {
            ex.printStackTrace();
            return "ios-php/";
        }

        if (SeshApplication.USE_PERSONAL) {
            //apiUrl = "users/" + user + "/";
            apiUrl = "ios-php/";
        } else {
            apiUrl = "ios-php/";
        }
        return apiUrl;
    }

    public void createRequestWithLearnRequest(LearnRequest learnRequest, Response.Listener<JSONObject> successListener,
                                              Response.ErrorListener errorListener) {
        long expirationTime = 0;
        long thirtyMinutesMillis = 30 * 60 * 1000;

        if (learnRequest.isInstant()) {
            expirationTime = learnRequest.timestamp + thirtyMinutesMillis;
        } else {
            for (AvailableBlock block : learnRequest.availableBlocks) {
                if (block.endTime - expirationTime > 0) {
                    expirationTime = block.endTime;
                }
            }
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();

        JSONObject params = new JSONObject();
        try  {
            params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
            params.put(LATITUDE_PARAM, learnRequest.latitude);
            params.put(LONGITUDE_PARAM, learnRequest.longitude);
            params.put(NUM_PEOPLE_PARAM, learnRequest.numPeople);
            params.put(CLASS_ID_PARAM, learnRequest.classId);
            params.put(DESCRIPTION_PARAM, learnRequest.descr);
            params.put(EST_TIME_PARAM, learnRequest.estTime);
            params.put(RATE_PARAM, Constants.getHourlyRate(mContext));
            params.put(FAVORITES_PARAM, new ArrayList<>());  // until Favorites implemented....
            params.put(IS_INSTANT_PARAM, learnRequest.isInstant() ? "1" :    "0");
            params.put(EXPIRATION_TIME_PARAM, formatter.print(new DateTime(expirationTime)));

            if (learnRequest.discount != null) {
                params.put(DISCOUNT_ID, learnRequest.discount.discountId);
            }

            JSONArray availableBlocksJson = new JSONArray();
            for (AvailableBlock availableBlock : learnRequest.availableBlocks) {
                availableBlocksJson.put(availableBlock.toJson());
            }

            params.put(AVAILABLE_BLOCKS_PARAM, availableBlocksJson);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create request, json error: " + e);
            return;
        }

        postWithRelativeUrl("create_request.php", params, successListener, errorListener);
    }

    public void getTutorCourses(Response.Listener<JSONObject> successListener,
                         Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("get_tutor_classes.php", params, successListener,
                errorListener);
    }

    public void getAvailableJobs(ArrayList<Course> courses, Response.Listener<JSONObject> successListener,
                                Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("get_possible_jobs.php", params, successListener,
                errorListener);
    }

    public void createBid(int request_id, double latitude, double longitude, Response.Listener<JSONObject> successListener,
                                Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(LATITUDE_PARAM, Double.toString(latitude));
        params.put(LONGITUDE_PARAM, Double.toString(longitude));
        params.put(REQUEST_ID_PARAM, Integer.toString(request_id));

        postWithRelativeUrl("create_bid.php", params, successListener,
                errorListener);
    }

    public void motivateTeam(Response.Listener<JSONObject> successListener,
                          Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();

        postWithRelativeUrl("motivate_team.php", params, successListener,
                errorListener);
    }

    public void getAndroidLaunchDate(Response.Listener<JSONObject> successListener,
                                     Response.ErrorListener errorListener) {
         Map<String, String> params = new HashMap<>();

        postWithRelativeUrl("get_android_launch_date.php", params, successListener, errorListener);
    }

    public void refreshNotifications(List<Notification> handledNotifications, Response.Listener<JSONObject> successListener,
                                     Response.ErrorListener errorListener) {
        JSONObject params = new JSONObject();

        try  {
            params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

            JSONArray handledNotificationIds = new JSONArray();
            for (Notification notification : handledNotifications) {
                handledNotificationIds.put(notification.notificationId);
            }

            params.put(HANDLED_NOTIFICATIONS_PARAM, handledNotificationIds);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to refresh notifications; json error: " + e);
            return;
        }

        postWithRelativeUrl("refresh_notifications.php", params, successListener, errorListener);
    }

    public void getSeshInformationForSeshId(int seshId, Response.Listener<JSONObject> successListener,
                                     Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));
        params.put(IS_PAST_PARAM, "0");

        postWithRelativeUrl("get_info_for_sesh.php", params, successListener, errorListener);
    }

    public void getPastRequestInformationForPastRequestId(int pastRequestId, Response.Listener<JSONObject> successListener,
                                           Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(REQUEST_ID_PARAM, Integer.toString(pastRequestId));
        params.put(IS_PAST_PARAM, "1");

        postWithRelativeUrl("get_info_for_request.php", params, successListener, errorListener);
    }

    public void startSeshWithSeshId(int seshId, Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));

        postWithRelativeUrl("start_sesh.php", params, successListener, errorListener);
    }

    public void cancelSeshWithSeshId(int seshId, Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));

        postWithRelativeUrl("cancel_sesh.php", params, successListener, errorListener);
    }

    public void cancelRequestWithRequestId(int requestId, Response.Listener<JSONObject> successListener,
                                     Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(REQUEST_ID_PARAM, Integer.toString(requestId));

        postWithRelativeUrl("delete_request.php", params, successListener, errorListener);
    }

    public void reportProblem(String problem, int pastSeshId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(PAST_SESH_ID_PARAM, Integer.toString(pastSeshId));
        params.put(CONTENT_PARAM, problem);

        postWithRelativeUrl("report_problem.php", params, successListener, errorListener);

    }

    public void getPastSeshInformationForPastSeshId(int pastSeshId, Response.Listener<JSONObject> successListener,
                                                          Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(pastSeshId));
        params.put(IS_PAST_PARAM, "1");

        postWithRelativeUrl("get_info_for_sesh.php", params, successListener, errorListener);
    }

    public void endSesh(int seshId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));

        postWithRelativeUrl("end_sesh.php", params, successListener, errorListener);
    }

    public void setSetTime(int seshId, DateTime setTime, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));
        params.put(SET_TIME_PARAM, formatter.print(new DateTime(setTime)));

        postWithRelativeUrl("set_start_time.php", params, successListener, errorListener);
    }

    public void setLocationNotesForSesh(int seshId, String locationNotes, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));
        params.put(LOCATION_NOTES_PARAM, locationNotes);

        postWithRelativeUrl("set_location_notes.php", params, successListener, errorListener);
    }

    public void setLocationNotesForRequest(int requestId, String locationNotes, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(REQUEST_ID_PARAM, Integer.toString(requestId));
        params.put(LOCATION_NOTES_PARAM, locationNotes);

        postWithRelativeUrl("set_location_notes.php", params, successListener, errorListener);
    }

    public void sendMessage(String message, int seshId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));
        params.put(CONTENT_PARAM, message);

        postWithRelativeUrl("send_message.php", params, successListener, errorListener);
    }

    public void hasReadUpToMessage(int messageId, int seshId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));
        params.put(MESSAGE_ID_PARAM, Integer.toString(messageId));

        postWithRelativeUrl("has_read_up_to_message.php", params, successListener, errorListener);
    }

    public static abstract class SynchronousRequest {
        public JSONObject execute() {
            RequestFuture<JSONObject> blocker = RequestFuture.newFuture();
            request(blocker);

            JSONObject response = null;
            try {
               response = blocker.get(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                onErrorException(e);
            } catch (ExecutionException e) {
                onErrorException(e);
            } catch (TimeoutException e) {
                onErrorException(e);
            }

            return response;
        }

        public abstract void request(RequestFuture<JSONObject> blocker);
        public abstract void onErrorException(Exception e);
    }

    public void uploadProfilePicture(Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("upload_profile_picture.php", params, successListener, errorListener);
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
//    public void hasReadCurrentMessage(...)

//    public void payOutstandingCharges(...)
}
