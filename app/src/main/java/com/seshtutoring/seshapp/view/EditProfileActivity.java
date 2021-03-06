package com.seshtutoring.seshapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.components.ActionMultilineEditText;
import com.seshtutoring.seshapp.view.components.SeshEditText;
import com.seshtutoring.seshapp.view.components.SeshViewPager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by lillioetting on 9/21/15.
 */
public class EditProfileActivity extends SeshActivity {

    public static final int RESULT_OK = 1;
    public static final int EDIT_PROFILE_REQUEST = 2;

    private SeshEditText majorText;
    private ActionMultilineEditText bioText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

        if (Build.VERSION.SDK_INT >= 19) {
            LayoutUtils utils = new LayoutUtils(this);
            RelativeLayout container = (RelativeLayout) findViewById(R.id.edit_container);
            container.setPadding(0, utils.getStatusBarHeight(), 0, 0);
        }

        RelativeLayout doneButton = (RelativeLayout) findViewById(R.id.done_icon);
        RelativeLayout cancelButton = (RelativeLayout) findViewById(R.id.cancel_icon);
        majorText = (SeshEditText) findViewById(R.id.major_edit_text);
        bioText = (ActionMultilineEditText) findViewById(R.id.bio_edit_text);

        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(majorText, InputMethodManager.SHOW_FORCED);

        Intent data = getIntent();
        String bio = data.getStringExtra("curr_bio");
        String major = data.getStringExtra("curr_major");
        if (!bio.isEmpty() && !bio.equals("edit profile to add bio")) {
            bioText.setText(bio);
        }
        if (!major.isEmpty() && !major.equals("edit profile to add major")) {
            majorText.setText(major);
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneCalled();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCalled();
            }
        });
    }

    private void doneCalled() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        Intent resultIntent = new Intent();
        if (bioText.getText().toString().isEmpty() && majorText.getText().toString().isEmpty()) {
            setResult(RESULT_CANCELED, resultIntent);
            finish();
            overridePendingTransition(R.anim.hold, R.anim.fade_out);
        }else {
            resultIntent.putExtra("bio", bioText.getText().toString());
            resultIntent.putExtra("major", majorText.getText().toString());
            setResult(RESULT_OK, resultIntent);
            finish();
            overridePendingTransition(R.anim.hold, R.anim.fade_out);
        }

    }

    private void cancelCalled() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
        overridePendingTransition(R.anim.hold, R.anim.fade_out);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
