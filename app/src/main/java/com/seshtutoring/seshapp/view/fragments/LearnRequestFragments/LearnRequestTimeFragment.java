package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Rate;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.CostUtils;
import com.seshtutoring.seshapp.view.RequestActivity;
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
    private TextView estimatedTotalLabel;
    private TextView estimatedTotalNumber;
    private SeshEditText durationTextBox;
    private SeshDurationPicker seshDurationPicker;
    private RequestActivity parentActivity;
    private float hourlyRate;
    private User currentUser;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_time_fragment, container, false);
        currentUser = User.currentUser(getActivity());

        parentActivity = (RequestActivity) getActivity();

        timeCostLabel = (TextView) v.findViewById(R.id.time_cost_label);
        timeCostNumber = (TextView) v.findViewById(R.id.time_cost_number);

        hourlyRate = Rate.getCurrentHourlyRate(getActivity()).getHourlyRate();
        String perMinuteString = CostUtils.floatToString(hourlyRate / 60, 2).substring(1);
        timeCostLabel.setText("Time X Cost ($" + perMinuteString + "/min)");

        creditsAppliedNumber = (TextView) v.findViewById(R.id.credits_applied_number);

        estimatedTotalLabel = (TextView) v.findViewById(R.id.estimated_total_label);
        estimatedTotalNumber = (TextView) v.findViewById(R.id.estimated_total_number);

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Bold.otf");
        estimatedTotalLabel.setTypeface(bold);
        estimatedTotalNumber.setTypeface(bold);

        durationTextBox = (SeshEditText) v.findViewById(R.id.duration_edit_text);
        seshDurationPicker = (SeshDurationPicker) v.findViewById(R.id.duration_picker);

        seshDurationPicker.setOnDurationChangedListener(this);
        seshDurationPicker.setNextButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.nextFragment();
            }
        });

        updateCostValues(seshDurationPicker.getHourValue(), seshDurationPicker.getMinuteValue());

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
        float estimatedTotalFloat = CostUtils.calculateEstimatedTotal(timeCostFloat, creditsAppliedFloat);
        creditsAppliedNumber.setText("-$" + CostUtils.floatToString(creditsAppliedFloat, 2));
        timeCostNumber.setText("$" + CostUtils.floatToString(timeCostFloat, 2));
        estimatedTotalNumber.setText("$" + CostUtils.floatToString(estimatedTotalFloat, 2));
    }

    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public void saveValues() {
        parentActivity.setSelectedDurationHours(seshDurationPicker.getHourValue());
        parentActivity.setSelectedDurationMinutes(seshDurationPicker.getMinuteValue());
    }
}
