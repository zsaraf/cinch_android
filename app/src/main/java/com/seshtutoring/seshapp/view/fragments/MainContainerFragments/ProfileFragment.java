package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.content.Context;
import android.media.Image;
import android.os.Parcelable;
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

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;
import com.seshtutoring.seshapp.view.components.SeshIconTextView;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileBioViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileStudentViewFragment;
import com.seshtutoring.seshapp.view.fragments.ProfileFragments.ProfileTutorViewFragment;
import com.seshtutoring.seshapp.view.fragments.TeachViewFragment;
import com.squareup.picasso.Callback;

import org.w3c.dom.Text;

import java.util.Map;

/**
 * Created by nadavhollander on 7/14/15.
 */

public class ProfileFragment extends Fragment implements FragmentOptionsReceiver {
    private Map<String, Object> options;
    private User user;
    private MainContainerActivity mainContainerActivity;
    private View homeView;
    private ViewPager viewPager;
    private ImageView viewPagerDots;
    private ProfileBioViewFragment profileBioViewFragment;
    private ProfileStudentViewFragment profileStudentViewFragment;
    private ProfileTutorViewFragment profileTutorViewFragment;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.homeView = layoutInflater.inflate(R.layout.profile_fragment, null);

        LayoutUtils layUtils = new LayoutUtils(getActivity());
        this.homeView.setPadding(0, layUtils.getActionBarHeightPx(), 0, 0);

        mainContainerActivity = (MainContainerActivity) getActivity();
        user = User.currentUser(mainContainerActivity.getApplicationContext());

        this.profileBioViewFragment = new ProfileBioViewFragment();
        this.profileStudentViewFragment = new ProfileStudentViewFragment();
        this.profileTutorViewFragment = new ProfileTutorViewFragment();

        this.viewPagerDots = (ImageView) this.homeView.findViewById(R.id.view_pager_dots);
        this.viewPagerDots.setImageResource(R.drawable.sign_up_dots_page1);

        this.viewPager = (ViewPager) this.homeView.findViewById(R.id.view_pager);
        this.viewPager.setAdapter(new ProfileViewPagerAdapter(getChildFragmentManager()));
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

        final ImageView profileImageView = (ImageView)this.homeView.findViewById(R.id.profile_picture);
        SeshNetworking seshNetworking = new SeshNetworking(mainContainerActivity);
        seshNetworking.downloadProfilePictureAsync(user.profilePictureUrl, profileImageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });

        return this.homeView;

    }

    private class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_TABS = 3;

        public ProfileViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return profileBioViewFragment;
            }else if (position == 1) {
                return profileStudentViewFragment;
            }else {
                return profileTutorViewFragment;
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
