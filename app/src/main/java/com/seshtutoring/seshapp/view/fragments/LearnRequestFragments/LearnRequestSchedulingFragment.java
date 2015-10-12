package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.DateUtils;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.components.SeshViewPager;
import com.seshtutoring.seshapp.view.components.TwoDHScrollView;
import com.seshtutoring.seshapp.view.components.TwoDVScrollView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadavhollander on 10/2/15.
 */
public class LearnRequestSchedulingFragment extends SeshViewPager.InputFragment  {
    private static final String TAG = LearnRequestSchedulingFragment.class.getName();

    private SeshViewPager seshViewPager;
    private VelocityTracker velocityTracker;
    private TwoDHScrollView hScroll;
    private TwoDVScrollView vScroll;

    private float mx, my;
    private float curX, curY;

    private boolean isCompleted;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_scheduling_fragment, container, false);

//        v.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return onTouchEvent(motionEvent);
//            }
//        });

        LinearLayout columnsContainer = (LinearLayout) v.findViewById(R.id.schedule_columns_container);
        for (int i = 0; i < 7; i++) {
            View column = layoutInflater.inflate(R.layout.scheduling_column_layout, null);
            columnsContainer.addView(column);
        }

        initDayLabels(v);

        hScroll = (TwoDHScrollView) v.findViewById(R.id.horizontal_scrollview);
        vScroll = (TwoDVScrollView) v.findViewById(R.id.vertical_scrollview );

        velocityTracker = VelocityTracker.obtain();

        isCompleted = false;
        return v;
    }

    private void initDayLabels(View v) {
        List<TextView> dayLabels = new ArrayList<>();
        dayLabels.add((TextView) v.findViewById(R.id.day_label_1));
        dayLabels.add((TextView) v.findViewById(R.id.day_label_2));
        dayLabels.add((TextView) v.findViewById(R.id.day_label_3));
        dayLabels.add((TextView) v.findViewById(R.id.day_label_4));
        dayLabels.add((TextView) v.findViewById(R.id.day_label_5));
        dayLabels.add((TextView) v.findViewById(R.id.day_label_6));
        dayLabels.add((TextView) v.findViewById(R.id.day_label_7));

        LayoutUtils layoutUtils = new LayoutUtils(getActivity());

        List<String> daysFormattedText = DateUtils.getSeshFormattedDaysOfTheWeek(new DateTime());
        for (int i= 0; i < 7; i++) {
            TextView label = dayLabels.get(i);
            label.setText(daysFormattedText.get(i));
            label.setTypeface(layoutUtils.getMediumGothamTypeface());
        }
    }

//    private boolean onTouchEvent(MotionEvent event) {
//        float curX, curY;
//        Log.d(TAG, "TOUCH EVENT: " + event);
//        double flingFactorMs = 0.5;
//
//        velocityTracker.addMovement(event);
//        switch (event.getAction()) {
//
//            case MotionEvent.ACTION_DOWN:
//                mx = event.getX();
//                my = event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                curX = event.getX();
//                curY = event.getY();
//                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
//                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
//                mx = curX;
//                my = curY;
//                break;
//            case MotionEvent.ACTION_UP:
//                velocityTracker.computeCurrentVelocity(1);
//                vScroll.smoothScrollBy((int) (velocityTracker.getXVelocity() * flingFactorMs),
//                        (int) (velocityTracker.getYVelocity() * flingFactorMs));
//                hScroll.smoothScrollBy((int) (velocityTracker.getXVelocity() * flingFactorMs),
//                        (int) (velocityTracker.getYVelocity() * flingFactorMs));
//                break;
//        }
//
//        return true;
//    }
//
//    private class RequestFlowGestureDetector extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            if (velocityX < 0) {
//                flingNextFragment();
//            } else {
//                flingPrevFragment();
//            }
//            return true;
//        }
//    }

    @Override
    public void attachSeshViewPager(SeshViewPager seshViewPager) {
        this.seshViewPager = seshViewPager;
    }

    @Override
    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public boolean allowsSwiping() {
        return false;
    }
}
