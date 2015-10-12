package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by nadavhollander on 10/2/15.
 */
public class TwoDHScrollView extends HorizontalScrollView {

    public TwoDHScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TwoDHScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwoDHScrollView(Context context) {
        super(context);
    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        return false;
//    }
}
