package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;

/**
 * Created by nadavhollander on 7/23/15.
 */
public class SeshDurationPicker extends RelativeLayout {
    private NumberPicker hoursPicker;
    private NumberPicker minutesPicker;

    public SeshDurationPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.sesh_duration_picker, this, true);

//
//        Typeface medium = Typeface.createFromAsset(context.getAssets(), "fonts/Gotham-Medium.otf");
//        button.setTypeface(medium);
    }
}
