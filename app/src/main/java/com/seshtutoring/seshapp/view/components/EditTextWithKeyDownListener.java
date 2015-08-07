package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by nadavhollander on 8/7/15.
 */
    public class EditTextWithKeyDownListener extends AppCompatEditText {
        private Runnable keyDownListener;

        public EditTextWithKeyDownListener(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void setKeyDownListener(Runnable runnable) {
            this.keyDownListener = runnable;
        }

        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (keyDownListener != null) {
                    keyDownListener.run();
                }
                return true;  // So it is not propagated.
            }
            return super.dispatchKeyEvent(event);
        }
    }

