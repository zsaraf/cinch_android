package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by nadavhollander on 10/16/15.
 */
public class SchedulingContainer extends RelativeLayout {
    private boolean userCreatingBlock;
    private GestureDetectorCompat gestureDetector;
    private SchedulingTouchListener listener;
    private boolean interceptTouchEvents;

    public static abstract class SchedulingTouchListener {
        public abstract void onTouchEvent(MotionEvent e, boolean isUserCreatingBlock);
    }

    public SchedulingContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        userCreatingBlock = false;
        gestureDetector = new GestureDetectorCompat(context, new SchedulingGestureDetector());
    }

    public void setInterceptTouchEvents(boolean interceptTouchEvents) {
        this.interceptTouchEvents = interceptTouchEvents;
    }

    public void setSchedulingTouchListener(SchedulingTouchListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (interceptTouchEvents) {
            gestureDetector.onTouchEvent(e);
            listener.onTouchEvent(e, userCreatingBlock);
        }

        return userCreatingBlock;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getActionMasked() == MotionEvent.ACTION_UP && userCreatingBlock) {
            userCreatingBlock = false;
        }

        listener.onTouchEvent(e, userCreatingBlock);
        return interceptTouchEvents;
    }

    private class SchedulingGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            userCreatingBlock = true;
        }
    }
}
