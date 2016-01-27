package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class LearnRequestProgressBar extends UnderlineProgressBar {
    private ImageButton scheduleIconClickable;
    private ImageButton classIconClickable;
    private ImageButton assignmentIconClickable;
    private ImageButton numberStudentsIconClickable;
    private ImageButton durationIconClickable;
    private ImageButton confirmIconClickable;
    private ImageView scheduleIcon;
    private ImageView classIcon;
    private ImageView assignmentIcon;
    private ImageView numberStudentsIcon;
    private ImageView durationIcon;
    private ImageView confirmIcon;
    private View underline;

    private ImageButton[] clickableIcons;
    private ImageView[] icons;
    private Context mContext;

    private View view;

    private OnProgressIconClickedListener listener;

    public LearnRequestProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = mInflater.inflate(R.layout.learn_request_progress_layout, this, true);

        this.mContext = context;

        this.scheduleIconClickable = (ImageButton) view.findViewById(R.id.schedule_icon_clickable);
        this.classIconClickable = (ImageButton) view.findViewById(R.id.class_icon_clickable);
        this.assignmentIconClickable = (ImageButton) view.findViewById(R.id.assignment_icon_clickable);
        this.numberStudentsIconClickable = (ImageButton) view.findViewById(R.id.number_students_icon_clickable);
        this.durationIconClickable = (ImageButton) view.findViewById(R.id.duration_icon_clickable);
        this.confirmIconClickable = (ImageButton) view.findViewById(R.id.confirm_icon_clickable);

        this.scheduleIcon = (ImageView) view.findViewById(R.id.schedule_icon);
        this.classIcon = (ImageView) view.findViewById(R.id.class_icon);
        this.assignmentIcon = (ImageView) view.findViewById(R.id.assignment_icon);
        this.numberStudentsIcon = (ImageView) view.findViewById(R.id.number_students_icon);
        this.durationIcon = (ImageView) view.findViewById(R.id.duration_icon);
        this.confirmIcon = (ImageView) view.findViewById(R.id.confirm_icon);

        this.underline = view.findViewById(R.id.progress_underline);

        this.icons = new ImageView[] { scheduleIcon, classIcon, assignmentIcon,
                numberStudentsIcon, durationIcon, confirmIcon };
        this.clickableIcons = new ImageButton[] { scheduleIconClickable, classIconClickable, assignmentIconClickable,
                numberStudentsIconClickable, durationIconClickable, confirmIconClickable };

        for (int i = 0; i < clickableIcons.length; i++) {
            final int index = i;

            clickableIcons[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onClick(index);
                    }
                }
            });
        }
    }

    public void setIconClickListener(OnProgressIconClickedListener listener) {
        this.listener = listener;
    }

    public void setSelectedIndex(int index) {
        for (int i = clickableIcons.length - 1; i >= index; i--) {
            if (clickableIcons[i].getAlpha() > 0) {
                icons[i].animate().alpha(1).setDuration(300).start();
                clickableIcons[i].animate().alpha(0).setDuration(300).start();
            }
        }

        for (int i = index - 1; i >= 0; i--) {
            if (clickableIcons[i].getAlpha() == 0) {
                icons[i].animate().alpha(0).setDuration(300).start();
                clickableIcons[i].animate().alpha(1).setDuration(300).start();
            }
        }
    }

    public float getCenterXForIconIndex(int index) {
        ImageView icon = icons[index];
        float x = ((ViewGroup)icon.getParent()).getX();
        return x + icon.getWidth() / 2;
    }

    @Override
    public void setUnderlineX(float x) {
        underline.setX(x);
    }

    @Override
    public float getUnderlineX() {
        return underline.getX();
    }

    @Override
    public float getUnderlineCenterOffset() {
        return  (underline.getWidth() / 2);
    }
}
