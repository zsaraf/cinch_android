package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.media.Image;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;

/**
 * Created by lillioetting on 7/29/15.
 */
public class SeshIconTextView extends RelativeLayout {

    private View view;
    private ImageView iconView;
    private TextView textView;

    public SeshIconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = mInflater.inflate(R.layout.sesh_icon_text_view, this, true);

        iconView = (ImageView) view.findViewById(R.id.icon_text_view_icon);
        textView = (TextView) view.findViewById(R.id.icon_text_view_text);
        //default to book
        iconView.setImageResource(R.drawable.calendar_unfilled);
    }

    public void setIconResourceId(int id) {
        iconView.setImageResource(id);
    }

    public String getText() {
        return textView.getText().toString();
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setText(Spanned html) {
        textView.setText(html);
    }

}
