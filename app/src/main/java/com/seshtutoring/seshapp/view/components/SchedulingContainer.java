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
        longPressDetector = new LongPressDetector(500) {
            @Override
            public void onLongPress(MotionEvent e) {
                userCreatingBlock = true;
                Log.d("TAG", "DRAG MODE ON");
                listener.onTouchEvent(e, userCreatingBlock);
            }
        };
    }

    public void setInterceptTouchEvents(boolean interceptTouchEvents) {
        this.interceptTouchEvents = interceptTouchEvents;
    }

    public void setSchedulingTouchListener(SchedulingTouchListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent e) {
        if (interceptTouchEvents) {
            longPressDetector.onTouchEvent(e);
            listener.onTouchEvent(e, userCreatingBlock);
        }

        return userCreatingBlock;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        listener.onTouchEvent(e, userCreatingBlock);
        if (e.getActionMasked() == MotionEvent.ACTION_UP && userCreatingBlock) {
            userCreatingBlock = false;
            Log.d("TAG", "DRAG MODE OFF");
        }
        return interceptTouchEvents;
    }

    private abstract class LongPressDetector {
        private MotionEvent initMotionEvent;
        private boolean listenForLongPress = false;
        private int longPressThreshold;

        public LongPressDetector(int longPressThreshold) {
            this.longPressThreshold = longPressThreshold;
        }

        public void onTouchEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                listenForLongPress = true;
                initMotionEvent = e;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (listenForLongPress) {
                            onLongPress(initMotionEvent);
                        }
                    }
                }, longPressThreshold);
            } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_MOVE) {
                listenForLongPress = false;
            }
        }

        public abstract void onLongPress(MotionEvent e);
    }
}
