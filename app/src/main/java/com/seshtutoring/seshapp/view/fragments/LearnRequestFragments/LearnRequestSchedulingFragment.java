package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.seshtutoring.seshapp.view.components.SchedulingContainer;
import com.seshtutoring.seshapp.view.components.SeshViewPager;

import org.joda.time.DateTime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private RelativeLayout schedulingContainerWithLabel;
    private RelativeLayout schedulingBlocksContainer;
    private SchedulingContainer schedulingContainer;
    private LinearLayout nowScheduleButtons;
    private TextView whenSeshLabel;
    private boolean buttonsRaised;
    private RequestActivity parentActivity;
    private boolean allowsSwiping;
    private LinearLayout columnsContainer;
    private HorizontalScrollView hScrollView;
    private ScrollView vScrollView;
    private GestureDetectorCompat gestureDetectorCompat;

    private List<Block> blocks;

    private class Block {
        public int dayIndex;
        public double startHour;
        public double endHour;
        public boolean isShadow;
    }

    private class BlockComparator implements Comparator<Block> {
        @Override
        public int compare(Block b1, Block b2) {
            int dayDiff = b1.dayIndex - b2.dayIndex;
            if (dayDiff == 0) {
                return (int) (10 * (b1.startHour - b2.startHour));
            } else {
                return dayDiff;
            }
        }
    }
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = layoutInflater.inflate(R.layout.learn_request_scheduling_fragment, container, false);

        columnsContainer = (LinearLayout) v.findViewById(R.id.schedule_columns_container);
        nowButton = (TextView) v.findViewById(R.id.now_button);
        scheduleButton = (TextView) v.findViewById(R.id.schedule_button);
        schedulingContainerWithLabel = (RelativeLayout) v.findViewById(R.id.scheduling_container_with_label);
        schedulingBlocksContainer = (RelativeLayout) v.findViewById(R.id.scheduling_blocks_container);
        schedulingContainer = (SchedulingContainer) v.findViewById(R.id.scheduling_container);
        nowScheduleButtons = (LinearLayout) v.findViewById(R.id.now_schedule_buttons);
        whenSeshLabel = (TextView) v.findViewById(R.id.when_sesh_label);
        hScrollView = (HorizontalScrollView) v.findViewById(R.id.horizontal_scrollview);
        vScrollView = (ScrollView) v.findViewById(R.id.vertical_scrollview);

        blocks = new ArrayList<>();
        Block block1 = new Block();
        Block block2 = new Block();
        block1.dayIndex = 0;
        block1.startHour = 15;
        block1.endHour = 16;
        block2.dayIndex = 0;
        block2.startHour = 16;
        block2.endHour = 17;
        blocks.add(block1);
        blocks.add(block2);
        coalesceBlocks();

        updateBlocksDisplay();

        initDayLabels(v);

        schedulingContainer.setInterceptTouchEvents(false);
        schedulingContainer.setSchedulingTouchListener(new SchedulingContainer.SchedulingTouchListener() {
            @Override
            public void onTouchEvent(MotionEvent e, boolean isUserCreatingBlock) {
                onSchedulingTouchEvent(e, isUserCreatingBlock);
            }
        });

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

        gestureDetectorCompat = new GestureDetectorCompat(parentActivity, new SimpleTapUpGestureDetector());

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
            schedulingContainerWithLabel
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
        schedulingContainer.setInterceptTouchEvents(false);
        allowsSwiping = true;
    }

    private void onScheduleButtonSelected(LayoutUtils utils) {
        if (!parentActivity.getCurrentLearnRequest().isInstant()) return;

        setScheduleButtonSelected(utils);
        isCompleted = false;
        allowsSwiping = false;

        if (!buttonsRaised) {
            animateNowSeshButtonsUp();
            schedulingContainerWithLabel
                    .animate()
                    .alpha(1)
                    .setDuration(300)
                    .setStartDelay(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            schedulingContainerWithLabel.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
            buttonsRaised = true;
        } else {
            schedulingContainerWithLabel
                    .animate()
                    .x(0)
                    .setDuration(200)
                    .setStartDelay(0)
                    .start();
        }

        parentActivity.getCurrentLearnRequest().setIsInstant(false);
        schedulingContainer.setInterceptTouchEvents(true);
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

        // round scrollViewY to a value that doesn't cutoff any of the labels
        scrollViewY -= scrollViewY % getResources().getDimensionPixelSize(R.dimen.scheduling_grid_block_height);

        vScrollView.scrollTo(0, scrollViewY);
    }

    private void onSchedulingTouchEvent(MotionEvent e, boolean userCreatingBlock) {
        if (userCreatingBlock) {
            dragCreationHandler(e);
        } else {
            // tapCreationHandler gets routed through the gesture detector)
            gestureDetectorCompat.onTouchEvent(e);
        }
    }

    private void dragCreationHandler(MotionEvent e) {
        // do something
    }

    private void tapCreationHandler(MotionEvent e) {
        blocks.add(blockForTapCoordinates(e.getRawX(), e.getRawY()));
        coalesceBlocks();
        updateBlocksDisplay();
        debugBlocks();
    }

    private Block blockForTapCoordinates(float x, float y) {
        LayoutUtils utils = new LayoutUtils(parentActivity);

        Block block = new Block();
        float adjustedXVal = x + hScrollView.getScrollX();
        block.dayIndex = (int) adjustedXVal / getResources().getDimensionPixelSize(R.dimen.scheduling_column_width);

        int[] scrollViewXY = new int[2];
        vScrollView.getLocationInWindow(scrollViewXY);
        float adjustedYVal = y - scrollViewXY[1] + vScrollView.getScrollY() - utils.dpToPixels(7);
        int gridBlockHeight = getResources().getDimensionPixelSize(R.dimen.scheduling_grid_block_height);
        block.startHour = Math.floor(adjustedYVal / gridBlockHeight);

        if (adjustedYVal % gridBlockHeight > gridBlockHeight / 2) {
            block.startHour += 0.5;
        }

        block.endHour = block.startHour + 0.5;
        return block;
    }

    private void debugBlocks() {
        Log.d(TAG, "DEBUG BLOCKS");
        for (Block block : blocks) {
            Log.d(TAG, "----=====---- BLOCK");
            Log.d(TAG, "dayIndex: " + block.dayIndex);
            Log.d(TAG, "startHour: " + block.startHour);
            Log.d(TAG, "endHour: " + block.endHour);
        }
    }

    private void coalesceBlocks() {
        Collections.sort(blocks, new BlockComparator());

        Block prevBlock = null;

        for (int i = 0;i < blocks.size(); i++) {
            Block currBlock = blocks.get(i);
            if (prevBlock != null && prevBlock.endHour == currBlock.startHour
                    && prevBlock.dayIndex == currBlock.dayIndex
                    && prevBlock.isShadow == currBlock.isShadow) {
                prevBlock.endHour = currBlock.endHour;
                blocks.remove(i);
                i--;
            } else {
                prevBlock = currBlock;
            }
        }
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

    private void updateBlocksDisplay() {
        schedulingBlocksContainer.removeViewsInLayout(1, schedulingContainer.getChildCount() - 1);
        for (Block block : blocks) {
            RelativeLayout blockView = new RelativeLayout(getActivity());
            int width = getResources().getDimensionPixelSize(R.dimen.scheduling_block_width);
            int height = (int)
                    ((block.endHour - block.startHour) * getResources().getDimensionPixelSize(R.dimen.scheduling_grid_block_height));
            blockView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));

            if (Build.VERSION.SDK_INT < 21) {
                blockView.setBackground(getResources().getDrawable(R.drawable.available_block_bg));
            } else  {
                blockView.setBackground(getResources().getDrawable(R.drawable.available_block_bg, null));
            }

            if (block.isShadow) {
                blockView.setAlpha(0.8f);
            }

            LayoutUtils utils = new LayoutUtils(getActivity());

            schedulingBlocksContainer.addView(blockView);
            blockView.setX(block.dayIndex * getResources().getDimensionPixelSize(R.dimen.scheduling_column_width)
                    + getResources().getDimensionPixelSize(R.dimen.scheduling_time_labels_width));
            blockView.setY((float) block.startHour * getResources().getDimensionPixelSize(R.dimen.scheduling_grid_block_height)  + utils.dpToPixels(7));
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

    private class SimpleTapUpGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            tapCreationHandler(e);
            return true;
        }
    }
}
