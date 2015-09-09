package com.seshtutoring.seshapp.view.components;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.components.LearnRequestProgressBar.OnProgressIconClickedListener;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.RequestActivity.InputFragment;

/**
 * Created by nadavhollander on 9/2/15.
 */
public class RequestFlowScrollView extends RelativeLayout {
    private static final String TAG = RequestFlowScrollView.class.getName();
    private static final int CLAMP_OFFSET_DP = 100;
    private static final int NUM_FRAGMENTS = 5;
    private static final int EPSILON_DP = 1;

    private Context mContext;
    private HorizontalScrollView scrollView;
    private View progressUnderline;
    private LearnRequestProgressBar progressBar;
    private GestureDetectorCompat gestureDetector;
    private LayoutUtils utils;
    private int fragmentWidth;
    private SpringSystem springSystem;
    private Spring flingSpring;
    private Spring flingSpringUnderline;
    private Spring clampSpring;
    private Spring clampSpringUnderline;
    private int currentScrollViewIndex;
    private int currentFragmentIndex;
    private Fragment[] fragments;
    private boolean overshootInProgress;
    private boolean didReachEndValue;

    public RequestFlowScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        gestureDetector = new GestureDetectorCompat(context, new RequestFlowGestureDetector());
        utils = new LayoutUtils(context);
        fragmentWidth = utils.getScreenWidthPx(context);

        springSystem = SpringSystem.create();
        flingSpring = springSystem.createSpring();
        flingSpring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));
        flingSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                scrollView.scrollTo((int) spring.getCurrentValue(), 0);

                /**
                 * In order to notify fragment when it is in foreground, we consider the flinging animation
                 * complete only once it has reached its end value a second time -- after it has "overshot"
                 * past its end value and then returned. Since spring returns float values for
                 * its current and end values, we determine equality using an Epsilon value.
                 */
                if (Math.abs(spring.getEndValue() - spring.getCurrentValue()) < utils.dpToPixels(EPSILON_DP)
                        && !didReachEndValue) {
                    if (overshootInProgress) {
                        InputFragment inputFragment = (InputFragment) fragments[currentFragmentIndex];
                        inputFragment.onFragmentInForeground();
                        didReachEndValue = true;
                    } else {
                        overshootInProgress = true;
                    }
                }
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
                overshootInProgress = false;
                didReachEndValue = false;
            }
        });

        flingSpringUnderline = springSystem.createSpring();
        flingSpringUnderline.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));
        flingSpringUnderline.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                progressUnderline.setX((int) spring.getCurrentValue() - getProgressUnderlineCenter());
            }
        });

        clampSpring = springSystem.createSpring();
        clampSpring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));
        clampSpringUnderline = springSystem.createSpring();
        clampSpringUnderline.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));

        currentScrollViewIndex = 0;
        currentFragmentIndex = -1;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        scrollView = (HorizontalScrollView) findViewById(R.id.scroll_view);
        scrollView.requestDisallowInterceptTouchEvent(true);
        scrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        progressBar = (LearnRequestProgressBar) findViewById(R.id.learn_request_progress_bar);
        progressBar.setIconClickListener(new OnProgressIconClickedListener() {
            @Override
            public void onClick(int index) {
                flingToIndex(index);
            }
        });

        progressUnderline = findViewById(R.id.progress_underline);
    }

    public void setRequestFlowFragments(Fragment[] fragments) {
        this.fragments = fragments;
        for (Fragment fragment : fragments) {
            ((RequestActivity.InputFragment) fragment).attachRequestFlowScrollView(this);
        }
    }

    public void flingToIndex(int index) {
        if (index < 0 || index > NUM_FRAGMENTS - 1) {
            if (index > currentFragmentIndex) {
                clampNextFragment();
            } else {
                clampPrevFragment();
            }
            return;
        }

        if (index > currentFragmentIndex && index != 0) {
            InputFragment inputFragment = (InputFragment) fragments[currentFragmentIndex];
            if (!inputFragment.isCompleted()) {
                clampNextFragment();
                return;
            } else {
                inputFragment.saveValues();
            }
        }

        currentScrollViewIndex = index + 1;
        currentFragmentIndex = index;

        InputFragment targetFragment = (InputFragment) fragments[currentFragmentIndex];
        targetFragment.beforeFragmentInForeground();

        flingSpring.setCurrentValue(scrollView.getScrollX());
        flingSpring.setEndValue((currentScrollViewIndex) * fragmentWidth);

        flingSpringUnderline.setCurrentValue(progressUnderline.getX());
        flingSpringUnderline.setEndValue(progressBar.getCenterXForIconIndex(currentFragmentIndex));

        progressBar.setSelectedIndex(currentFragmentIndex);
    }

    public void startEntranceAnimation() {
        progressUnderline.setX(-1 *
                getResources().getDimensionPixelSize(R.dimen.request_flow_underline_width));
        flingToIndex(0);
    }

    public void flingNextFragment() {
        flingToIndex(currentFragmentIndex + 1);
    }

    public void flingPrevFragment() {
        flingToIndex(currentFragmentIndex - 1);
    }

    public void clampNextFragment() {
        final int initialScrollX = currentScrollViewIndex * fragmentWidth;
        final int initialUnderlineX = progressBar.getCenterXForIconIndex(currentFragmentIndex) - getProgressUnderlineCenter();
        final int clampOffsetPx = utils.dpToPixels(CLAMP_OFFSET_DP);
        final int clampOffsetUnderlinePx = utils.dpToPixels(20);

        clampSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                int currentValue = (int) spring.getCurrentValue();
                int scrollXValue;
                if (currentValue < clampOffsetPx / 2) {
                    scrollXValue = initialScrollX + currentValue;
                } else {
                    scrollXValue = initialScrollX + (clampOffsetPx - currentValue);
                }
                scrollView.scrollTo(scrollXValue, 0);
            }
        });

        clampSpringUnderline.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                int currentValue = (int) spring.getCurrentValue();
                int underlineXValue;
                if (currentValue < clampOffsetUnderlinePx / 2) {
                    underlineXValue = initialUnderlineX + currentValue;
                } else {
                    underlineXValue = initialUnderlineX + (clampOffsetUnderlinePx - currentValue);
                }
                progressUnderline.setX(underlineXValue);
            }
        });

        clampSpring.setCurrentValue(0);
        clampSpring.setEndValue(clampOffsetPx);
        clampSpringUnderline.setCurrentValue(0);
        clampSpringUnderline.setEndValue(clampOffsetUnderlinePx);
    }

    public void clampPrevFragment() {
        final int initialScrollX = currentScrollViewIndex * fragmentWidth;
        final int initialUnderlineX = progressBar.getCenterXForIconIndex(currentFragmentIndex) - getProgressUnderlineCenter();
        final int clampOffsetPx = utils.dpToPixels(CLAMP_OFFSET_DP);
        final int clampOffsetUnderlinePx = utils.dpToPixels(20);

        clampSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                int currentValue = (int) spring.getCurrentValue();
                int scrollXValue;
                if (currentValue < clampOffsetPx / 2) {
                    scrollXValue = initialScrollX - currentValue;
                } else {
                    scrollXValue = initialScrollX - (clampOffsetPx - currentValue);
                }
                scrollView.scrollTo(scrollXValue, 0);
            }
        });

        clampSpringUnderline.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                int currentValue = (int) spring.getCurrentValue();
                int underlineXValue;
                if (currentValue < clampOffsetUnderlinePx / 2) {
                    underlineXValue = initialUnderlineX - currentValue;
                } else {
                    underlineXValue = initialUnderlineX - (clampOffsetUnderlinePx - currentValue);
                }
                progressUnderline.setX(underlineXValue);
            }
        });

        clampSpring.setCurrentValue(0);
        clampSpring.setEndValue(clampOffsetPx);
        clampSpringUnderline.setCurrentValue(0);
        clampSpringUnderline.setEndValue(clampOffsetUnderlinePx);
    }

    private int getProgressUnderlineCenter() {
        return getResources().getDimensionPixelSize(R.dimen.request_flow_underline_width) / 2;
    }

    private class RequestFlowGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityX < 0) {
                flingNextFragment();
            } else {
                flingPrevFragment();
            }
            return true;
        }
    }
}
