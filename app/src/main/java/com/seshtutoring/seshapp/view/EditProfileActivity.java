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

import com.seshtutoring.seshapp.R;
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

        ImageView doneButton = (ImageView) findViewById(R.id.done_icon);
        ImageView cancelButton = (ImageView) findViewById(R.id.cancel_icon);
        doneButton.setImageResource(R.drawable.check_green);
        cancelButton.setImageResource(R.drawable.x_red);
        majorText = (SeshEditText) findViewById(R.id.major_edit_text);
        bioText = (ActionMultilineEditText) findViewById(R.id.bio_edit_text);

        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(majorText, InputMethodManager.SHOW_FORCED);

        Intent data = getIntent();
        String bio = data.getStringExtra("curr_bio");
        String major = data.getStringExtra("curr_major");
        if (!bio.isEmpty()) {
            bioText.setHint(bio);
        }
        if (!major.isEmpty()) {
            majorText.setHint(major);
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
        Intent resultIntent = new Intent();
        if (bioText.getText().toString().isEmpty() && majorText.getText().toString().isEmpty()) {
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        }else {
            resultIntent.putExtra("bio", bioText.getText().toString());
            resultIntent.putExtra("major", majorText.getText().toString());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void cancelCalled() {
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
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
