package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
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
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;
import com.seshtutoring.seshapp.view.fragments.TeachViewFragment;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentFlagReceiver;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class HomeFragment extends Fragment implements FragmentFlagReceiver {
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
    private LearnViewFragment learnViewFragment;
    private TeachViewFragment teachViewFragment;
    private Button learnTabButton;
    private Button teachTabButton;
    private String fragmentFlag;
    private TabItem currTabItem;

    public final static String SHOW_AVAILABLE_JOBS_FLAG = "show_available_jobs";

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        homeView = layoutInflater.inflate(R.layout.home_fragment, container, false);

        this.learnViewFragment = new LearnViewFragment();
        this.teachViewFragment = new TeachViewFragment();

        this.viewPager = (ViewPager) homeView.findViewById(R.id.view_pager);
        this.viewPager.setAdapter(new HomeViewPagerAdapter(getChildFragmentManager()));

        LayoutUtils utils = new LayoutUtils(getActivity());

        LinearLayout tabButtons = (LinearLayout) homeView.findViewById(R.id.home_tabs);
        LinearLayout.MarginLayoutParams margins = (LinearLayout.MarginLayoutParams) tabButtons.getLayoutParams();
        margins.topMargin = utils.getActionBarHeightPx();
        tabButtons.setLayoutParams(margins);

        learnTabButton = (Button) homeView.findViewById(R.id.learn_button);
        teachTabButton = (Button) homeView.findViewById(R.id.teach_button);

        learnTabButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                learnTabButton.setPressed(true);
                setCurrentTabItem(TabItem.LEARN_TAB, true);
                return false;
            }
        });

        teachTabButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                teachTabButton.setPressed(true);
                setCurrentTabItem(TabItem.TEACH_TAB, true);
                return false;
            }
        });


        if (fragmentFlag != null && fragmentFlag.equals(SHOW_AVAILABLE_JOBS_FLAG)) {
            setCurrentTabItem(TabItem.TEACH_TAB, false);
        } else {
            setCurrentTabItem(TabItem.LEARN_TAB, false);
        }

        return homeView;
    }


    private void setCurrentTabItem(TabItem tabItem, boolean withAnimation) {
        viewPager.setCurrentItem(tabItem.viewPagerPosition, withAnimation);
        if (tabItem == TabItem.LEARN_TAB) {
            learnTabButton.setTextColor(getResources().getColor(R.color.seshorange));
            teachTabButton.setTextColor(getResources().getColor(R.color.seshcharcoal));
        } else if (tabItem == TabItem.TEACH_TAB) {
            teachTabButton.setTextColor(getResources().getColor(R.color.seshorange));
            learnTabButton.setTextColor(getResources().getColor(R.color.seshcharcoal));
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
        return learnViewFragment.getMap();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (fragmentFlag != null && fragmentFlag.equals(SHOW_AVAILABLE_JOBS_FLAG)) {
            setCurrentTabItem(TabItem.TEACH_TAB, false);
            Log.d(TAG, "Fragment flag worked");
            // show available jobs view
        }
    }

    private class HomeViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_TABS = 2;

        public HomeViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return (position == 0) ? learnViewFragment : teachViewFragment;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        learnViewFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void updateFragmentFlag(String flag) {
        this.fragmentFlag = flag;
        if (flag.equals(SHOW_AVAILABLE_JOBS_FLAG)) {
            if (isAdded()) {
                setCurrentTabItem(TabItem.TEACH_TAB, false);
            }
        }
    }

    @Override
    public void clearFragmentFlag() {
        this.fragmentFlag = null;
    }
}
