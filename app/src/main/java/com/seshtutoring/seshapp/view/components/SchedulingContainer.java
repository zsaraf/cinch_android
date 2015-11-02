package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.text.method.DateTimeKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import org.joda.time.DateTime;

/**
 * Container is used to detect long taps -- and disable scrolling when a user is "holding"
 * SchedulingContainer exports a listener called SchedulingTouchListener, which has a touch listener
 * that can specify whether a user is trying to create a block by holding and dragging.
 *
 * Created by nadavhollander on 10/16/15.
 */
public class SchedulingContainer extends RelativeLayout {
    private boolean userCreatingBlock;
    private SchedulingTouchListener listener;
    private LongPressDetector longPressDetector;
    private boolean interceptTouchEvents;

    public static abstract class SchedulingTouchListener {
        public abstract void onTouchEvent(MotionEvent e, boolean isUserCreatingBlock);
    }

    public SchedulingContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        userCreatingBlock = false;
        longPressDetector = new LongPressDetector(500);
    }

    public void setInterceptTouchEvents(boolean interceptTouchEvents) {
        this.interceptTouchEvents = interceptTouchEvents;
    }

    public void setSchedulingTouchListener(SchedulingTouchListener listener) {
        this.listener = listener;
    }

    /**
     * Worth reading up on the difference and interaction between onInterceptTouchEvent and
     * onTouchEvent, but, basically, whenever the user is "holding down", all events are routed to
     * onTouchEvent (meaning they will not propogate down to the scrollview), while, if not,
     * all events are routed only through onInterceptTouchEvent and the scrollView receives events as well.
     *
     */

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent e) {
        if (interceptTouchEvents) {
            longPressDetector.onTouchEvent(e);
            listener.onTouchEvent(e, userCreatingBlock);
        }
        Log.d("TAG", "INTERCEPT TOUCH EVENT: " + e + " intercepting = " + (userCreatingBlock ? "true" : "false"));

        return userCreatingBlock;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!interceptTouchEvents) return false;

        Log.d("TAG", "REGULAR TOUCH EVENT: " + e + " intercepting = " + (userCreatingBlock ? "true" : "false"));
        longPressDetector.onTouchEvent(e);
        listener.onTouchEvent(e, userCreatingBlock);
        if (e.getActionMasked() == MotionEvent.ACTION_UP && userCreatingBlock) {
            userCreatingBlock = false;
            Log.d("TAG", "DRAG MODE OFF");
        }

        return interceptTouchEvents;
    }

    private class LongPressDetector {
        private MotionEvent initMotionEvent;
        private boolean listenForLongPress = false;
        private int longPressThreshold;
        private Handler handler;
        private Runnable longPressRunnable;

        public LongPressDetector(int longPressThreshold) {
            this.longPressThreshold = longPressThreshold;
            this.handler = new Handler();
            this.longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    if (listenForLongPress) {
                        onLongPress(initMotionEvent);
                    }
                }
            };
        }

        public void onTouchEvent(MotionEvent e) {
//            Log.d("TAG", "DRAG MODE TOUCH EVENT: " + e.toString());
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                if (userCreatingBlock) userCreatingBlock = false;

                if (listenForLongPress) {
                    listenForLongPress = false;
                    handler.removeCallbacks(longPressRunnable);
                } else {
                    listenForLongPress = true;
                    initMotionEvent = e;
                    handler.postDelayed(longPressRunnable, longPressThreshold);
                    Log.d("TAG", "==== DRAG RUNNABLE POSTED");
                }
            } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_MOVE) {
                listenForLongPress = false;
                handler.removeCallbacks(longPressRunnable);
                Log.d("TAG", "==== DRAG RUNNABLE CANCELED");
            }
        }

        public void onLongPress(MotionEvent e) {
            userCreatingBlock = true;
            Log.d("TAG", "DRAG MODE ON");
            listener.onTouchEvent(e, userCreatingBlock);
        }
    }
}
