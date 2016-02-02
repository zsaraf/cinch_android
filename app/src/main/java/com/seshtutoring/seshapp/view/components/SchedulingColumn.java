package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;

/**
 * Created by nadavhollander on 10/14/15.
 */
public class SchedulingColumn extends RelativeLayout {
    public SchedulingColumn(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.scheduling_column_layout, this, true);
    }
}
