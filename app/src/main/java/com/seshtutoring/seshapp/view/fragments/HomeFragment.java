package com.seshtutoring.seshapp.view.fragments;

import android.content.res.Resources;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.seshtutoring.seshapp.R;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class HomeFragment extends Fragment {
    private ViewPager viewPager;
    private LearnViewFragment learnViewFragment;
    private TeachViewFragment teachViewFragment;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View homeView = layoutInflater.inflate(R.layout.home_fragment, container, false);
        
        this.learnViewFragment = new LearnViewFragment();
        this.teachViewFragment = new TeachViewFragment();

        this.viewPager = (ViewPager) homeView.findViewById(R.id.view_pager);
        this.viewPager.setAdapter(new HomeViewPagerAdapter(getChildFragmentManager()));

        final Button learnTabButton = (Button) homeView.findViewById(R.id.learn_button);
        final Button teachTabButton = (Button) homeView.findViewById(R.id.teach_button);

        learnTabButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewPager.setCurrentItem(0, true);
                learnTabButton.setTextColor(getResources().getColor(R.color.seshorange));
                teachTabButton.setTextColor(getResources().getColor(R.color.seshcharcoal));

                learnTabButton.setPressed(true);
                return false;
            }
        });

        teachTabButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewPager.setCurrentItem(1, true);
                teachTabButton.setTextColor(getResources().getColor(R.color.seshorange));
                learnTabButton.setTextColor(getResources().getColor(R.color.seshcharcoal));

                teachTabButton.setPressed(true);
                return false;
            }
        });

        int currentItem = this.viewPager.getCurrentItem();
        if (currentItem == 0) {
            learnTabButton.setTextColor(getResources().getColor(R.color.seshorange));
        } else {
            teachTabButton.setTextColor(getResources().getColor(R.color.seshorange));
        }

        return homeView;
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
}
