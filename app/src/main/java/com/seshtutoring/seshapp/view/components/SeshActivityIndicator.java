package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;

/**
 * Created by nadavhollander on 8/11/15.
 */
public class SeshActivityIndicator extends RelativeLayout {
    private ImageView indicator;
    public SeshActivityIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.sesh_activity_indicator, this, true);

        this.indicator = (ImageView) v.findViewById(R.id.activity_indicator);
    }

    @Override
    protected void onAttachedToWindow()  {
        super.onAttachedToWindow();
        Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_repeating);
        indicator.startAnimation(rotateAnimation);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        indicator.clearAnimation();
    }
}
