package com.seshtutoring.seshapp.services.notifications.handlers;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.PastRequest;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.RequestTimeoutButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 8/20/15.
 */
public class RequestTimeoutNotificationHandler extends NotificationHandler {
    private static final String TAG = RequestTimeoutNotificationHandler.class.getName();
    public static final String REQUEST_TIMEOUT_DIALOG_TYPE = "request_dialog";

    public RequestTimeoutNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    @Override
    public void handleDisplayInsideApp() {
        int pastRequestId = (int) mNotification.getDataObject("past_request_id");
        final int requestId = (int) mNotification.getDataObject("request_id");

        SeshNetworking seshNetworking = new SeshNetworking(mContext);
        seshNetworking.getPastRequestInformationForPastRequestId(pastRequestId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        List<LearnRequest> learnRequestsFound =
                                LearnRequest.find(LearnRequest.class, "learn_request_id = ?", Integer.toString(requestId));

                        if (learnRequestsFound.size() > 0) {
                            LearnRequest currentLearnRequest = learnRequestsFound.get(0);
                            currentLearnRequest.delete();
                        }

                        PastRequest pastRequest = PastRequest.createOrUpdatePastRequest(jsonObject.getJSONObject("past_request"));

                        showDialog(pastRequest, false);
                    } else {
                        Log.e(TAG, "Failed to get past request information: " + jsonObject.getString("message"));
                        mNotification.handled(mContext, false);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to get past request information; json malformed: " + e);
                    mNotification.handled(mContext, false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to get past request information; network error: " + volleyError);
                mNotification.handled(mContext, false);
            }
        });
    }

    private void showDialog(final PastRequest pastRequest, boolean withDelay) {
        final SeshDialog seshDialog = new SeshDialog();
        final SeshNetworking seshNetworking = new SeshNetworking(mContext);
        LayoutUtils utils = new LayoutUtils(mContext);

        LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentLayout = layoutInflater.inflate(R.layout.sesh_dialog_content_request_timeout, null);

        final RequestTimeoutButton oneHourButton = (RequestTimeoutButton) contentLayout.findViewById(R.id.one_hour_button);
        final RequestTimeoutButton threeHoursButton = (RequestTimeoutButton) contentLayout.findViewById(R.id.three_hours_button);
        final RequestTimeoutButton fiveHoursButton = (RequestTimeoutButton) contentLayout.findViewById(R.id.five_hours_button);

        final Button firstDialogButton = (Button) contentLayout.findViewById(R.id.request_timeout_first_dialog_btn);
        final Button secondDialogButton = (Button) contentLayout.findViewById(R.id.request_timeout_second_dialog_btn);

        firstDialogButton.setTypeface(utils.getMediumGothamTypeface());
        secondDialogButton.setTypeface(utils.getMediumGothamTypeface());

        // custom buttons are used in order to hide first dialog button when no retry period is selected
        seshDialog.setDialogType(SeshDialog.SeshDialogType.CUSTOM_BUTTONS);

        oneHourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFirstDialogButtonIfInvisible(firstDialogButton);

                oneHourButton.setSelected(true);
                threeHoursButton.setSelected(false);
                fiveHoursButton.setSelected(false);
            }
        });

        threeHoursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFirstDialogButtonIfInvisible(firstDialogButton);

                oneHourButton.setSelected(false);
                threeHoursButton.setSelected(true);
                fiveHoursButton.setSelected(false);
            }
        });

        fiveHoursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFirstDialogButtonIfInvisible(firstDialogButton);

                oneHourButton.setSelected(false);
                threeHoursButton.setSelected(false);
                fiveHoursButton.setSelected(true);
            }
        });

        firstDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seshDialog.setNetworking(true);

                LearnRequest learnRequest = LearnRequest.learnRequestFromPastRequest(pastRequest);

                if (oneHourButton.isSelected()) {
                    learnRequest.createAvailableBlockForNow(1);
                } else if (threeHoursButton.isSelected()) {
                    learnRequest.createAvailableBlockForNow(3);
                } else if (fiveHoursButton.isSelected()) {
                    learnRequest.createAvailableBlockForNow(5);
                }

                final Runnable dismissCallback = new Runnable() {
                    @Override
                    public void run() {
                        mNotification.handled(mContext, true);
                    }
                };

                seshNetworking.createRequestWithLearnRequest(learnRequest, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject.getString("status").equals("SUCCESS")) {
                                LearnRequest.createOrUpdateLearnRequest(jsonObject.getJSONObject("learn_request"));
                                seshDialog.setNetworking(false);
                                mNotification.handled(mContext, true);
                            } else {
                                Log.e(TAG, "Failed to recreate Learn request: " + jsonObject.getString("message"));
                                seshDialog.networkOperationFailed("Error!", jsonObject.getString("message"), "OKAY",
                                        dismissCallback);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to recreate Learn request; json malformed: " + e);
                            seshDialog.networkOperationFailed("Error!", "Something went wrong.  Try again later.", "OKAY", dismissCallback);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, "Failed to recreate Learn request; network error: " + volleyError);
                        seshDialog.networkOperationFailed("Network Error", "We couldn't reach the server.  Try again later.", "OKAY", dismissCallback);
                    }
                });
            }
        });

        secondDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotification.handled(mContext, true);
                seshDialog.dismiss(2);
            }
        });

        seshDialog.setContentLayout(contentLayout);
        seshDialog.setTitle("No Tutors Available");

        String messageTemplate = mContext.getResources().getString(R.string.sesh_request_timeout_dialog_text);
        seshDialog.setMessage(String.format(messageTemplate, pastRequest.classString));
        seshDialog.setType(REQUEST_TIMEOUT_DIALOG_TYPE);

        if (withDelay) {
            seshDialog.showWithDelay(ApplicationLifecycleTracker
                    .sharedInstance(mContext)
                    .getActivityInForeground()
                    .getFragmentManager(), null, 2000);
        } else {
            seshDialog.show(ApplicationLifecycleTracker
                    .sharedInstance(mContext)
                    .getActivityInForeground()
                    .getFragmentManager(), null);
        }

    }

    private void showFirstDialogButtonIfInvisible(Button firstButton) {
        if (firstButton.getAlpha() == 0) {
            firstButton.animate().alpha(1).setDuration(300).start();
        }
    }
}