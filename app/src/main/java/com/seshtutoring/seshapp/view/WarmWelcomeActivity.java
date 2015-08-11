package com.seshtutoring.seshapp.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.fragments.WarmWelcomeFragments.FirstWelcomeFragment;
import com.seshtutoring.seshapp.view.fragments.WarmWelcomeFragments.FourthWelcomeFragment;
import com.seshtutoring.seshapp.view.fragments.WarmWelcomeFragments.SecondWelcomeFragment;
import com.seshtutoring.seshapp.view.fragments.WarmWelcomeFragments.ThirdWelcomeFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nadavhollander on 8/10/15.
 */
public class WarmWelcomeActivity extends SeshActivity {
    private ViewPager viewPager;
    private FragmentPagerAdapter pagerAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.warm_welcome_activity);

        this.viewPager = (ViewPager) findViewById(R.id.warm_welcome_view_pager);
        viewPager.setAdapter(new WarmWelcomePagerAdapter(getSupportFragmentManager()));
        viewPager.setPageTransformer(true, new FadePageTransformer());
    }

    private class WarmWelcomePagerAdapter extends FragmentPagerAdapter {
        private Fragment[] warmWelcomeFragments = {
                new FirstWelcomeFragment(),
                new SecondWelcomeFragment(),
                new ThirdWelcomeFragment(),
                new FourthWelcomeFragment()
        };

        public WarmWelcomePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int i) {
            return warmWelcomeFragments[i];
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    private class FadePageTransformer implements ViewPager.PageTransformer {
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            View imageView = view.findViewById(R.id.background_image);
            View contentView = view.findViewById(R.id.content_area);

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left
            } else if (position <= 0) { // [-1,0]
                // This page is moving out to the left

                // Counteract the default swipe
                view.setTranslationX(pageWidth * -position);
                if (contentView != null) {
                    // But swipe the contentView
                    contentView.setTranslationX(pageWidth * position);
                }
                if (imageView != null) {
                    // Fade the image in
                    imageView.setAlpha(1 + position);
                }

            } else if (position <= 1) { // (0,1]
                // This page is moving in from the right

                // Counteract the default swipe
                view.setTranslationX(pageWidth * -position);
                if (contentView != null) {
                    // But swipe the contentView
                    contentView.setTranslationX(pageWidth * position);
                }
                if (imageView != null) {
                    // Fade the image out
                    imageView.setAlpha(1 - position);
                }
            } else { // (1,+Infinity]
                // This page is way off-screen to the right
            }
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
