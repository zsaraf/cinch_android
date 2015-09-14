package com.seshtutoring.seshapp.view.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
    private View dottedLine;
    private Context mContext;

    public SeshInformationLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.sesh_information_label, this, true);

        init(attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SeshInformationLabel, defStyle, 0);

        Drawable icon;
        String text;
        int textColor;
        int dotsColor;
        Boolean dotsVisible;
        try {
            text= a.getString(R.styleable.SeshInformationLabel_informationLabelText);
            icon = a.getDrawable(R.styleable.SeshInformationLabel_informationLabelIcon);
            textColor = a.getColor(R.styleable.SeshInformationLabel_textColor, getResources().getColor(R.color.seshlightgray));
            dotsColor = a.getColor(R.styleable.SeshInformationLabel_dotsColor, getResources().getColor(R.color.sesh_information_label_dots_default));
            dotsVisible = a.getBoolean(R.styleable.SeshInformationLabel_dotsVisible, true);
        } finally {
            a.recycle();
        }

        textView = (TextView) findViewById(R.id.text_view);
        iconView = (ImageView) findViewById(R.id.icon);
        dottedLine = (View) findViewById(R.id.dotted_line);

        textView.setText(text);
        iconView.setImageDrawable(icon);
        Typeface medium = Typeface.createFromAsset(mContext.getAssets(), "fonts/Gotham-Light.otf");
        textView.setTypeface(medium);
        textView.setTextColor(textColor);

        Drawable drawable = null;
        if (Build.VERSION.SDK_INT < 21) {
           drawable = getResources().getDrawable(R.drawable.dotted);
        } else {
            drawable = getResources().getDrawable(R.drawable.dotted, null);
        }

        drawable.setColorFilter(dotsColor, PorterDuff.Mode.SRC_IN);
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            dottedLine.setBackgroundDrawable(drawable);
        } else {
            dottedLine.setBackground(drawable);
        }

        setDotsVisible(dotsVisible);
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public void setIcon(Drawable icon) {
        iconView.setImageDrawable(icon);
    }

    public void setDotsVisible(Boolean visible) {
        dottedLine.setVisibility(visible ? VISIBLE : INVISIBLE);
    }

}
