package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.SeshActivity;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshEditText;

/**
 * Created by nadavhollander on 7/24/15.
 */
public class LearnRequestConfirmFragment extends Fragment {
    private RequestActivity parentActivity;
    private SeshEditText classTextBox;
    private SeshEditText assignmentTextBox;
    private SeshEditText numStudentsTextBox;
    private SeshEditText durationTextBox;
    private SeshButton requestButton;
    private SeshActivityIndicator activityIndicator;
    private LinearLayout confirmationDetailsList;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_confirm_fragment, container, false);

        this.parentActivity = (RequestActivity) getActivity();

        this.classTextBox = (SeshEditText) v.findViewById(R.id.class_confirmation_box);
        this.assignmentTextBox = (SeshEditText) v.findViewById(R.id.assignment_confirmation_box);
        this.numStudentsTextBox = (SeshEditText) v.findViewById(R.id.num_students_confirmation_box);
        this.durationTextBox = (SeshEditText) v.findViewById(R.id.duration_confirmation_box);
        this.activityIndicator = (SeshActivityIndicator) v.findViewById(R.id.request_activity_indicator);
        this.confirmationDetailsList = (LinearLayout) v.findViewById(R.id.confirmation_detail_list);

        this.requestButton = (SeshButton) v.findViewById(R.id.request_button);

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActivityIndicator();
                parentActivity.createLearnRequest();
                requestButton.setEnabled(false);
            }
        });
        return v;
    }

    public void showActivityIndicator() {
        activityIndicator.animate().alpha(1).setDuration(300).start();
        confirmationDetailsList.animate().alpha(0).setDuration(300).start();
    }

    public void hideActivityIndicator() {
        activityIndicator.animate().alpha(0).setDuration(300).start();
        confirmationDetailsList.animate().alpha(1).setDuration(300).start();
    }

    public void fillInConfirmationBoxes() {
        LearnRequest currentLearnRequest = parentActivity.getCurrentLearnRequest();

        classTextBox.setText(currentLearnRequest.classString);
        assignmentTextBox.setText(currentLearnRequest.descr);

        if (currentLearnRequest.numPeople > 1) {
            numStudentsTextBox.setText(currentLearnRequest.numPeople + " Students");
        } else {
            numStudentsTextBox.setText(currentLearnRequest.numPeople + " Student");
        }

        durationTextBox.setText(currentLearnRequest.getEstTimeString());
    }

    public void enableRequestButton() {
        this.requestButton.setEnabled(true);
    }
}
