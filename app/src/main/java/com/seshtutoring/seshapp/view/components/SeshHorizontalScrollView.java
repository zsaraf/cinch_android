package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by nadavhollander on 9/11/15.
 */
public class SeshHorizontalScrollView extends HorizontalScrollView {
    public SeshHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void scrollToX(int x) {
        super.scrollTo(x, 0);
    }

    // prevents Horizontal Scroll View from scrolling while user edits SeshEditText
    @Override
    public void scrollTo(int x, int y) {
        // do nothing
    }
}