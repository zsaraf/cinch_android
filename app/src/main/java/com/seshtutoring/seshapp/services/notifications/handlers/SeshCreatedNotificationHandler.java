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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.maps.model.CircleOptions;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker.ApplicationResumeListener;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.SeshNetworking.SynchronousRequest;
import com.seshtutoring.seshapp.view.InSeshActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.SeshActivity;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragment;
import com.squareup.picasso.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nadavhollander on 8/21/15.
 */
public class SeshCreatedNotificationHandler extends NotificationHandler {
    private static final String TAG = SeshCreatedNotificationHandler.class.getName();
    public static final String SESH_CREATED_DIALOG_TYPE = "SESH_CREATED";

    public SeshCreatedNotificationHandler(Notification notification, Context context) {
        super(notification, context);
    }

    public void handleDisplayInsideApp() {
        final int requestId = (int) mNotification.getDataObject("request_id");
        final int seshId = (int) mNotification.getDataObject("sesh_id");

        final SeshNetworking seshNetworking = new SeshNetworking(mContext);
        SynchronousRequest request = new SynchronousRequest() {
            public void request(RequestFuture<JSONObject> blocker) {
                seshNetworking.getSeshInformationForSeshId(seshId, blocker, blocker);
            }

            public void onErrorException(Exception e) {
                Log.e(TAG, "Failed to get sesh info for sesh id: " + e);
                mNotification.handled(mContext, false);
            }
        };

        JSONObject jsonObject = request.execute();

        if (jsonObject != null) {
            try {
                if (jsonObject.getString("status").equals("SUCCESS")) {
                    List<LearnRequest> learnRequestsFound
                            = LearnRequest.find(LearnRequest.class,
                            "learn_request_id = ?", Integer.toString(requestId));
                    if (learnRequestsFound.size() > 0) {
                        learnRequestsFound.get(0).delete();
                    }
                    final Sesh createdSesh = Sesh.createOrUpdateSeshWithObject(jsonObject.getJSONObject("sesh"), mContext);
                    createdSesh.requiresAnimatedDisplay = true;
                    createdSesh.save();

                    final ImageView profilePicture = new ImageView(mContext);
                    loadImage(profilePicture, new Callback() {
                        @Override
                        public void onSuccess() {
                            showDialog(createdSesh, profilePicture);
                        }
                        @Override
                        public void onError() {
                            showDialog(createdSesh, profilePicture);
                        }
                    });
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to get sesh info for sesh id; malformed response: " + e);
                mNotification.handled(mContext, false);
            }
        }
    }

    private void showDialog(final Sesh createdSesh, ImageView profilePicture) {
        LayoutUtils utils = new LayoutUtils(mContext);

        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);

        LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentLayout = layoutInflater.inflate(R.layout.sesh_dialog_content_sesh_created, null);

        TextView studentTutorName = (TextView) contentLayout.findViewById(R.id.student_tutor_name);
        studentTutorName.setText(createdSesh.abbreviatedNameForOtherPerson());
        studentTutorName.setTypeface(utils.getLightGothamTypeface());

        CircleImageView circleImageView = (CircleImageView) contentLayout.findViewById(R.id.profile_image);
        circleImageView.setImageDrawable(profilePicture.getDrawable());

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

        final SeshActivity foregroundActivity = (SeshActivity) ApplicationLifecycleTracker
                .sharedInstance(mContext)
                .getActivityInForeground();

        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (foregroundActivity.isMainContainerActivity()) {
                    Intent intent = new Intent(MainContainerActivity.DISPLAY_SIDE_MENU_UPDATE);
                    intent.putExtra(ViewSeshFragment.SESH_KEY, createdSesh.seshId);
                    mContext.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(MainContainerActivity.DISPLAY_SIDE_MENU_UPDATE, null,
                            mContext, MainContainerActivity.class);
                    intent.putExtra(ViewSeshFragment.SESH_KEY, createdSesh.seshId);
                    foregroundActivity.startActivity(intent);
                }

                mNotification.handled(mContext, true);
                seshDialog.dismiss();
            }
        });

        seshDialog.show(foregroundActivity
                .getFragmentManager(), null);

    }
}
