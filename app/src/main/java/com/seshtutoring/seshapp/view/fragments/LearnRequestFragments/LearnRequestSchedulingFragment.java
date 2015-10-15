package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.DateUtils;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.components.SeshViewPager;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadavhollander on 10/2/15.
 */
public class LearnRequestSchedulingFragment extends SeshViewPager.InputFragment  {
    private static final String TAG = LearnRequestSchedulingFragment.class.getName();
    private boolean isCompleted;
    private SeshViewPager seshViewPager;
    private TextView nowButton;
    private TextView scheduleButton;
    private RelativeLayout schedulingContainer;
    private LinearLayout nowScheduleButtons;
    private TextView whenSeshLabel;
    private boolean buttonsRaised;
    private RequestActivity parentActivity;
    private boolean allowsSwiping;
    private LinearLayout columnsContainer;
    private HorizontalScrollView hScrollView;
    private ScrollView vScrollView;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = layoutInflater.inflate(R.layout.learn_request_scheduling_fragment, container, false);

        columnsContainer = (LinearLayout) v.findViewById(R.id.schedule_columns_container);
        nowButton = (TextView) v.findViewById(R.id.now_button);
        scheduleButton = (TextView) v.findViewById(R.id.schedule_button);
        schedulingContainer = (RelativeLayout) v.findViewById(R.id.scheduling_container);
        nowScheduleButtons = (LinearLayout) v.findViewById(R.id.now_schedule_buttons);
        whenSeshLabel = (TextView) v.findViewById(R.id.when_sesh_label);
        hScrollView = (HorizontalScrollView) v.findViewById(R.id.horizontal_scrollview);
        vScrollView = (ScrollView) v.findViewById(R.id.vertical_scrollview);

        initDayLabels(v);

        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initCurrentTimeLine();
                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        final LayoutUtils utils = new LayoutUtils(getActivity());

        nowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNowButtonSelected(utils);
            }
        });
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScheduleButtonSelected(utils);
            }
        });

        nowButton.setTypeface(utils.getBookGothamTypeface());
        scheduleButton.setTypeface(utils.getBookGothamTypeface());
        isCompleted = false;
        buttonsRaised = false;

        parentActivity = (RequestActivity) getActivity();
        return v;
    }

    private void onNowButtonSelected(LayoutUtils utils) {
        setNowButtonSelected(utils);

        if (!buttonsRaised) {
            animateNowSeshButtonsUp();
        }

        if (parentActivity.getCurrentLearnRequest().isInstant()) {
            isCompleted = true;
            seshViewPager.flingNextFragment();
        } else {
            schedulingContainer
                    .animate()
                    .x(utils.getScreenWidthPx())
                    .setDuration(200)
                    .setStartDelay(0)
                    .start();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isCompleted = true;
                    seshViewPager.flingNextFragment();
                }
            }, 300);
        }

        parentActivity.getCurrentLearnRequest().setIsInstant(true);
        allowsSwiping = true;
    }

    private void onScheduleButtonSelected(LayoutUtils utils) {
        if (!parentActivity.getCurrentLearnRequest().isInstant()) return;

        setScheduleButtonSelected(utils);
        isCompleted = false;
        allowsSwiping = false;

        if (!buttonsRaised) {
            animateNowSeshButtonsUp();
            schedulingContainer
                    .animate()
                    .alpha(1)
                    .setDuration(300)
                    .setStartDelay(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            schedulingContainer.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
            buttonsRaised = true;
        } else {
            schedulingContainer
                    .animate()
                    .x(0)
                    .setDuration(200)
                    .setStartDelay(0)
                    .start();
        }

        parentActivity.getCurrentLearnRequest().setIsInstant(false);
    }

    private void setScheduleButtonSelected(LayoutUtils utils) {
        scheduleButton.setTypeface(utils.getMediumGothamTypeface());
        scheduleButton.setTextColor(getResources().getColor(R.color.seshorange));
        nowButton.setTypeface(utils.getBookGothamTypeface());
        nowButton.setTextColor(getResources().getColor(R.color.white));
    }

    private void setNowButtonSelected(LayoutUtils utils) {
        nowButton.setTypeface(utils.getMediumGothamTypeface());
        nowButton.setTextColor(getResources().getColor(R.color.seshorange));
        scheduleButton.setTypeface(utils.getBookGothamTypeface());
        scheduleButton.setTextColor(getResources().getColor(R.color.white));
    }

    private void initCurrentTimeLine() {
        RelativeLayout todayColumn = (RelativeLayout) columnsContainer.getChildAt(0);
        LinearLayout currentTimeLine = (LinearLayout) todayColumn.findViewById(R.id.current_time_line);
        currentTimeLine.setVisibility(View.VISIBLE);

        int currentTimeLineY = yForTime(DateTime.now());
        currentTimeLine.setY(currentTimeLineY);

        int scrollViewY = currentTimeLineY - vScrollView.getMeasuredHeight() / 2;
        int maxScrollY = vScrollView.getChildAt(0).getHeight() - vScrollView.getMeasuredHeight();
        if (scrollViewY < 0) {
            scrollViewY = 0;
        } else if (scrollViewY > maxScrollY) {
            scrollViewY = maxScrollY;
        }

        vScrollView.scrollTo(0, scrollViewY);
    }

    private void animateNowSeshButtonsUp() {
        whenSeshLabel.animate().alpha(0).setDuration(150).start();
        nowScheduleButtons.animate().y(0).setDuration(300).start();
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

    private int yForTime(DateTime time) {
        int blockHeight = getResources().getDimensionPixelOffset(R.dimen.scheduling_grid_block_height);
        int hourY = time.getHourOfDay() * blockHeight;
        int minuteOfHourY = (int) ((time.getMinuteOfHour() / 60.0) * blockHeight);

        return hourY + minuteOfHourY;
    }

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
        return allowsSwiping;
    }

    @Override
    public void onFragmentInForeground() {
        parentActivity.hideKeyboard();
    }
}
