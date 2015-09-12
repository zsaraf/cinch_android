package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by nadavhollander on 9/11/15.
 */
public abstract class UnderlineProgressBar extends RelativeLayout {

    public static abstract class OnProgressIconClickedListener {
        public abstract void onClick(int index);
    }

    public UnderlineProgressBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public abstract void setUnderlineX(float x);
    public abstract float getUnderlineX();
    public abstract float getUnderlineCenterOffset();
    public abstract void setIconClickListener(OnProgressIconClickedListener listener);
    public abstract float getCenterXForIconIndex(int index);
    public abstract void setSelectedIndex(int index);
}
