package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.model.Message;
import com.seshtutoring.seshapp.util.LayoutUtils;

/**
 * Created by franzwarning on 9/3/15.
 */
public class MessageRow {

    public TextView leftText;
    public TextView rightText;
    private Message message;

    public void setCurrentMessage(Message message, Typeface tf, int position, int totalItems, Context context) {
        this.message = message;

        leftText.setTypeface(tf);
        rightText.setTypeface(tf);

        TextView activeTextView = message.fromYou ? rightText : leftText;
        TextView inactiveTextView = message.fromYou ? leftText : rightText;

        int leftMargin = 10;
        int rightMargin = 10;
        int topMargin = 5;
        int bottomMargin = 5;
        int topSpecialMargin = 10;
        int bottomSpecialMargin = 10;

        if (activeTextView == rightText) {

            if (rightText.getLineCount() > 1) {
                rightText.setGravity(Gravity.LEFT);
            } else {
                rightText.setGravity(Gravity.RIGHT);
            }
            leftMargin = 100;
        } else {
            rightMargin = 100;
        }

        activeTextView.setText(message.content.trim());
        activeTextView.setVisibility(View.VISIBLE);

        inactiveTextView.setText("");
        inactiveTextView.setVisibility(View.INVISIBLE);



        LayoutUtils lu = new LayoutUtils(context);

        if (position == 0) {
            ViewGroup.MarginLayoutParams nlp = (ViewGroup.MarginLayoutParams)activeTextView.getLayoutParams();
            nlp.setMargins(lu.dpToPixels(leftMargin), lu.dpToPixels(topSpecialMargin), lu.dpToPixels(rightMargin), lu.dpToPixels(bottomMargin));
            activeTextView.setLayoutParams(nlp);
        } else if (position == totalItems - 1) {
            ViewGroup.MarginLayoutParams nlp = (ViewGroup.MarginLayoutParams)activeTextView.getLayoutParams();
            nlp.setMargins(lu.dpToPixels(leftMargin), lu.dpToPixels(topMargin), lu.dpToPixels(rightMargin), lu.dpToPixels(bottomSpecialMargin));
            activeTextView.setLayoutParams(nlp);
        } else {
            ViewGroup.MarginLayoutParams nlp = (ViewGroup.MarginLayoutParams)activeTextView.getLayoutParams();
            nlp.setMargins(lu.dpToPixels(leftMargin), lu.dpToPixels(topMargin), lu.dpToPixels(rightMargin), lu.dpToPixels(bottomMargin));
            activeTextView.setLayoutParams(nlp);
        }
    }

    public void updateMessageRow() {

    }

}
