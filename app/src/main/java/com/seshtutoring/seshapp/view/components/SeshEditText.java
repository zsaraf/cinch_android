package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
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
        FULLNAME(R.drawable.fullname_icon, R.drawable.fullname_icon_filled, false, false),
        CLASS(R.drawable.book, R.drawable.book, false, false),
        ASSIGNMENT(R.drawable.subject, R.drawable.subject_filled, false, false),
        CLOCK(R.drawable.clock_orange, R.drawable.clock_orange, false, false),
        NUMBER(R.drawable.hashtag_orange, R.drawable.hashtag_orange, false, false);


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

    public static final float SESH_EDIT_TEXT_HEIGHT_DP = 42.5f;

    private SeshEditTextType editTextType;
    private String hint;
    private EditText editText;
    private ImageView icon;
    private boolean filledIconActive = false;
    private int visibleHeight;


    public SeshEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.sesh_edit_text, this, true);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SeshEditText,
                0, 0);

        int editTextTypeIndex;
        int imeOptionsIndex;
        final boolean transparentMode;
        String text;
        boolean editable;

        try {
            editTextTypeIndex = a.getInt(R.styleable.SeshEditText_editTextType, 0);
            hint = a.getString(R.styleable.SeshEditText_hint);
            imeOptionsIndex = a.getInt(R.styleable.SeshEditText_imeOptions, -1);
            transparentMode = a.getBoolean(R.styleable.SeshEditText_transparentMode, false);
            text = a.getString(R.styleable.SeshEditText_text);
            editable = a.getBoolean(R.styleable.SeshEditText_editable, true);
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
            case 4:
                this.editTextType = SeshEditTextType.CLASS;
                break;
            case 5:
                this.editTextType = SeshEditTextType.ASSIGNMENT;
                break;
            case 6:
                this.editTextType = SeshEditTextType.CLOCK;
                break;
            case 7:
                this.editTextType = SeshEditTextType.NUMBER;
                break;
            default:
                this.editTextType = SeshEditTextType.EMAIL;
                break;
        }

        if (transparentMode) {
            RelativeLayout background = (RelativeLayout) findViewById(R.id.sesh_edit_text_background);
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                background.setBackgroundDrawable(null);
            } else {
                background.setBackground(null);
            }
        }

        editText = (EditText) findViewById(R.id.editText);
        icon = (ImageView) findViewById(R.id.icon);

        editText.setHint(hint);
        if (text != null) {
            editText.setText(text);
        }

        editText.setMovementMethod(null);
        icon.setImageResource(editTextType.iconResource);

        if (!editable) {
            editText.setKeyListener(null);
        }

        switch (imeOptionsIndex) {
            case 0:
                editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                break;
            case 1:
                editText.setImeOptions(EditorInfo.IME_ACTION_GO);
                break;
            case 2:
                editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            default:
                break;
        }

        if (editTextType.isPassword) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            // hacky fix for font changing whenever input type is set to 'password'
            Typeface light = Typeface.createFromAsset(context.getAssets(), "fonts/Gotham-Light.otf");
            editText.setTypeface(light);
        }

        if (editTextType.isEmail) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        if (editTextType == SeshEditTextType.FULLNAME) {
            Typeface light = Typeface.createFromAsset(context.getAssets(), "fonts/Gotham-Light.otf");
            editText.setTypeface(light);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    if (!filledIconActive && !transparentMode) {
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

    public void addTextChangedListener(TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }

    public void setOnEditorActionListener(EditText.OnEditorActionListener input) {
        editText.setOnEditorActionListener(input);
    }

    public String getText() {
        return editText.getText().toString();
    }

    public void setText(String text) {
        editText.setText(text);
    }

    public boolean requestEditTextFocus() {
        return editText.requestFocus();
    }
}
