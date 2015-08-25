package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;

/**
 * Created by nadavhollander on 8/24/15.
 */
public class RequestTimeoutButton extends RelativeLayout {
    private int numHours;
    private boolean selected;
    private View view;
    private Context mContext;
    private TextView numberLabel;
    private TextView hoursLabel;

    public RequestTimeoutButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        this.selected = false;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view =  mInflater.inflate(R.layout.request_timeout_button_layout, this, true);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RequestTimeoutButton,
                0, 0);

        this.numberLabel = (TextView) view.findViewById(R.id.num_hours_label);
        this.hoursLabel = (TextView) view.findViewById(R.id.hours_label);

        this.numHours = a.getInt(R.styleable.RequestTimeoutButton_numHours, -1);
        numberLabel.setText(Integer.toString(numHours));
        hoursLabel.setText((numHours > 1) ? "hours" : "hour");

        LayoutUtils utils = new LayoutUtils(mContext);
        numberLabel.setTypeface(utils.getLightGothamTypeface());
        hoursLabel.setTypeface(utils.getLightGothamTypeface());
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        RelativeLayout container = (RelativeLayout) view.findViewById(R.id.timeout_button_background);

        int layoutId = selected ? R.drawable.request_timeout_button_selected : R.drawable.request_timeout_button_unselected;
        if (Build.VERSION.SDK_INT < 16) {
            container.setBackgroundDrawable(mContext.getResources().getDrawable(layoutId));
        } else if (Build.VERSION.SDK_INT < 21) {
            container.setBackground(mContext.getResources().getDrawable(layoutId));
        } else  {
            container.setBackground(mContext.getResources().getDrawable(layoutId, null));
        }

        if (selected) {
            numberLabel.setTextColor(mContext.getResources().getColor(R.color.request_timeout_button_selected));
            hoursLabel.setTextColor(mContext.getResources().getColor(R.color.request_timeout_button_selected));
        } else {
            numberLabel.setTextColor(mContext.getResources().getColor(R.color.seshlightgray));
            hoursLabel.setTextColor(mContext.getResources().getColor(R.color.seshlightgray));
        }
    }

    public boolean isSelected() {
        return selected;
    }
}
