package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.components.SeshEditText;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class LearnRequestAssignmentFragment extends Fragment implements RequestActivity.InputFragment {
    private SeshEditText assignmentInput;
    private RequestActivity parentActivity;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_assignment_fragment, container, false);

        this.parentActivity = (RequestActivity) getActivity();

        this.assignmentInput = (SeshEditText) v.findViewById(R.id.assignment_edit_text);
        assignmentInput.setOnEditorActionListener(parentActivity);

        return v;
    }

    @Override
    public void saveValues() {
        parentActivity.setSelectedAssignment(assignmentInput.getText());
    }

    @Override
    public boolean isCompleted() {
        return (assignmentInput.getText().length() > 0);
    }
 }
