package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Constants;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.CostUtils;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.components.RequestFlowScrollView;
import com.seshtutoring.seshapp.view.components.SeshDurationPicker;
import com.seshtutoring.seshapp.view.components.SeshEditText;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class LearnRequestTimeFragment extends Fragment implements RequestActivity.InputFragment,
        SeshDurationPicker.OnDurationChangeListener {
    private TextView timeCostLabel;
    private TextView timeCostNumber;
    private TextView creditsAppliedNumber;
    private RelativeLayout additionalStudentsRow;
    private TextView additionalStudentsNumber;
    private TextView estimatedTotalLabel;
    private TextView estimatedTotalNumber;
    private SeshEditText durationTextBox;
    private SeshDurationPicker seshDurationPicker;
    private RequestActivity parentActivity;
    private float hourlyRate;
    private User currentUser;
    private RequestFlowScrollView requestFlowScrollView;
    private boolean isCompleted;
    private LearnRequest currentLearnRequest;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_time_fragment, container, false);
        currentUser = User.currentUser(getActivity());

        parentActivity = (RequestActivity) getActivity();

        timeCostLabel = (TextView) v.findViewById(R.id.time_cost_label);
        timeCostNumber = (TextView) v.findViewById(R.id.time_cost_number);

        hourlyRate = Constants.getHourlyRate(getActivity());
        String perMinuteString = CostUtils.floatToString(hourlyRate / 60, 2).substring(1);
        timeCostLabel.setText("Estimated cost ($" + perMinuteString + "/min)");

        creditsAppliedNumber = (TextView) v.findViewById(R.id.credits_applied_number);

        additionalStudentsRow = (RelativeLayout) v.findViewById(R.id.additional_students_row);
        additionalStudentsNumber = (TextView) v.findViewById(R.id.additional_students_number);

        estimatedTotalLabel = (TextView) v.findViewById(R.id.estimated_total_label);
        estimatedTotalNumber = (TextView) v.findViewById(R.id.estimated_total_number);

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Bold.otf");
        estimatedTotalLabel.setTypeface(bold);

        durationTextBox = (SeshEditText) v.findViewById(R.id.duration_edit_text);
        seshDurationPicker = (SeshDurationPicker) v.findViewById(R.id.duration_picker);

        isCompleted = false;

        seshDurationPicker.setOnDurationChangedListener(this);
        seshDurationPicker.setNextButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCompleted = true;
                requestFlowScrollView.flingNextFragment();
            }
        });

        return v;
    }

    @Override
    public void onDurationChanged(int hourValue, int minuteValue) {
        String seshDuration = "";

        if (hourValue > 0) {
            if (hourValue == 1) {
                seshDuration += hourValue + " hr ";
            } else {
                seshDuration += hourValue + " hrs ";
            }
        }
        if (minuteValue > 0) {
            seshDuration += minuteValue + " min";
        }

        durationTextBox.setText(seshDuration);
        updateCostValues(hourValue, minuteValue);
    }

    private void updateCostValues(int hourValue, int minuteValue) {
        float timeCostFloat = CostUtils.calculateCostForDuration(hourValue, minuteValue, hourlyRate);
        float creditsAppliedFloat = Math.min(timeCostFloat, currentUser.getCreditSum());
        float additionalStudentsFloat
                = CostUtils.calculateAdditionalStudentCharge(hourValue, minuteValue, currentLearnRequest.numPeople);
        float estimatedTotalFloat = CostUtils.calculateEstimatedTotal(timeCostFloat, additionalStudentsFloat, creditsAppliedFloat);
        creditsAppliedNumber.setText("-$" + CostUtils.floatToString(creditsAppliedFloat, 2));
        timeCostNumber.setText("$" + CostUtils.floatToString(timeCostFloat, 2));
        additionalStudentsNumber.setText("$" + CostUtils.floatToString(additionalStudentsFloat, 2));
        estimatedTotalNumber.setText("$" + CostUtils.floatToString(estimatedTotalFloat, 2));
    }

    @Override
    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public void saveValues() {
        parentActivity.getCurrentLearnRequest().estTime =
                (seshDurationPicker.getHourValue() * 60) + seshDurationPicker.getMinuteValue();
        parentActivity.getCurrentLearnRequest().setEstTimeString(durationTextBox.getText());
    }

    @Override
    public void attachRequestFlowScrollView(RequestFlowScrollView requestFlowScrollView) {
        this.requestFlowScrollView = requestFlowScrollView;
    }

    @Override
    public void onFragmentInForeground() {
        parentActivity.showRequestFlowNextButton();
    }

    @Override
    public void beforeFragmentInForeground() {
        currentLearnRequest = parentActivity.getCurrentLearnRequest();
        if (currentLearnRequest.numPeople > 1) {
            additionalStudentsRow.setVisibility(View.VISIBLE);
        } else {
            additionalStudentsRow.setVisibility(View.GONE);
        }

        onDurationChanged(seshDurationPicker.getHourValue(), seshDurationPicker.getMinuteValue());
    }
}
