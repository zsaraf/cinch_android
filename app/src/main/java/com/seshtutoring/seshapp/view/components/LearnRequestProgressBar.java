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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.seshtutoring.seshapp.R;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class LearnRequestProgressBar extends LinearLayout {
    private ImageButton classIcon;
    private ImageButton assignmentIcon;
    private ImageButton numberStudentsIcon;
    private ImageButton durationIcon;

    private ImageButton[] icons;
    private Context mContext;

    private View view;

    public LearnRequestProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = mInflater.inflate(R.layout.learn_request_progress_layout, this, true);

        this.mContext = context;

        this.classIcon = (ImageButton) view.findViewById(R.id.class_icon);
        this.assignmentIcon = (ImageButton) view.findViewById(R.id.assignment_icon);
        this.numberStudentsIcon = (ImageButton) view.findViewById(R.id.number_students_icon);
        this.durationIcon = (ImageButton) view.findViewById(R.id.duration_icon);

        this.icons = new ImageButton[] { classIcon, assignmentIcon,
                numberStudentsIcon, durationIcon };


        numberStudentsIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setActiveIconIndex(2);
            }
        });
    }

    public void setActiveIconIndex(int index) {
       for (int i = 0; i < index; i ++) {
            tintIcon(icons[i], R.color.seshorange);
        }

        tintIcon(icons[index], R.color.learn_request_progress_selected);

        for (int i = index + 1; i < 4; i++) {
            tintIcon(icons[i], R.color.learn_request_progress_unselected);
        }
    }

    @SuppressWarnings({"deprecation", "restriction"})
    private void tintIcon(ImageButton icon, int colorRes) {
        Drawable tintedDrawable = icon.getDrawable();
        int color = mContext.getResources().getColor(colorRes);
        tintedDrawable.setColorFilter(color,
                PorterDuff.Mode.SRC_IN);

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            classIcon.setImageDrawable(tintedDrawable);
        }
    }
}
