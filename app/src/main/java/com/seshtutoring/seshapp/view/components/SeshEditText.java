package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;

/**
 * Created by nadavhollander on 7/15/15.
 */
public class SeshEditText extends RelativeLayout {
    private enum SeshEditTextType {
        EMAIL(R.drawable.email_icon, R.drawable.email_icon_filled, true, false),
        PASSWORD(R.drawable.password_icon, R.drawable.password_icon_filled, false, true),
        RE_ENTER_PASSWORD(R.drawable.re_enter_password_icon,
                R.drawable.re_enter_password_icon_filled, false, true),
        FULLNAME(R.drawable.fullname_icon, R.drawable.fullname_icon_filled, false, false);


        public int iconResource;
        public int filledIconResource;
        public boolean isEmail;
        public boolean isPassword;


        private SeshEditTextType(int iconResource, int filledIconResource,
                     boolean isEmail, boolean isPassword) {
            this.iconResource = iconResource;
            this.filledIconResource = filledIconResource;
            this.isEmail = isEmail;
            this.isPassword = isPassword;
        }
    }

    private SeshEditTextType editTextType;
    private String hint;
    private EditText editText;
    private ImageView icon;
    private boolean filledIconActive = false;

    public SeshEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.sesh_edit_text, this, true);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SeshEditText,
                0, 0);

        int editTextTypeIndex;

        try {
            editTextTypeIndex = a.getInt(R.styleable.SeshEditText_editTextType, 0);
            hint = a.getString(R.styleable.SeshEditText_hint);
        } finally {
            a.recycle();
        }

        switch (editTextTypeIndex) {
            case 0:
                this.editTextType = SeshEditTextType.EMAIL;
                break;
            case 1:
                this.editTextType = SeshEditTextType.PASSWORD;
                break;
            case 2:
                this.editTextType = SeshEditTextType.RE_ENTER_PASSWORD;
                break;
            case 3:
                this.editTextType = SeshEditTextType.FULLNAME;
                break;
            default:
                this.editTextType = SeshEditTextType.EMAIL;
                break;
        }

        editText = (EditText) findViewById(R.id.editText);
        icon = (ImageView) findViewById(R.id.icon);

        editText.setHint(hint);
        icon.setImageResource(editTextType.iconResource);

        if (editTextType.isPassword) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            // hacky fix for font changing whenever input type is set to 'password'
            Typeface light = Typeface.createFromAsset(context.getAssets(), "fonts/Gotham-Light.otf");
            editText.setTypeface(light);
        }

        if (editTextType.isEmail) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    if(!filledIconActive) {
                        icon.setImageResource(editTextType.filledIconResource);
                        filledIconActive = true;
                    }
                } else {
                    icon.setImageResource(editTextType.iconResource);
                    filledIconActive = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        });
    }

    public String getText() {
        return editText.getText().toString();
    }
}
