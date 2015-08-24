package com.seshtutoring.seshapp.services.notifications.handlers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker.ApplicationResumeListener;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.InSeshActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 8/21/15.
 */
public class SeshCreatedNotificationHandler extends NotificationHandler {
    private static final String TAG = SeshCreatedNotificationHandler.class.getName();
    public static final String SESH_CREATED_DIALOG_TYPE = "SESH_CREATED";

    public SeshCreatedNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    public void handle() {
        final int requestId = (int) mNotification.getDataObject("request_id");
        int seshId = (int) mNotification.getDataObject("sesh_id");
        SeshNetworking seshNetworking = new SeshNetworking(mContext);
        seshNetworking.getSeshInformationForSeshId(seshId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        List<LearnRequest> learnRequestsFound
                                = LearnRequest.find(LearnRequest.class,
                                "learn_request_id = ?", Integer.toString(requestId));
                        if (learnRequestsFound.size() > 0) {
                            learnRequestsFound.get(0).delete();
                        }

                        final Sesh createdSesh = Sesh.createOrUpdateSeshWithObject(jsonObject.getJSONObject("sesh"), mContext);

                        ApplicationLifecycleTracker applicationLifecycleTracker = ApplicationLifecycleTracker.sharedInstance(mContext);
                        if (applicationLifecycleTracker.applicationInForeground()) {
                            showDialog(createdSesh);
                        } else {
                            applicationLifecycleTracker.setApplicationResumeListener(new ApplicationResumeListener() {
                                @Override
                                public void onApplicationResume() {
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog(createdSesh);
                                        }
                                    }, 1500);
                                }
                            });
                        }

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to get sesh info for sesh id; malformed response: " + e);
                    mNotification.handled(mContext, false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Failed to get sesh info for sesh id; network error: " + error);
                mNotification.handled(mContext, false);
            }
        });
    }

    private void showDialog(Sesh createdSesh) {
        LayoutUtils utils = new LayoutUtils(mContext);

        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);

        LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentLayout = layoutInflater.inflate(R.layout.sesh_dialog_content_sesh_created, null);

        TextView studentTutorName = (TextView) contentLayout.findViewById(R.id.student_tutor_name);
        studentTutorName.setText(createdSesh.abbreviatedNameForOtherPerson());
        studentTutorName.setTypeface(utils.getLightGothamTypeface());

        seshDialog.setContentLayout(contentLayout);

        String titleString;
        String messageString;
        if (createdSesh.isStudent) {
            titleString = mContext.getResources().getString(R.string.sesh_created_student_dialog_title);
            messageString = String.format(mContext.getResources().getString(R.string.sesh_created_student_dialog_text), createdSesh.firstName());
        } else {
            titleString = mContext.getResources().getString(R.string.sesh_created_tutor_dialog_title);
            messageString = String.format(mContext.getResources().getString(R.string.sesh_created_tutor_dialog_text), createdSesh.firstName());
        }

        seshDialog.setTitle(titleString);
        seshDialog.setMessage(messageString);

        seshDialog.setFirstChoice("OKAY");
        seshDialog.setType(SESH_CREATED_DIALOG_TYPE);

        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotification.handled(mContext, true);
                seshDialog.dismiss();
            }
        });

        seshDialog.show(ApplicationLifecycleTracker
                .sharedInstance(mContext)
                .getActivityInForeground()
                .getFragmentManager(), null);

    }
}
