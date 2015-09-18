package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.components.SeshViewPager;
import com.seshtutoring.seshapp.view.components.SeshEditText;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class LearnRequestAssignmentFragment extends SeshViewPager.InputFragment implements EditText.OnEditorActionListener {
    private SeshEditText assignmentInput;
    private RequestActivity parentActivity;
    private SeshViewPager seshViewPager;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_assignment_fragment, container, false);

        this.parentActivity = (RequestActivity) getActivity();

        this.assignmentInput = (SeshEditText) v.findViewById(R.id.assignment_edit_text);
        assignmentInput.setOnEditorActionListener(this);

        return v;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
            saveValues();
            seshViewPager.flingNextFragment();
            return true;
        }
        return false;
    }

    @Override
    public void saveValues() {
        parentActivity.getCurrentLearnRequest().descr = assignmentInput.getText();
    }

    @Override
    public boolean isCompleted() {
        return (assignmentInput.getText().length() > 0);
    }

    @Override
    public void attachSeshViewPager(SeshViewPager seshViewPager) {
        this.seshViewPager = seshViewPager;
    }

    @Override
    public void onFragmentInForeground() {
        assignmentInput.requestEditTextFocus();
        parentActivity.showKeyboard();
        parentActivity.showRequestFlowNextButton();
    }

    @Override
    public void beforeFragmentInForeground() {
        // do nothing
    }

    @Override
    public SeshEditText editText() {
        return assignmentInput;
    }
 }
