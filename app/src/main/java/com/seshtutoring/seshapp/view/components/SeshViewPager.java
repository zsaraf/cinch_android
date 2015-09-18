package com.seshtutoring.seshapp.view.components;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.SeshActivity;
import com.seshtutoring.seshapp.view.components.UnderlineProgressBar.OnProgressIconClickedListener;
import com.seshtutoring.seshapp.view.RequestActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadavhollander on 9/2/15.
 */
public class SeshViewPager extends RelativeLayout {
    private static final String TAG = SeshViewPager.class.getName();
    private static final int CLAMP_OFFSET_DP = 100;
    private static final int EPSILON_DP = 1;
    private static final boolean DEFAULT_SWIPING_ALLOWED = true;

    private Context mContext;
    private SeshActivity activity;
    private SeshHorizontalScrollView scrollView;
    private UnderlineProgressBar progressBar;
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
    private List<Fragment> fragments;
    private boolean overshootInProgress;
    private boolean didReachEndValue;
    private int numFragments;
    private LinearLayout fragmentsContainer;
    private boolean swipingAllowed;
    private Handler mHandler;

    public static abstract class InputFragment extends Fragment {
        public abstract boolean isCompleted();
        public abstract void attachSeshViewPager(SeshViewPager seshViewPager);

        public void saveValues() {
            // optional implementation
        }

        public void onFragmentInForeground() {
            // optional implementation
        }

        public void beforeFragmentInForeground() {
            // optional implementation
        }

        public SeshEditText editText() {
            return null;
        }
    }

    public SeshViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        gestureDetector = new GestureDetectorCompat(context, new RequestFlowGestureDetector());
        utils = new LayoutUtils(context);
        fragmentWidth = utils.getScreenWidthPx();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.sesh_view_pager, this, true);

        fragmentsContainer = (LinearLayout) v.findViewById(R.id.fragments_container);

        scrollView = (SeshHorizontalScrollView) v.findViewById(R.id.scroll_view);
        scrollView.requestDisallowInterceptTouchEvent(true);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        springSystem = SpringSystem.create();
        flingSpring = springSystem.createSpring();
        flingSpring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));
        flingSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                scrollView.scrollToX((int) spring.getCurrentValue());

                /**
                 * In order to notify fragment when it is in foreground, we consider the flinging animation
                 * complete only once it has reached its end value a second time -- after it has "overshot"
                 * past its end value and then returned. Since spring returns float values for
                 * its current and end values, we determine equality using an Epsilon value.
                 */
                if (Math.abs(spring.getEndValue() - spring.getCurrentValue()) < utils.dpToPixels(EPSILON_DP)
                        && !didReachEndValue) {
                    if (overshootInProgress) {
                        InputFragment inputFragment = (InputFragment) fragments.get(currentFragmentIndex);
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
                if (progressBar != null) {
                    progressBar.setUnderlineX((int) spring.getCurrentValue()
                            - progressBar.getUnderlineCenterOffset());
                }
            }
        });

        clampSpring = springSystem.createSpring();
        clampSpring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));
        clampSpringUnderline = springSystem.createSpring();
        clampSpringUnderline.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));

        swipingAllowed = DEFAULT_SWIPING_ALLOWED;

        currentScrollViewIndex = 1;
        currentFragmentIndex = 0;

//        if (Build.VERSION.SDK_INT < 19) {
//            mHandler = new Handler();
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    startRepeatingTask();
//                }
//            }, 2000);
//        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            checkContentOffset(); //this function can change value of mInterval.
            mHandler.postDelayed(mStatusChecker, 1);
        }
    };

    public void checkContentOffset() {
        if (clampSpring.isAtRest()) {
            final int initialScrollX = currentScrollViewIndex * fragmentWidth;
            int currentValue = (int) clampSpring.getCurrentValue();
            final int clampOffsetPx = utils.dpToPixels(CLAMP_OFFSET_DP);
            int scrollXValue;
            if (currentValue < clampOffsetPx / 2) {
                scrollXValue = initialScrollX + currentValue;
            } else {
                scrollXValue = initialScrollX + (clampOffsetPx - currentValue);
            }
            if (scrollView.getScrollX() != scrollXValue) {
                scrollView.scrollToX(scrollXValue);
            }

        }


    }


    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public void attachToActivity(SeshActivity activity) {
        this.activity = activity;
    }

    public void setSwipingAllowed(boolean swipingAllowed) {
        this.swipingAllowed = swipingAllowed;
    }

    private void setFrameLayoutWidth(FrameLayout frameLayout, int width) {
        frameLayout.setLayoutParams(
                new LinearLayout.LayoutParams(width,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }

    public void setProgressBar(UnderlineProgressBar progressBar) {
        this.progressBar = progressBar;
        progressBar.setIconClickListener(new OnProgressIconClickedListener() {
            @Override
            public void onClick(int index) {
                flingToFragmentIndex(index);
            }
        });
    }

    public void setViewPagerFragments(List<Fragment> fragments) {
        this.fragments = fragments;
        this.numFragments = fragments.size();

        FrameLayout leftBuffer = new FrameLayout(mContext);
        setFrameLayoutWidth(leftBuffer, fragmentWidth);
        fragmentsContainer.addView(leftBuffer);

        for (Fragment fragment : fragments) {
            try {
                ((InputFragment) fragment).attachSeshViewPager(this);

                FrameLayout frameLayout = new FrameLayout(mContext);
                frameLayout.setId(LayoutUtils.generateViewId());
                fragmentsContainer.addView(frameLayout);
                setFrameLayoutWidth(frameLayout, fragmentWidth);

                activity.getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(), fragment, null).commit();
            } catch (ClassCastException e) {
                throw new ClassCastException("Fragment must implement InputFragment interface.");
            }
        }

        FrameLayout rightBuffer = new FrameLayout(mContext);
        setFrameLayoutWidth(rightBuffer, fragmentWidth);
        fragmentsContainer.addView(rightBuffer);

        // set scroll view at currentScrollViewIndex (accounting for buffers)
        scrollView.scrollToX(fragmentWidth * currentScrollViewIndex);
    }

    public void flingToFragmentIndex(int index) {

        if (index < 0 || index > numFragments - 1) {
            if (index > currentFragmentIndex) {
                clampNextFragment();
            } else {
                clampPrevFragment();
            }
            return;
        }

        if (index > currentFragmentIndex && index != 0) {
            InputFragment inputFragment = (InputFragment) fragments.get(currentFragmentIndex);
            if (!inputFragment.isCompleted()) {
                clampNextFragment();
                return;
            } else {
                inputFragment.saveValues();
            }
        }

        if (currentFragmentIndex >= 0 && currentFragmentIndex < numFragments) {
            Fragment fragment = fragments.get(currentFragmentIndex);
            SeshEditText editText = ((SeshViewPager.InputFragment) fragment).editText();
            if (editText != null) {
                editText.clearEditTextFocus();
            }
        }

        currentScrollViewIndex = index + 1;
        currentFragmentIndex = index;

        InputFragment targetFragment = (InputFragment) fragments.get(currentFragmentIndex);
        targetFragment.beforeFragmentInForeground();

        flingSpring.setCurrentValue(scrollView.getScrollX());
        flingSpring.setEndValue((currentScrollViewIndex) * fragmentWidth);

        if (progressBar != null) {
            flingSpringUnderline.setCurrentValue(progressBar.getUnderlineX());
            flingSpringUnderline.setEndValue(progressBar.getCenterXForIconIndex(currentFragmentIndex));

            progressBar.setSelectedIndex(currentFragmentIndex);
        }
    }

    public void startEntranceAnimation() {
        if (progressBar != null) {
            progressBar.setUnderlineX(-1 *
                    getResources().getDimensionPixelSize(R.dimen.request_flow_underline_width));
        }

        currentScrollViewIndex = 0;
        currentFragmentIndex = -1;
        scrollView.scrollToX(currentScrollViewIndex * fragmentWidth);

        boolean swipingAllowedUserChoice = swipingAllowed;
        swipingAllowed = true;
        flingToFragmentIndex(0);
        swipingAllowed = swipingAllowedUserChoice;
    }

    public void flingNextFragment() {
        flingToFragmentIndex(currentFragmentIndex + 1);
    }

    public void flingPrevFragment() {
        flingToFragmentIndex(currentFragmentIndex - 1);
    }

    public void flingSameFragment() {
        flingToFragmentIndex(currentFragmentIndex);
    }

    public void clampNextFragment() {
        final int initialScrollX = currentScrollViewIndex * fragmentWidth;
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
                scrollView.scrollToX(scrollXValue);
            }
        });
        clampSpring.setCurrentValue(0);
        clampSpring.setEndValue(clampOffsetPx);

        if (progressBar != null) {
            final int initialUnderlineX = (int) (progressBar.getCenterXForIconIndex(currentFragmentIndex)
                    - progressBar.getUnderlineCenterOffset());
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
                    progressBar.setUnderlineX(underlineXValue);
                }
            });
            clampSpringUnderline.setCurrentValue(0);
            clampSpringUnderline.setEndValue(clampOffsetUnderlinePx);
        }
    }

    public void clampPrevFragment() {
        final int initialScrollX = currentScrollViewIndex * fragmentWidth;
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
                scrollView.scrollToX(scrollXValue);
            }
        });
        clampSpring.setCurrentValue(0);
        clampSpring.setEndValue(clampOffsetPx);

        if (progressBar != null) {
            final int initialUnderlineX = (int) (progressBar.getCenterXForIconIndex(currentFragmentIndex)
                    - progressBar.getUnderlineCenterOffset());
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
                    progressBar.setUnderlineX(underlineXValue);
                }
            });
            clampSpringUnderline.setCurrentValue(0);
            clampSpringUnderline.setEndValue(clampOffsetUnderlinePx);
        }
    }

    private class RequestFlowGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (swipingAllowed) {
                if (velocityX < 0) {
                    flingNextFragment();
                } else {
                    flingPrevFragment();
                }
            }
            return true;
        }
    }
}
