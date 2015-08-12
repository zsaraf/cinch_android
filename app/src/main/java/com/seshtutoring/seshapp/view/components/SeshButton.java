package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.seshtutoring.seshapp.R;

/**
 * Created by nadavhollander on 7/15/15.
 */
public class SeshButton extends LinearLayout {
    private Button button;

    public SeshButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mInflater.inflate(R.layout.sesh_red_button_layout, this, true);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SeshButton,
                0, 0);

        int buttonType;
        int textSize;
        try {
            buttonType = a.getInt(R.styleable.SeshButton_buttonType, 0);
            textSize = a.getInteger(R.styleable.SeshButton_textSize, 14);
        } finally {
            a.recycle();
        }

        if (buttonType == 0) {
            mInflater.inflate(R.layout.sesh_red_button_layout, this, true);
        } else {
            mInflater.inflate(R.layout.sesh_gray_button_layout, this, true);
        }

        int[] attrSet = { android.R.attr.text };
        a = context.obtainStyledAttributes(attrs, attrSet);
        CharSequence text = a.getText(0);

        this.button = (Button) findViewById(R.id.sesh_button);

        if (text != null) {
            this.button.setText(text);
        }

        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

        Typeface medium = Typeface.createFromAsset(context.getAssets(), "fonts/Gotham-Medium.otf");
        button.setTypeface(medium);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        button.setOnClickListener(onClickListener);
    }

    public void setText(String text) {
        button.setText(text);
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        button.setTextColor(getResources().getColor(R.color.white));
    }

    public Button getButton() {
        return button;
    }
}
