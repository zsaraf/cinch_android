package com.seshtutoring.seshapp.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.view.components.LearnRequestProgressBar;
import com.seshtutoring.seshapp.view.components.RequestFlowViewPager;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestAssignmentFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestCourseFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestNumberOfStudentsFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestTimeFragment;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nadavhollander on 7/20/15.
 */
public class RequestActivity extends FragmentActivity implements EditText.OnEditorActionListener {
    private static final String TAG = RequestActivity.class.getName();
    private LearnRequestProgressBar progressBar;
    private RequestFlowViewPager viewPager;
    private ImageButton requestFlowNext;
    private ImageButton requestFlowClose;
    private Course selectedCourse = null;
    private String selectedAssignment = null;
    private int selectedNumberOfStudents = -1;
    private int selectedDuration = -1;

    private Fragment[] requestFlowFragments = {
            new LearnRequestCourseFragment(),
            new LearnRequestAssignmentFragment(),
            new LearnRequestNumberOfStudentsFragment(),
            new LearnRequestTimeFragment()
    };

    public interface InputFragment {
        public boolean isCompleted();
        public void saveValues();
    }

    @SuppressWarnings("all")
    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        setContentView(R.layout.request_transparent_layout);

        // set blurred background
        if (getIntent().hasExtra(LearnViewFragment.BLURRED_MAP_BITMAP_PATH_KEY)) {
            String path = getIntent().getStringExtra(LearnViewFragment.BLURRED_MAP_BITMAP_PATH_KEY);
            RelativeLayout requestLayoutBackground =
                    (RelativeLayout) findViewById(R.id.request_layout_background);

            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                requestLayoutBackground.setBackgroundDrawable(Drawable.createFromPath(path));
            } else {
                requestLayoutBackground.setBackground(Drawable.createFromPath(path));
            }
        } else {
            Log.e(TAG, "Blurred background not included with intent to Request Layout");
        }

        this.progressBar = (LearnRequestProgressBar) findViewById(R.id.learn_request_progress_bar);
        this.viewPager = (RequestFlowViewPager) findViewById(R.id.learn_request_view_pager);

        progressBar.setActiveIconIndex(0);

        viewPager.setAdapter(new LearnRequestPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // do nothing
            }

            @Override
            public void onPageSelected(int position) {
                progressBar.setActiveIconIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        });

        this.requestFlowClose = (ImageButton) findViewById(R.id.request_flow_close);
        this.requestFlowNext = (ImageButton) findViewById(R.id.request_flow_next);
        requestFlowClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard();
            }
        }, 500);
    }

    private class LearnRequestPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_FRAGMENTS = 4;

        public LearnRequestPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return requestFlowFragments[position];
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }
    }

    public void nextFragment() {
        int currentItemIndex = viewPager.getCurrentItem();
        InputFragment currentFragment = (InputFragment) requestFlowFragments[currentItemIndex];

        // check if current fragment is completed
        if (currentFragment.isCompleted()) {
            if (currentItemIndex < 3) {
                currentFragment.saveValues();
                viewPager.setCurrentItem(currentItemIndex + 1);
                progressBar.setActiveIconIndex(currentItemIndex + 1);
                if (currentItemIndex == 1) {
                    hideKeyboard();
                }
            } else {
                // figure out
            }
        } else {
            // not allowed
        }
    }

    public void setSelectedCourse(Course course) {
        this.selectedCourse = course;
    }

    public void setSelectedAssignment(String selectedAssignment) {
        this.selectedAssignment = selectedAssignment;
    }

    public void setSelectedNumberOfStudents(int numberOfStudents) {
        this.selectedNumberOfStudents = numberOfStudents;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
            nextFragment();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard();
        overridePendingTransition(0, R.anim.fade_out);
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, 0);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
