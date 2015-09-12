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
import com.seshtutoring.seshapp.view.components.SeshViewPager;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;

/**
 * Created by nadavhollander on 7/24/15.
 */
public class LearnRequestConfirmFragment extends Fragment implements SeshViewPager.InputFragment {
    private RequestActivity parentActivity;
    private SeshInformationLabel classLabel;
    private SeshInformationLabel assignmentLabel;
    private SeshInformationLabel numStudentsLabel;
    private SeshInformationLabel durationLabel;
    private SeshInformationLabel timeLabel;
    private SeshButton requestButton;
    private SeshActivityIndicator activityIndicator;
    private LinearLayout confirmationDetailsList;
    private SeshViewPager seshViewPager;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_confirm_fragment, container, false);

        this.parentActivity = (RequestActivity) getActivity();

        this.classLabel = (SeshInformationLabel) v.findViewById(R.id.class_label);
        this.assignmentLabel = (SeshInformationLabel) v.findViewById(R.id.assignment_label);
        this.numStudentsLabel = (SeshInformationLabel) v.findViewById(R.id.num_people_label);
        this.durationLabel = (SeshInformationLabel) v.findViewById(R.id.duration_label);
        this.timeLabel = (SeshInformationLabel) v.findViewById(R.id.time_label);

        this.requestButton = (SeshButton) v.findViewById(R.id.request_button);

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.createLearnRequest();
                requestButton.setEnabled(false);
            }
        });
        return v;
    }

    public void fillInConfirmationBoxes() {
        LearnRequest currentLearnRequest = parentActivity.getCurrentLearnRequest();

        classLabel.setText(currentLearnRequest.classString);
        assignmentLabel.setText(currentLearnRequest.descr);

        if (currentLearnRequest.numPeople > 1) {
            numStudentsLabel.setText(currentLearnRequest.numPeople + " people");
        } else {
            numStudentsLabel.setText(currentLearnRequest.numPeople + " person");
        }

        durationLabel.setText(currentLearnRequest.getEstTimeString());

        if (currentLearnRequest.isInstant()) {
            timeLabel.setText("NOW");
        } else  {
            // @TODO implement when scheduling is implemented
        }
    }

    public void enableRequestButton() {
        this.requestButton.setEnabled(true);
    }


    @Override
    public void saveValues() {
        // do nothing
    }

    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public void attachRequestFlowScrollView(SeshViewPager seshViewPager) {
        this.seshViewPager = seshViewPager;
    }

    @Override
    public void onFragmentInForeground() {
        parentActivity.hideRequestFlowNextButton();
        parentActivity.hideKeyboard();
    }

    @Override
    public void beforeFragmentInForeground() {
        fillInConfirmationBoxes();
    }
}
