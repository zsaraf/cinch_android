package com.seshtutoring.seshapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.DateUtils;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshDatePicker;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshDurationPicker;
import com.seshtutoring.seshapp.view.components.SeshEditText;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.List;

public class ViewSeshSetTimeActivity extends SeshActivity {
    private ImageButton closeSetTimeButton;

    public static final int SET_TIME_CREATE_SUCCESS = 1;
    public static final int SET_TIME_CREATE_FAILURE = 2;
    public static final int SET_TIME_CREATE_EXITED = 3;

    public static final String SET_TIME_SESH_ID_KEY = "SET_TIME_SESH_ID";

    public Sesh sesh;
    public SeshInformationLabel availableBlocksLabel;
    private SeshDatePicker seshDatePicker;
    private SeshEditText editText;
    private SeshActivityIndicator seshActivityIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sesh_set_time);

        // configure transparent status bar
        if (Build.VERSION.SDK_INT >= 19) {
            LayoutUtils utils = new LayoutUtils(this);
            RelativeLayout container = (RelativeLayout) findViewById(R.id.set_time_relative_layout_container);
            container.setPadding(0, utils.getStatusBarHeight(), 0, 0);
        }

        this.closeSetTimeButton = (ImageButton) findViewById(R.id.close_set_time);
        this.availableBlocksLabel = (SeshInformationLabel) findViewById(R.id.available_blocks_label);
        closeSetTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(SET_TIME_CREATE_EXITED);
                onBackPressed();
            }
        });

        seshActivityIndicator = (SeshActivityIndicator)findViewById(R.id.set_time_activity_indicator);
        seshActivityIndicator.setAlpha(0);

        Intent intent = getIntent();
        if (intent.hasExtra(SET_TIME_SESH_ID_KEY)) {
            int seshId = intent.getIntExtra(SET_TIME_SESH_ID_KEY, 0);
            List<Sesh> seshesFound = Sesh.find(Sesh.class, "sesh_id = ?", Integer.toString(seshId));
            sesh = seshesFound.get(0);
        }

        List<AvailableBlock> availableBlockList = AvailableBlock.find(AvailableBlock.class, "sesh = ?", Long.toString(sesh.getId()));
        this.availableBlocksLabel.setText(Html.fromHtml(AvailableBlock.getReadableBlocks(availableBlockList)));
        seshDatePicker = (SeshDatePicker) findViewById(R.id.date_picker);
        editText = (SeshEditText) findViewById(R.id.set_time_edit_text);
        seshDatePicker.setOnDateChangedListener(new SeshDatePicker.OnDateChangeListener() {
            @Override
            public void onDateChanged(DateTime dateTime) {
                editText.setText(DateUtils.getSeshFormattedDate(dateTime));
            }
        });
        seshDatePicker.setNextButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishedInputtingSetTime();
            }
        });

        editText.setText(DateUtils.getSeshFormattedDate(seshDatePicker.currentDateTime));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard();
        overridePendingTransition(0, R.anim.fade_out);
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void finishedInputtingSetTime() {
        final SeshNetworking seshNetworking = new SeshNetworking(this);
        final DateTime dateTime = seshDatePicker.currentDateTime;
        setNetworking(true);
        seshNetworking.setSetTime(sesh.seshId, dateTime, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                sesh.seshSetTime = dateTime.getMillis();
                onBackPressed();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                final SeshDialog seshDialog = new SeshDialog();
                seshDialog.setTitle("Network Error!");
                seshDialog.setMessage("We can't connect to the network! Try again!");
                seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
                seshDialog.setFirstChoice("OKAY");
                seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setNetworking(false);
                        seshDialog.dismiss();
                    }
                });
            }
        });
    }

    private void setNetworking(Boolean networking) {
        availableBlocksLabel.setEnabled(!networking);
        seshDatePicker.setEnabled(!networking);

        seshActivityIndicator
                .animate()
                .alpha(networking ? 1f : 0f)
                .setDuration(300)
                .setStartDelay(0)
                .start();
    }
}
