package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.components.MessageAdapter;

import java.util.logging.Handler;

/**
 * Created by franzwarning on 8/31/15.
 */
public class MessagingActivity extends SeshActivity {

    private static final String TAG = MessagingActivity.class.getName();

    private EditText textField;
    private RelativeLayout textFieldContainer;
    private TextView sendTextView;
    private ListView listView;
    private MessageAdapter messageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.sesh_action_bar);
        getSupportActionBar().setElevation(0);
        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText("MESSAGES");
        title.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf"));

        ImageButton menuButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        ImageButton backButton = (ImageButton) findViewById(R.id.action_bar_back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });

        // Assign the shite
        this.textField = (EditText)findViewById(R.id.text_field);
        this.textFieldContainer = (RelativeLayout)findViewById(R.id.text_field_container);
        this.sendTextView = (TextView)findViewById(R.id.send_text);
        this.listView = (ListView)findViewById(R.id.list_view);

        LayoutUtils layUtils = new LayoutUtils(this);

        // Set the font for the send button;
        this.sendTextView.setTypeface(layUtils.getBookGothamTypeface());
        this.textField.setTypeface(layUtils.getLightGothamTypeface());

        this.textField.clearFocus();
        this.textField.setOnFocusChangeListener(focusListener);
        final Paint paint = new Paint();
        paint.setTextSize(this.textField.getTextSize());
        paint.setTypeface(layUtils.getLightGothamTypeface());

        // Setup the message adapter
        this.messageAdapter = new MessageAdapter(this, new String[]{"data1",
                "data2", "test", "test222", "test333", "test222", "test333", "test222", "test333", "test222", "test333", "test222", "test333", "test222", "test333", "test222", "test333", "test222", "test333"});

        // Setup the list view
        this.listView.setOverScrollMode(ListView.OVER_SCROLL_ALWAYS);
        this.listView.setAdapter(this.messageAdapter);

    }

    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener(){
        public void onFocusChange(View v, boolean hasFocus){
            if (hasFocus){
                listView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listView.smoothScrollToPosition(messageAdapter.getCount() - 1);
                    }
                }, 250);
            } else {

            }
        }
    };
}
