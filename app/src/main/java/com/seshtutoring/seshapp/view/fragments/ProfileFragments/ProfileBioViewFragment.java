package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.view.EditProfileActivity;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.squareup.picasso.Callback;


import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshIconTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by lillioetting on 8/28/15.
 */
public class ProfileBioViewFragment extends Fragment implements MainContainerActivity.FragmentOptionsReceiver {
    private Map<String, Object> options;
    private User user;
    private MainContainerActivity mainContainerActivity;
    private SeshIconTextView schoolView;
    private SeshIconTextView emailView;
    private SeshIconTextView majorView;
    private TextView bioView;
    private BroadcastReceiver broadcastReceiver;
    private SeshNetworking seshNetworking;

    private String oldBio;
    private String oldMajor;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        View v = layoutInflater.inflate(R.layout.profile_bio_fragment, null);

        mainContainerActivity = (MainContainerActivity) getActivity();
        user = User.currentUser(mainContainerActivity.getApplicationContext());

        oldBio = "";
        oldMajor = "";

        seshNetworking = new SeshNetworking(getActivity());

        schoolView = (SeshIconTextView) v.findViewById(R.id.school_name);
        emailView = (SeshIconTextView) v.findViewById(R.id.email);
        majorView = (SeshIconTextView) v.findViewById(R.id.major);
        bioView = (TextView) v.findViewById(R.id.bio);
        TextView editButton = (TextView) v.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //show web view
//                Intent viewIntent =
//                        new Intent("android.intent.action.VIEW",
//                                Uri.parse("https://www.seshtutoring.com/registration"));
//                mainContainerActivity.startActivity(viewIntent);
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("curr_bio", bioView.getText().toString());
                intent.putExtra("curr_major", majorView.getText().toString());
                startActivityForResult(intent, EditProfileActivity.EDIT_PROFILE_REQUEST);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.hold);

            }
        });

        schoolView.setIconResourceId(R.drawable.university_big);
        emailView.setIconResourceId(R.drawable.email_icon);
        majorView.setIconResourceId(R.drawable.book_orange);

        refreshBioViewWithUser(user);

        broadcastReceiver = actionBroadcastReceiver;


        return v;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (EditProfileActivity.EDIT_PROFILE_REQUEST):
                if (resultCode == EditProfileActivity.RESULT_OK) {
                    String bio = data.getStringExtra("bio");
                    String major = data.getStringExtra("major");
                    oldBio = bioView.getText().toString();
                    oldMajor = majorView.getText().toString();
                    if (!bio.isEmpty()) {
                        bioView.setText(bio);
                    }
                    if (!major.isEmpty()) {
                        majorView.setText(major);
                    }
                    seshNetworking.updateUserInformationWithMajorAndBio(major, bio, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject responseJson) {
                            onUpdateInfoResponse(responseJson);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            onUpdateInfoFailure(volleyError.getMessage());
                        }
                    });
                }else if (resultCode == EditProfileActivity.RESULT_CANCELED) {
                    break;
                }
            default:
                break;
        }
    }

    private void onUpdateInfoResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                //success!!
                bioView.setTextColor(getResources().getColor(R.color.seshlightgray));
                majorView.setTextColor(getResources().getColor(R.color.seshlightgray));
                user.major = majorView.getText().toString();
                user.bio = bioView.getText().toString();
                if (bioView.getText().equals("edit profile to add bio")) {
                    bioView.setTextColor(getResources().getColor(R.color.light_gray));
                    user.bio = "";
                }
                if (majorView.getText().equals("edit profile to add major")) {
                    majorView.setTextColor(getResources().getColor(R.color.light_gray));
                    user.major = "";
                }
                user.save();

            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                showErrorDialog("Whoops!", message);
                bioView.setText(oldBio);
                majorView.setText(oldMajor);
            }
        } catch (JSONException e) {
            showErrorDialog("Whoops!", "There was an error updating your profile, please try again later.");
            bioView.setText(oldBio);
            majorView.setText(oldMajor);
        }
    }

    private void onUpdateInfoFailure(String errorMessage) {
        //show error and revert to old profile values
        showErrorDialog("Whoops!", "There was an error, please check your network connection and try again.");
        bioView.setText(oldBio);
        majorView.setText(oldMajor);
    }

    private void showErrorDialog(String title, String message) {
        SeshDialog.showDialog(getActivity().getFragmentManager(), title, message,
                "OKAY", null, "view_request_network_error");
    }


    @Override
    public void onResume() {
        super.onResume();

        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.REFRESH_USER_INFO);
        this.mainContainerActivity.registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        this.mainContainerActivity.unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            User currentUser = User.currentUser(context);
            refreshBioViewWithUser(currentUser);
        }
    };

    public void refreshBioViewWithUser(User user) {
        this.user = user;

        emailView.setText(user.email);
        schoolView.setText(user.school.schoolName);


        String major = "edit profile to add major";
        if (user.major != null && !user.major.isEmpty()) {
            major = user.major;
            majorView.setTextColor(getResources().getColor(R.color.seshlightgray));
        }else {
            majorView.setTextColor(getResources().getColor(R.color.light_gray));
        }
        majorView.setText(major);

        String bio = "edit profile to add bio";
        if (user.bio != null && !user.bio.isEmpty()) {
            bio = user.bio;
            bioView.setTextColor(getResources().getColor(R.color.seshlightgray));
        }else {
            bioView.setTextColor(getResources().getColor(R.color.light_gray));
        }
        bioView.setText(bio);



    }

    @Override
    public void updateFragmentOptions(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public void clearFragmentOptions() {
        this.options = null;
    }

}
