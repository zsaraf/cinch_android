package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Discount;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.SeshMixpanelAPI;
import com.seshtutoring.seshapp.view.AddTutorClassesActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MessagingActivity;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.TutorTermsActivity;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;
import com.seshtutoring.seshapp.view.fragments.TeachDisabledViewFragment;
import com.seshtutoring.seshapp.view.fragments.TeachViewFragment;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class HomeFragment extends Fragment implements FragmentOptionsReceiver {
    private static final String TAG = HomeFragment.class.getName();
    public enum TabItem {
        LEARN_TAB(0), TEACH_TAB(1);

        public int viewPagerPosition;

        TabItem(int position) {
            this.viewPagerPosition = position;
        }
    };

    private View homeView;
    private ViewPager viewPager;
    private Button learnTabButton;
    private Button teachTabButton;
    private TabItem currTabItem;
    private LinearLayout tabButtons;
    private BroadcastReceiver broadcastReceiver;

    private Map<String, Object> options;

    public final static String SHOW_AVAILABLE_JOBS_FLAG = "show_available_jobs";

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        homeView = layoutInflater.inflate(R.layout.home_fragment, container, false);

        // initialized in viewPager bc may be contributing to issues with detached fragments

        this.viewPager = (ViewPager) homeView.findViewById(R.id.view_pager);
        this.viewPager.setAdapter(new HomeViewPagerAdapter(getChildFragmentManager(), User.currentUser(getActivity()).tutor.enabled));

        LayoutUtils utils = new LayoutUtils(getActivity());

        tabButtons = (LinearLayout) homeView.findViewById(R.id.home_tabs);
        learnTabButton = (Button) homeView.findViewById(R.id.learn_button);
        teachTabButton = (Button) homeView.findViewById(R.id.teach_button);

        learnTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                learnTabButton.setPressed(true);
                setCurrentTabItem(TabItem.LEARN_TAB, true);
            }
        });

        teachTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teachTabButton.setPressed(true);
                setCurrentTabItem(TabItem.TEACH_TAB, true);
            }
        });

        if (options != null && options.containsKey(SHOW_AVAILABLE_JOBS_FLAG) &&
                (boolean) options.get(SHOW_AVAILABLE_JOBS_FLAG)) {
            setCurrentTabItem(TabItem.TEACH_TAB, false);
        } else {
            setCurrentTabItem(TabItem.LEARN_TAB, false);
        }

        Discount.displayDiscountNotificationIfNecessary(getActivity());

        SeshMixpanelAPI seshMixpanelAPI = ((SeshApplication)getActivity().getApplication()).getSeshMixpanelAPI();
        JSONObject props = new JSONObject();
        try {
            props.put("User School", User.currentUser(getActivity()).school.schoolName);
            seshMixpanelAPI.registerSuperProperties(props);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return homeView;
    }

    private void setCurrentTabItem(TabItem tabItem, boolean withAnimation) {
        viewPager.setCurrentItem(tabItem.viewPagerPosition, withAnimation);
        if (tabItem == TabItem.LEARN_TAB) {
            //turn off calls to refresh open jobs when we leave the teach tab
            ViewAvailableJobsFragment viewJobsFragment = (ViewAvailableJobsFragment)getActivity().getFragmentManager().findFragmentByTag("ViewJobsFragment");
            if (viewJobsFragment != null) {
                viewJobsFragment.stopRepeatingTask();
            }
            learnTabButton.setTextColor(getResources().getColor(R.color.seshorange));
            teachTabButton.setTextColor(getResources().getColor(R.color.seshextralightgray));
        } else if (tabItem == TabItem.TEACH_TAB) {
            //turn on calls to refresh open jobs when we enter the teach tab
            ViewAvailableJobsFragment viewJobsFragment = (ViewAvailableJobsFragment)getActivity().getFragmentManager().findFragmentByTag("ViewJobsFragment");
            if (viewJobsFragment != null) {
                viewJobsFragment.startRepeatingTask();
            }
            teachTabButton.setTextColor(getResources().getColor(R.color.seshorange));
            learnTabButton.setTextColor(getResources().getColor(R.color.seshextralightgray));
        }
        currTabItem = tabItem;
    }

    public TabItem getCurrTabItem() {
        return currTabItem;
    }

    public GoogleMap getLearnViewMap() {
        if (currTabItem != TabItem.LEARN_TAB) {
            Log.e(TAG, "Cannot pull learn view map when learn tab is not selected.");
            return null;
        }
        return ((HomeViewPagerAdapter)this.viewPager.getAdapter()).learnViewFragment.getMap();
    }
    @Override
    public void onResume() {
        super.onResume();

        /* Handle open with SHOW_AVAILABLE_JOBS_FLAG set to true */
        if (options != null && options.containsKey(SHOW_AVAILABLE_JOBS_FLAG) &&
                (boolean) options.get(SHOW_AVAILABLE_JOBS_FLAG)) {
            setCurrentTabItem(TabItem.TEACH_TAB, false);
            Log.d(TAG, "Fragment flag worked");
            // show available jobs view
        }

        broadcastReceiver = actionBroadcastReceiver;
        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.REFRESH_USER_INFO);
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /* Handle tutor enabled */
            ((HomeViewPagerAdapter)HomeFragment.this.viewPager.getAdapter()).setTutorEnabled(User.currentUser(getActivity()).tutor.enabled);

            /* Handle tutor terms */
            if (User.currentUser(getActivity()).tutor.enabled == true &&
                    User.currentUser(getActivity()).tutor.didAcceptTerms == false) {
                final SeshDialog seshDialog = new SeshDialog();
                seshDialog.setTitle("New Tutor!");
                seshDialog.setMessage("Congratulations on becoming a Sesh tutor! Before you can begin tutoring, you need to accept the Sesh Tutor Agreement. Your one step away from learning and earning!");
                seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
                seshDialog.setFirstChoice("OKAY");
                seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seshDialog.dismiss();
                        Intent intent = new Intent(getActivity(), TutorTermsActivity.class);
                        getActivity().startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.fade_in, 0);
                    }
                });
                seshDialog.setType("new_tutor");
                seshDialog.show(getActivity().getFragmentManager(), "new_tutor");
            }
        }
    };

    private class HomeViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_TABS = 2;
        public boolean tutorEnabled;
        public LearnViewFragment learnViewFragment;
        public TeachViewFragment teachViewFragment;
        public TeachDisabledViewFragment teachDisabledViewFragment;

        public HomeViewPagerAdapter(FragmentManager fm, boolean tutorEnabled) {
            super(fm);
            this.tutorEnabled = tutorEnabled;
        }

        public LearnViewFragment getLearnViewFragment() {
            if (learnViewFragment == null) {
                learnViewFragment = new LearnViewFragment();
            }
            return learnViewFragment;
        }

        public TeachViewFragment getTeachViewFragment() {
            if (teachViewFragment == null) {
                teachViewFragment = new TeachViewFragment();
            }
            return teachViewFragment;
        }

        public TeachDisabledViewFragment getTeachDisabledViewFragment() {
            if (teachDisabledViewFragment == null) {
                teachDisabledViewFragment = new TeachDisabledViewFragment();
            }
            return teachDisabledViewFragment;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return getLearnViewFragment();
            } else {
                if (tutorEnabled) {
                    return getTeachViewFragment();
                } else {
                    return getTeachDisabledViewFragment();
                }
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
            int i = 0;
        }

        public void setTutorEnabled(boolean tutorEnabled) {
            if (this.tutorEnabled != tutorEnabled) {
                this.tutorEnabled = tutorEnabled;
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AddTutorClassesActivity.ADD_TUTOR_CLASSES_CREATE) {
            ((HomeViewPagerAdapter)this.viewPager.getAdapter()).teachViewFragment.refreshTutorClasses();
        } else {
            ((HomeViewPagerAdapter)this.viewPager.getAdapter()).learnViewFragment.onActivityResult(requestCode, resultCode, data);
        }


    }

    @Override
    public void updateFragmentOptions(Map<String, Object> options) {
        this.options = options;

        if (options.containsKey(SHOW_AVAILABLE_JOBS_FLAG) && (boolean)options.get(SHOW_AVAILABLE_JOBS_FLAG)) {
            if (isAdded()) {
                setCurrentTabItem(TabItem.TEACH_TAB, false);
            }
        }
    }

    @Override
    public void clearFragmentOptions() {
        this.options = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        //turn off calls to refresh open jobs when we leave the teach tab
        if (getCurrTabItem() == TabItem.TEACH_TAB && User.currentUser(getActivity()) != null && User.currentUser(getActivity()).tutor.enabled) {
            ViewAvailableJobsFragment viewJobsFragment = (ViewAvailableJobsFragment) getActivity().getFragmentManager().findFragmentByTag("ViewJobsFragment");
            if (viewJobsFragment != null) {
                viewJobsFragment.stopRepeatingTask();
            }
        }
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }

    public void mapViewReady() {
        ((MainContainerActivity)getActivity()).onFragmentReplacedAndRendered();
    }
}
