package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        View v = layoutInflater.inflate(R.layout.profile_bio_fragment, null);

        mainContainerActivity = (MainContainerActivity) getActivity();
        user = User.currentUser(mainContainerActivity.getApplicationContext());

        SeshIconTextView schoolView = (SeshIconTextView) v.findViewById(R.id.school_name);
        SeshIconTextView emailView = (SeshIconTextView) v.findViewById(R.id.email);
        SeshIconTextView majorView = (SeshIconTextView) v.findViewById(R.id.major);
        TextView bioView = (TextView) v.findViewById(R.id.bio);
        Button editButton = (Button) v.findViewById(R.id.edit_button);

        schoolView.setText(user.school.schoolName);
        schoolView.setIconResourceId(R.drawable.university_big);
        emailView.setText(user.email);
        emailView.setIconResourceId(R.drawable.email_icon);
        majorView.setText(user.major);
        majorView.setIconResourceId(R.drawable.book);
        bioView.setText(user.bio);

//        final ImageView profileImageView = (ImageView)v.findViewById(R.id.profile_picture);
//        SeshNetworking seshNetworking = new SeshNetworking(mainContainerActivity);
//        seshNetworking.downloadProfilePicture(user.getProfilePictureUrl(), profileImageView, new Callback() {
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onError() {
//
//            }
//        });
        return v;

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
