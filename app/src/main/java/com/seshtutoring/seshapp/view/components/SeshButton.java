package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
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
        mInflater.inflate(R.layout.sesh_button_layout, this, true);

        int[] attrSet = { android.R.attr.text };
        TypedArray a = context.obtainStyledAttributes(attrs, attrSet);
        CharSequence text = a.getText(0);

        this.button = (Button) findViewById(R.id.sesh_button);

        if (text != null) {
            this.button.setText(text);
        }

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
    }

    public Button getButton() {
        return button;
    }
}
