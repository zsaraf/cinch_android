package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;

import org.w3c.dom.Text;

public class SeshInformationLabel extends RelativeLayout {

    private TextView textView;
    private ImageView iconView;
    private Context mContext;

    public SeshInformationLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.sesh_information_label, this, true);

        init(attrs, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SeshInformationLabel, defStyle, 0);

        Drawable icon;
        String text;
        try {
            text= a.getString(R.styleable.SeshInformationLabel_informationLabelText);
            icon = a.getDrawable(R.styleable.SeshInformationLabel_informationLabelIcon);
        } finally {
            a.recycle();
        }

        textView = (TextView) findViewById(R.id.text_view);
        iconView = (ImageView) findViewById(R.id.icon);

        textView.setText(text);
        iconView.setImageDrawable(icon);
        Typeface medium = Typeface.createFromAsset(mContext.getAssets(), "fonts/Gotham-Light.otf");
        textView.setTypeface(medium);
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public void setIcon(Drawable icon) {
        iconView.setImageDrawable(icon);
    }

}
