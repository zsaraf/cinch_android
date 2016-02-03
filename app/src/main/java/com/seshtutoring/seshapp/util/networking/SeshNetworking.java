package com.seshtutoring.seshapp.util.networking;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.Constants;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.Department;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.DeviceUtils;
import com.squareup.picasso.Callback;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String CLASS_ID_PARAM = "course";
    private static final String CODE_PARAM = "code";
    private static final String CUSTOMER_TOKEN_PARAM = "customer_token";
    private static final String RECIPIENT_TOKEN_PARAM = "recipient_token";
    private static final String IS_RECIPIENT_PARAM = "is_recipient";
    private static final String NUM_PEOPLE_PARAM = "num_people";
    private static final String DESCRIPTION_PARAM = "description";
    private static final String EST_TIME_PARAM = "est_time";
    private static final String RATE_PARAM = "hourly_rate";
    private static final String FAVORITES_PARAM = "favorites";
    private static final String AVAILABLE_BLOCKS_PARAM = "available_blocks";
    private static final String IS_INSTANT_PARAM = "is_instant";
    private static final String EXPIRATION_TIME_PARAM = "expiration_time";
    private static final String TUTOR_COURSES_PARAM = "courses";
    private static final String REQUEST_ID_PARAM = "request_id";
    private static final String VERSION_NUMBER_PARAM = "version_number";
    private static final String SESH_ID_PARAM = "sesh_id";
    private static final String LOCATION_NOTES_PARAM = "location_notes";
    private static final String IS_PAST_PARAM = "is_past";
    private static final String PAST_SESH_ID_PARAM = "past_sesh_id";
    private static final String CONTENT_PARAM = "message";
    private static final String SET_TIME_PARAM = "start_time";
    private static final String HANDLED_NOTIFICATIONS_PARAM = "handled_notifications";
    private static final String MESSAGE_ID_PARAM = "last_activity_id";
    private static final String DISCOUNT_ID = "discount_id";
    private static final String CARD_ID_PARAM = "card_id";
    private static final String CLASSES_TO_ADD_ID = "classes_to_add";
    private static final String CLASSES_TO_DELETE_ID = "classes_to_delete";
    private static final String DEPARTMENTS_TO_ADD_ID = "depts_to_add";
    private static final String DEPARTMENTS_TO_DELETE_ID = "depts_to_delete";
    private static final String CANCELLATION_REASON_PARAM = "cancellation_reason";

    public enum RequestMethod {
        POST,
        GET,
    }

    public enum RequestType {
        DJANGO,
        PHP,
    }

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

    public String s3BucketPrefix() {
        if (!SeshApplication.IS_DEV) {
            return "https://sesh-tutoring-prod.s3.amazonaws.com";
        } else {
            return "https://sesh-tutoring-dev.s3.amazonaws.com";
        }
    }

    public static String networkErrorDetail(VolleyError volleyError) {
        if (volleyError.networkResponse != null) {
            try {
                JSONObject object = new JSONObject(new String(volleyError.networkResponse.data));
                return object.getString("detail");
            } catch (JSONException e) {
                return "We couldn't reach the network, sorry!";
            }
        } else {
            return "We couldn't reach the network, sorry!";
        }
    }

    public void downloadProfilePictureAsync(String profilePictureUrl, ImageView imageView, Callback callback) {
        profilePictureUrl = s3BucketPrefix() + "/images/profile_pictures/" + profilePictureUrl + "_large.jpeg";
        PicassoWrapper.getInstance(mContext).picasso.load(profilePictureUrl).placeholder(R.drawable.default_profile_picture).into(imageView, callback);
    }

    public void postWithRelativeUrl(String relativeUrl, Map<String, String> params, RequestType requestType, RequestMethod requestMethod,
                                    final Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        postWithRelativeUrl(relativeUrl, new JSONObject(params), requestType, requestMethod, successListener, errorListener);
    }

    public void postWithRelativeUrl(String relativeUrl, JSONObject jsonParams, RequestType requestType, RequestMethod requestMethod,
                                    final Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        String absoluteUrl = baseUrl() + apiUrl(requestType) + relativeUrl;

//        Log.i(TAG, "Issuing POST request to URL:  " + absoluteUrl + " with params: " +
//                jsonParams != null ? jsonParams.toString() : "{}");

        if ((requestType == RequestType.DJANGO || !jsonParams.has(SESSION_ID_PARAM)) || SeshAuthManager.sharedManager(mContext).isValidSession()) {
            Response.Listener<JSONObject> successListenerWrapper = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    checkForInvalidSessionId(jsonObject);
                    successListener.onResponse(jsonObject);
                }
            };

            int method = requestMethod == RequestMethod.GET ? 0 : 1;
            JsonObjectRequestWithAuth requestWithAuth = new JsonObjectRequestWithAuth(method, absoluteUrl,
                    jsonParams, mContext, successListenerWrapper, errorListener);
            requestWithAuth.setRetryPolicy(new DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleyNetworkingWrapper.getInstance(mContext).addToRequestQueue(requestWithAuth);
        } else {
            errorListener.onErrorResponse(new VolleyError("No authentication token so not making request!"));
        }
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

    public void postWithLongTimeout(String relativeUrl, Map<String, String> params, RequestType requestType,  RequestMethod requestMethod,
                                    Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        String absoluteUrl = baseUrl() + apiUrl(requestType) + relativeUrl;

        Log.i(TAG, "Issuing POST request to URL:  " + absoluteUrl + " with params: " +
                params.toString());
        int method = requestMethod == RequestMethod.GET ? 0 : 1;
        JsonObjectRequestWithAuth requestWithAuth = new JsonObjectRequestWithAuth(method, absoluteUrl,
                new JSONObject(params), mContext, successListener, errorListener);
        requestWithAuth.setRetryPolicy(new DefaultRetryPolicy(
                20000,
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

        postWithRelativeUrl("accounts/users/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void loginWithEmail(String email, String password,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(FORMAT_PARAM, "json");
        params.put(EMAIL_PARAM, email);
        params.put(PASSWORD_PARAM, password);

        DeviceUtils.paramsByAddingDeviceInformation(params, mContext);

        postWithRelativeUrl("accounts/users/login/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void forgotPasswordWithEmail(String email,
                                        Response.Listener<JSONObject> successListener,
                                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(EMAIL_PARAM, email);

        postWithRelativeUrl("forgot_password.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void searchForClassName(String className,
                                   Response.Listener<JSONObject> successListener,
                                   Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(FORMAT_PARAM, "json");
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SEARCH_QUERY_PARAM, className);

        postWithRelativeUrl("search_classes.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void updateDeviceToken(String deviceToken,
                                  Response.Listener<JSONObject> successListener,
                                  Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(DEVICE_TOKEN_PARAM, deviceToken);

        DeviceUtils.paramsByAddingDeviceInformation(params, mContext);

        postWithRelativeUrl("update_device_token.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void updateUserInformationWithMajorAndBio(String major, String bio,
                                                     Response.Listener<JSONObject> successListener,
                                                     Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(MAJOR_PARAM, major);
        params.put(BIO_PARAM, bio);
        params.put(CLASS_YEAR_PARAM, "2015"); // @TODO this is sketchy

        postWithRelativeUrl("accounts/users/update_user_info/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void sendBecomeATutorEmail(Response.Listener<JSONObject> successListener,
                                      Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("send_become_a_tutor_email.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void getFullUserInfo(Response.Listener<JSONObject> successListener,
                                Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();

        postWithRelativeUrl("accounts/users/get_full_info/", params, RequestType.DJANGO, RequestMethod.GET, successListener, errorListener);
    }

    public void verifySeshStateWithSeshStateIdentifier(String identifier,
                                                       Response.Listener<JSONObject> successListener,
                                                       Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_STATE_IDENTIFIER_PARAM, identifier);

        postWithRelativeUrl("verify_sesh_state.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void submitSeshRating(int seshId, int helpfulRating, int knowledgeRating, int friendlinessRating,
                                 boolean favorited, Response.Listener<JSONObject> successListener,
                                 Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(HELPFUL_RATING_PARAM, Integer.toString(helpfulRating));
        params.put(KNOWLEDGE_RATING_PARAM, Integer.toString(knowledgeRating));
        params.put(FRIENDLINESS_RATING_PARAM, Integer.toString(friendlinessRating));
        params.put(FAVORITED_PARAM, favorited ? "1" : "0");

        postWithRelativeUrl("tutoring/past_seshes/" + seshId + "/submit_rating/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void changePassword(String oldPassword, String newPassword,
                               Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(OLD_PASSWORD_PARAM, oldPassword);
        params.put(NEW_PASSWORD_PARAM, newPassword);

        postWithRelativeUrl("change_password.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void becomeCampusRepAtSchool(String school,
                                        Response.Listener<JSONObject> successListener,
                                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SCHOOL_NAME_PARAM, school);

        postWithRelativeUrl("become_campus_rep.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void updateFullLegalName(String fullLegalName,
                                    Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(FULL_LEGAL_NAME_PARAM, fullLegalName);

        postWithRelativeUrl("update_full_legal_name.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void getConstants(Response.Listener<JSONObject> successListener,
                             Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("get_constants.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void resendVerificationEmail(String email, Response.Listener<JSONObject> successListener,
                                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(EMAIL_PARAM, email);

        postWithRelativeUrl("resend_verification_email.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void cashout(Response.Listener<JSONObject> successListener,
                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("cash_out.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void logout(Response.Listener<JSONObject> successListener,
                       Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("logout.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void acceptTutorTerms(Response.Listener<JSONObject> successListener,
                                 Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("accept_terms.php", params, RequestType.PHP, RequestMethod.POST, successListener,
                errorListener);
    }

    public void redeemCode(String code, Response.Listener<JSONObject> successListener,
                           Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(CODE_PARAM, code);

        postWithRelativeUrl("redeem_code.php", params, RequestType.PHP, RequestMethod.POST, successListener,
                errorListener);
    }

    public void addCard(String customerToken, String recipientToken, boolean isRecipient, Response.Listener<JSONObject> successListener,
                        Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(CUSTOMER_TOKEN_PARAM, customerToken);
        params.put(RECIPIENT_TOKEN_PARAM, recipientToken);
        params.put(IS_RECIPIENT_PARAM, isRecipient ? "1" : "0");

        postWithLongTimeout("add_card.php", params, RequestType.PHP, RequestMethod.POST, successListener,
                errorListener);
    }

    private String baseUrl() {
        String baseUrl;
        if (SeshApplication.IS_DEV) {
            baseUrl = "https://cinchtutoring.com/";
        } else {
            baseUrl = "https://seshtutoring.com/";
        }
        return baseUrl;
    }

    private String apiUrl(RequestType requestType) {
        String apiUrl;

        if (requestType == RequestType.DJANGO) {
            apiUrl = "django/";
        } else {
            apiUrl = "php/";
        }

        return apiUrl;
    }

    public void createRequestWithLearnRequest(LearnRequest learnRequest, Response.Listener<JSONObject> successListener,
                                              Response.ErrorListener errorListener) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();

        JSONObject params = new JSONObject();
        try  {
            params.put(LATITUDE_PARAM, learnRequest.latitude);
            params.put(LONGITUDE_PARAM, learnRequest.longitude);
            params.put(NUM_PEOPLE_PARAM, learnRequest.numPeople);
            params.put(CLASS_ID_PARAM, learnRequest.classId);
            params.put(DESCRIPTION_PARAM, learnRequest.descr);
            params.put(EST_TIME_PARAM, learnRequest.estTime);
            params.put(RATE_PARAM, Constants.getHourlyRate(mContext));
            params.put(IS_INSTANT_PARAM, "0");

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

        postWithRelativeUrl("tutoring/sesh_requests/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void getAvailableJobs(Response.Listener<JSONObject> successListener,
                                 Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();

        postWithRelativeUrl("tutoring/sesh_requests/get_available_jobs/", params, RequestType.DJANGO, RequestMethod.POST, successListener,
                errorListener);
    }

    public void createBid(int request_id, double latitude, double longitude, Response.Listener<JSONObject> successListener,
                          Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(LATITUDE_PARAM, Double.toString(latitude));
        params.put(LONGITUDE_PARAM, Double.toString(longitude));
        params.put(REQUEST_ID_PARAM, Integer.toString(request_id));

        postWithRelativeUrl("create_bid.php", params, RequestType.PHP, RequestMethod.POST, successListener,
                errorListener);
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

        postWithRelativeUrl("refresh_notifications.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void getSeshInformationForSeshId(int seshId, Response.Listener<JSONObject> successListener,
                                            Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();

        postWithRelativeUrl("tutoring/open_seshes/" + seshId + "/", params, RequestType.DJANGO, RequestMethod.GET, successListener, errorListener);
    }

    public void getRequestInformationForRequestId(int requestId, Response.Listener<JSONObject> successListener,
                                                          Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();

        postWithRelativeUrl("tutoring/sesh_requests/" + requestId + "/", params, RequestType.DJANGO, RequestMethod.GET, successListener, errorListener);
    }

    public void startSeshWithSeshId(int seshId, Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();

        postWithRelativeUrl("tutoring/open_seshes/" + seshId + "/start/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void cancelSeshWithSeshId(int seshId, String reason, Response.Listener<JSONObject> successListener,
                                     Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(CANCELLATION_REASON_PARAM, reason);

        postWithRelativeUrl("tutoring/open_seshes/" + seshId + "/cancel/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void cancelRequestWithRequestId(int requestId, String reason, Response.Listener<JSONObject> successListener,
                                           Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(CANCELLATION_REASON_PARAM, reason);

        postWithRelativeUrl("tutoring/sesh_requests/" + requestId + "/cancel/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void reportProblem(String problem, int pastSeshId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(PAST_SESH_ID_PARAM, Integer.toString(pastSeshId));
        params.put(CONTENT_PARAM, problem);

        postWithRelativeUrl("report_problem.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);

    }

    public void getPastSeshInformationForPastSeshId(int pastSeshId, Response.Listener<JSONObject> successListener,
                                                    Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();

        postWithRelativeUrl("tutoring/past_seshes/" + pastSeshId + "/", params, RequestType.DJANGO, RequestMethod.GET, successListener, errorListener);
    }

    public void endSesh(int seshId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();

        postWithRelativeUrl("tutoring/open_seshes/" + seshId + "/end/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void setSetTime(int seshId, DateTime setTime, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));
        params.put(SET_TIME_PARAM, formatter.print(new DateTime(setTime)));

        postWithRelativeUrl("set_start_time.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void setLocationNotesForSesh(int seshId, String locationNotes, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(SESH_ID_PARAM, Integer.toString(seshId));
        params.put(LOCATION_NOTES_PARAM, locationNotes);

        postWithRelativeUrl("set_location_notes.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void setLocationNotesForRequest(int requestId, String locationNotes, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(REQUEST_ID_PARAM, Integer.toString(requestId));
        params.put(LOCATION_NOTES_PARAM, locationNotes);

        postWithRelativeUrl("set_location_notes.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void sendMessage(String message, int chatroomId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(CONTENT_PARAM, message);

        postWithRelativeUrl("chatrooms/chatrooms/" + chatroomId + "/send_message/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void payOutstandingCharges(Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());

        postWithRelativeUrl("pay_outstanding_charge.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void markMessagesReadWithChatroomId(int messageId, int chatroomId, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(MESSAGE_ID_PARAM, Integer.toString(messageId));

        postWithRelativeUrl("chatrooms/chatrooms/" + chatroomId + "/mark_as_read/", params, RequestType.DJANGO, RequestMethod.POST, successListener, errorListener);
    }

    public void deleteCard(String cardId, boolean isRecipient, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(IS_RECIPIENT_PARAM, isRecipient ? "1" : "0");
        params.put(CARD_ID_PARAM, cardId);

        postWithRelativeUrl("delete_card.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void makeDefaultCard(String cardId, boolean isRecipient, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        params.put(IS_RECIPIENT_PARAM, isRecipient ? "1" : "0");
        params.put(CARD_ID_PARAM, cardId);

        postWithRelativeUrl("make_default_card.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public void toggleNotificationsEnabled(Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put(SESSION_ID_PARAM, SeshAuthManager.sharedManager(mContext).getAccessToken());
        postWithRelativeUrl("toggle_notifications_enabled.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
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

    public void uploadProfilePicture(Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener, File image) {
        Map<String, String> params = new HashMap<>();
        String url = baseUrl() + apiUrl(RequestType.DJANGO) + "accounts/users/upload_profile_picture/";
        JsonMultipartRequest request = new JsonMultipartRequest(url, mContext, successListener, errorListener, image);

        VolleyNetworkingWrapper.getInstance(mContext).addToRequestQueue(request);
    }

    public void editTutorClasses(List<Course> classesToAdd,
                                 List<Course> classesToDelete,
                                 List<Department> departmentsToAdd,
                                 List<Department> departmentsToDelete,
                                 Response.Listener<JSONObject> successListener,
                                 Response.ErrorListener errorListener) {
        JSONArray courseIdsToAdd = new JSONArray();
        for (Course course : classesToAdd) {
            courseIdsToAdd.put(course.courseId);
        }

        JSONArray courseIdsToDelete = new JSONArray();
        for (Course course : classesToDelete) {
            courseIdsToDelete.put(course.courseId);
        }

        JSONArray deptIdsToAdd = new JSONArray();
        for (Department department : departmentsToAdd) {
            deptIdsToAdd.put(department.departmentId);
        }

        JSONArray deptIdsToDelete = new JSONArray();
        for (Department department : departmentsToDelete) {
            deptIdsToDelete.put(department.departmentId);
        }

        JSONObject params = new JSONObject();
        try {
            params.put("session_id", SeshAuthManager.sharedManager(mContext).getAccessToken());
            params.put(CLASSES_TO_ADD_ID, courseIdsToAdd);
            params.put(CLASSES_TO_DELETE_ID, courseIdsToDelete);
            params.put(DEPARTMENTS_TO_ADD_ID, deptIdsToAdd);
            params.put(DEPARTMENTS_TO_DELETE_ID, deptIdsToDelete);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        postWithRelativeUrl("edit_tutor_classes.php", params, RequestType.PHP, RequestMethod.POST, successListener, errorListener);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(contentUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

}
