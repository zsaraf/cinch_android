package com.seshtutoring.seshapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.view.components.SeshDatePicker;
import com.seshtutoring.seshapp.view.components.SeshDurationPicker;
import com.seshtutoring.seshapp.view.components.SeshEditText;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;

import org.joda.time.DateTime;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sesh_set_time);

        this.closeSetTimeButton = (ImageButton) findViewById(R.id.close_set_time);
        this.availableBlocksLabel = (SeshInformationLabel) findViewById(R.id.available_blocks_label);
        closeSetTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(SET_TIME_CREATE_EXITED);
                onBackPressed();
            }
        });

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
//        seshDatePicker.setOnDateChangedListener(new SeshDatePicker.OnDateChangeListener() {
//            @Override
//            public void onDateChanged(DateTime dateTime) {
//
//            }
//        });
//        seshDatePicker.setNextButtonOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
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
}
