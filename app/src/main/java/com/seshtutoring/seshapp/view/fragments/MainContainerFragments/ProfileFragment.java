package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.JsonMultipartRequest;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.VolleyNetworkingWrapper;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;
import com.seshtutoring.seshapp.view.components.SeshIconTextView;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileBioViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileStudentViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileTutorViewFragment;
import com.seshtutoring.seshapp.view.fragments.TeachViewFragment;
import com.squareup.picasso.Callback;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nadavhollander on 7/14/15.
 */

public class ProfileFragment extends Fragment implements FragmentOptionsReceiver {

    private static final int SELECT_PICTURE = 1;

    private Map<String, Object> options;
    private User user;
    private MainContainerActivity mainContainerActivity;
    private View homeView;
    private ViewPager viewPager;
    private ImageView viewPagerDots;
    private SeshNetworking seshNetworking;
    private ImageView profileImageView;
    private ProfileBioViewFragment profileBioViewFragment;
    private ProfileStudentViewFragment profileStudentViewFragment;
    private ProfileTutorViewFragment profileTutorViewFragment;

    private CallbackManager callbackManager;

    private String selectedImagePath;


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        mainContainerActivity = (MainContainerActivity) getActivity();

        this.homeView = layoutInflater.inflate(R.layout.profile_fragment, null);

        LayoutUtils layUtils = new LayoutUtils(getActivity());
        this.homeView.setPadding(0, layUtils.getActionBarHeightPx(), 0, 0);

        user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        //initializing fragments here might contribute to detachment issues, I think we're supposed to initialize in viewPager.getItem() instead so viewPager tracks reference
        //this.profileBioViewFragment = new ProfileBioViewFragment();
        //this.profileStudentViewFragment = new ProfileStudentViewFragment();
        //this.profileTutorViewFragment = new ProfileTutorViewFragment();

        this.viewPagerDots = (ImageView) this.homeView.findViewById(R.id.view_pager_dots);
        this.viewPagerDots.setImageResource(R.drawable.sign_up_dots_page1);

        this.viewPager = (ViewPager) this.homeView.findViewById(R.id.view_pager);
        this.viewPager.setAdapter(new ProfileViewPagerAdapter(getFragmentManager()));
        this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    viewPagerDots.setImageResource(R.drawable.sign_up_dots_page1);
                } else if (position == 1) {
                    viewPagerDots.setImageResource(R.drawable.sign_up_dots_page2);
                } else if (position == 2) {
                    viewPagerDots.setImageResource(R.drawable.sign_up_dots_page3);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        this.profileImageView = (ImageView)this.homeView.findViewById(R.id.profile_picture);
        SeshNetworking seshNetworking = new SeshNetworking(mainContainerActivity);
        seshNetworking.downloadProfilePictureAsync(user.profilePictureUrl, this.profileImageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });

        this.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Add code here for selecting new photo to upload

//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
//                mainContainerActivity.facebookLogin();

            }
        });

        return this.homeView;

    }

    private void onUploadResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                //success, update profile picture locally
                JSONObject userObj = (JSONObject) responseJson.get("user");
                String newProfilePicture = userObj.getString("profile_picture");
                user.profilePictureUrl = newProfilePicture;
                seshNetworking.downloadProfilePictureAsyncNoPlaceholder(user.profilePictureUrl, profileImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        String str = "Success";
                    }

                    @Override
                    public void onError() {
                        String str = "Fail";
                    }
                });

            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
            }
        } catch (JSONException e) {
        }
    }

    private class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_TABS = 3;

        public ProfileViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ProfileBioViewFragment();
            }else if (position == 1) {
                return new ProfileStudentViewFragment();
            }else {
                return new ProfileTutorViewFragment();
            }
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            // Do nothing here!!  A bit hacky, but this is a fix for something weird in Android's
            // way of handling ViewPagers within fragments
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainContainerActivity)getActivity()).onFragmentReplacedAndRendered();
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
