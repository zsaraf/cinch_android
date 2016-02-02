package com.seshtutoring.seshapp.view;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.ChatroomActivity;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.MessageAdapter;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.util.SoftKeyboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import android.os.Handler;

/**
 * Created by franzwarning on 8/31/15.
 */
public class MessagingActivity extends SeshActivity implements View.OnClickListener{

    private static final String TAG = MessagingActivity.class.getName();
    public static final String SESH_ID = "sesh_id";
    public static final String REFRESH_MESSAGES = "refresh_messages";

    private EditText textField;
    private RelativeLayout textFieldContainer;
    private TextView sendTextView;
    private ListView listView;
    private MessageAdapter messageAdapter;
    private Sesh sesh;
    private int seshId;
    private boolean wasKeyboardOpen = false;
    private SeshNetworking seshNetworking;
    private SoftKeyboard softKeyboard;
    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        LayoutUtils utils = new LayoutUtils(this);
        utils.setupCustomActionBar(this, true);

        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText("Messaging");
        title.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf"));

        RelativeLayout menuButton = (RelativeLayout) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        RelativeLayout backButton = (RelativeLayout) findViewById(R.id.action_bar_back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onBackPressed();
                }
                return true;
            }
        });

        // Assign the shite
        this.textField = (EditText)findViewById(R.id.text_field);
        this.textFieldContainer = (RelativeLayout)findViewById(R.id.text_field_container);
        this.sendTextView = (TextView)findViewById(R.id.send_text);
        this.listView = (ListView)findViewById(R.id.list_view);
        this.seshNetworking = new SeshNetworking(this);

        LayoutUtils layUtils = new LayoutUtils(this);

        // Set the font for the send button;
        this.sendTextView.setTypeface(layUtils.getBookGothamTypeface());
        this.textField.setTypeface(layUtils.getLightGothamTypeface());

        this.textField.clearFocus();

        final Paint paint = new Paint();
        paint.setTextSize(this.textField.getTextSize());
        paint.setTypeface(layUtils.getLightGothamTypeface());

        // Get the the sesh
        Bundle b = getIntent().getExtras();
        this.seshId = b.getInt(SESH_ID);

        // Setup the message adapter
        this.messageAdapter = new MessageAdapter(this);

        // Setup the list view
        this.listView.setOverScrollMode(ListView.OVER_SCROLL_ALWAYS);
        this.listView.setAdapter(this.messageAdapter);

        watchKeyboard();

        broadcastReceiver = actionBroadcastReceiver;
    }

    private void isCurrentOnMessages() {

        List<ChatroomActivity> messages =  this.sesh.chatroom.getChatroomActivities();

        if (messages.size() <= 0) return;

        this.sesh.chatroom.unreadActivityCount = 0;
        this.sesh.chatroom.save();

        ChatroomActivity lastMessage = messages.get(messages.size() - 1);
        // TODO: change to markMessagesAsRead
        this.seshNetworking.markMessagesReadWithChatroomId(lastMessage.chatroomActivityId, this.sesh.chatroom.chatroomId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d(TAG, "Updated read messages");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        this.sesh = Sesh.findSeshWithId(this.seshId);
        this.messageAdapter.messages = this.sesh.chatroom.getChatroomActivities();

        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.smoothScrollToPosition(messageAdapter.getCount() - 1);
            }
        }, 250);

        isCurrentOnMessages();

        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REFRESH_MESSAGES);
        this.registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onNewMessage();
        }
    };

    public void onNewMessage() {
        this.sesh = Sesh.findSeshWithId(this.seshId);
        isCurrentOnMessages();
        messageAdapter.messages = sesh.chatroom.getChatroomActivities();
        messageAdapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(messageAdapter.getCount() - 1);
    }

    private void watchKeyboard() {

        RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.messaging_root); // You must use your root layout
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        /*
        Instantiate and pass a callback
        */
        softKeyboard = new SoftKeyboard(mainLayout, im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged()
        {

            @Override
            public void onSoftKeyboardHide()
            {
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        textField.clearFocus();
                        listView.smoothScrollToPosition(messageAdapter.getCount() - 1);
                    }
                };
                mainHandler.post(myRunnable);
            }

            @Override
            public void onSoftKeyboardShow()
            {
                // Code here
                listView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listView.smoothScrollToPosition(messageAdapter.getCount() - 1);
                    }
                }, 250);
            }
        });
    }

    public void onClick(View v) {
        if (v == this.sendTextView) {
            String currentMessage = this.textField.getText().toString();
            if (currentMessage == null || currentMessage.equals("")) {
                return;
            }
            this.textField.setText("");
            this.textField.setHint("Sending...");
            this.textField.setCursorVisible(false);
            seshNetworking.sendMessage(currentMessage, this.sesh.chatroom.chatroomId, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    ChatroomActivity message = ChatroomActivity.createOrUpdateChatroomActivityWithJSON(jsonObject, sesh.chatroom, getApplicationContext());
                    message.save();
                    isCurrentOnMessages();

                    List<ChatroomActivity> messages = ChatroomActivity.find(ChatroomActivity.class, "chatroom = ?", Long.toString(sesh.chatroom.getId()));
                    Collections.sort(messages, new Comparator<ChatroomActivity>() {
                        @Override
                        public int compare(ChatroomActivity lhs, ChatroomActivity rhs) {
                            return (int) (lhs.timestamp- rhs.timestamp);
                        }
                    });

                    messageAdapter.messages = messages;
                    messageAdapter.notifyDataSetChanged();
                    listView.smoothScrollToPosition(messageAdapter.getCount() - 1);
                    textField.setHint("Message...");
                    textField.setCursorVisible(true);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    SeshDialog.showDialog(getFragmentManager(), "Whoops",
                            "Something went wrong.  Try again later.",
                            "Okay", null, "error");
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        softKeyboard.unRegisterSoftKeyboardCallback();
    }
}
