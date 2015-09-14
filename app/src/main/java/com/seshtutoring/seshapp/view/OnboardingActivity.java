package com.seshtutoring.seshapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.components.SeshViewPager;
import com.seshtutoring.seshapp.view.fragments.OnboardingFragments.OnboardingBioFragment;
import com.seshtutoring.seshapp.view.fragments.OnboardingFragments.OnboardingCardFragment;
import com.seshtutoring.seshapp.view.fragments.OnboardingFragments.OnboardingMajorFragment;
import com.seshtutoring.seshapp.view.fragments.OnboardingFragments.OnboardingPhotoFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nadavhollander on 9/12/15.
 */
public class OnboardingActivity extends SeshActivity {
    public static final String ONBOARDING_REQS_KEY = "onboarding_reqs";
    public static final String IS_STUDENT_ONBOARDING_KEY = "is_student_onboarding";
    public static final int ONBOARDING_REQUEST_CODE = 9;
    public static final int ONBOARDING_SUCCESSFUL_RESPONSE_CODE = 11;
    public static final int ONBOARDING_INCOMPLETE_RESPONSE_CODE = 12;

    public enum OnboardingRequirement {
        PROFILE_PICTURE(new OnboardingPhotoFragment()),
        MAJOR(new OnboardingMajorFragment()),
        CREDIT_CARD(new OnboardingCardFragment()),
        BIO(new OnboardingBioFragment());

        public Fragment fragment;
        OnboardingRequirement(Fragment fragment) {
            this.fragment = fragment;
        }
    }

    private SeshViewPager seshViewPager;
    private ArrayList<Fragment> fragments;
    private boolean isKeyboardShowing;
    private List<OnboardingRequirement> onboardingRequirements;
    private Set<OnboardingRequirement> requirementsFulfilled;
    private boolean isStudentOnboarding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_activity);

        Intent intent = getIntent();
        this.onboardingRequirements
                = (List<OnboardingRequirement>) intent.getSerializableExtra(ONBOARDING_REQS_KEY);
        this.isStudentOnboarding = intent.getBooleanExtra(IS_STUDENT_ONBOARDING_KEY, true);

        this.fragments = new ArrayList<>();
        this.requirementsFulfilled = new HashSet<>();
        for (OnboardingRequirement onboardingRequirement : onboardingRequirements) {
            Fragment fragment  = onboardingRequirement.fragment;
            if (onboardingRequirement == OnboardingRequirement.PROFILE_PICTURE) {
                Bundle args = new Bundle();
                args.putBoolean(IS_STUDENT_ONBOARDING_KEY, isStudentOnboarding);
                fragment.setArguments(args);
            }
            fragments.add(fragment);
        }

        this.seshViewPager = (SeshViewPager) findViewById(R.id.view_pager);
        seshViewPager.attachToActivity(this);
        seshViewPager.setViewPagerFragments(fragments);
        seshViewPager.setSwipingAllowed(false);

        final View rootView = (findViewById(android.R.id.content));
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                seshViewPager.startEntranceAnimation();

                if (Build.VERSION.SDK_INT < 16) {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    public void setRequirementFulfilled(OnboardingRequirement requirement) {
        requirementsFulfilled.add(requirement);

        boolean allReqsFulfilled = true;
        for (OnboardingRequirement req : onboardingRequirements) {
            if (!requirementsFulfilled.contains(req)) {
                allReqsFulfilled = false;
            }
        }

        if (allReqsFulfilled) {
            onAllRequirementsFulfilled();
        } else {
            seshViewPager.flingNextFragment();
        }
    }

    public void onAllRequirementsFulfilled() {
        setResult(ONBOARDING_SUCCESSFUL_RESPONSE_CODE);
        finish();
    }

    public void showKeyboard() {
        if (!isKeyboardShowing) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(0, 0);
            isKeyboardShowing = true;
        }
    }

    public void hideKeyboard() {
        if (isKeyboardShowing) {
            // Check if no view has focus:
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            isKeyboardShowing = false;
        }
    }

    public void cancelOnboarding() {
        setResult(ONBOARDING_INCOMPLETE_RESPONSE_CODE);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
