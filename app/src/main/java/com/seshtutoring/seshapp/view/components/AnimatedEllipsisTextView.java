package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;

/**
 * Created by nadavhollander on 7/28/15.
 */
public class AnimatedEllipsisTextView extends TextView {
    private Handler handler;
    private Runnable characterAnimator;
    private boolean animating;

    public AnimatedEllipsisTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
//
//        TypedArray a = context.getTheme().obtainStyledAttributes(
//                attrs,
//                R.styleable.SeshEditText,
//                0, 0);

        final String textWithEllipsis = getText() + "...";

        this.handler = new Handler();
        this.characterAnimator = new Runnable() {
            @Override
            public void run() {
                if (!animating) return;

                String modifiedText;
                int currTextLength = getText().length();
                if (currTextLength == textWithEllipsis.length()) {
                    modifiedText = textWithEllipsis.substring(0, textWithEllipsis.length() - 3);
                } else {
                    modifiedText = textWithEllipsis.substring(0, currTextLength + 1);
                }
                setText(modifiedText);

                handler.postDelayed(characterAnimator, 500);
            }
        };
    }

    @Override
    protected void onAttachedToWindow()  {
        super.onAttachedToWindow();
        animating = true;
        handler.postDelayed(characterAnimator, 500);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animating = false;
    }
}
