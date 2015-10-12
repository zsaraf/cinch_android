package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * Created by nadavhollander on 10/2/15.
 */
public class TwoDVScrollView extends ScrollView {

    public TwoDVScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TwoDVScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwoDVScrollView(Context context) {
        super(context);
    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        return false;
//    }
}