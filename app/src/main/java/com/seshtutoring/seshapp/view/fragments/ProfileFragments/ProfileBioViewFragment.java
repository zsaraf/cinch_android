package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.squareup.picasso.Callback;


import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshIconTextView;

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

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        View v = layoutInflater.inflate(R.layout.profile_bio_fragment, null);

        mainContainerActivity = (MainContainerActivity) getActivity();
        user = User.currentUser(mainContainerActivity.getApplicationContext());

        schoolView = (SeshIconTextView) v.findViewById(R.id.school_name);
        emailView = (SeshIconTextView) v.findViewById(R.id.email);
        majorView = (SeshIconTextView) v.findViewById(R.id.major);
        bioView = (TextView) v.findViewById(R.id.bio);
        TextView editButton = (TextView) v.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show web view
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("https://www.seshtutoring.com/registration"));
                mainContainerActivity.startActivity(viewIntent);
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
