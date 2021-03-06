package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Constants;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.CostUtils;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.components.SeshViewPager;
import com.seshtutoring.seshapp.view.components.SeshDurationPicker;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class LearnRequestTimeFragment extends SeshViewPager.InputFragment {
    private TextView timeCostLabel;
    private TextView timeCostNumber;
    private TextView creditsAppliedNumber;
    private RelativeLayout additionalStudentsRow;
    private TextView additionalStudentsLabel;
    private TextView additionalStudentsNumber;
    private RelativeLayout discountRow;
    private TextView discountLabel;
    private TextView discountNumber;
    private TextView estimatedTotalLabel;
    private TextView estimatedTotalNumber;
    private SeshEditText durationTextBox;
    private RequestActivity parentActivity;
    private float hourlyRate;
    private int additionalStudentsFee;
    private User currentUser;
    private SeshViewPager seshViewPager;
    private boolean isCompleted;
    private LearnRequest currentLearnRequest;
    private int currentHours;
    private int currentMinutes;
    private ArrayList<TextView> minutesViews;
    private ArrayList<TextView> hoursViews;


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_time_fragment, container, false);
        currentUser = User.currentUser(getActivity());

        parentActivity = (RequestActivity) getActivity();

        currentLearnRequest = parentActivity.getCurrentLearnRequest();

        timeCostLabel = (TextView) v.findViewById(R.id.time_cost_label);
        timeCostNumber = (TextView) v.findViewById(R.id.time_cost_number);

        hourlyRate = Constants.getHourlyRate(getActivity());
        String perMinuteString = CostUtils.floatToString(hourlyRate / 60, 2).substring(1);
        timeCostLabel.setText("Estimated cost ($" + perMinuteString + "/min)");

        creditsAppliedNumber = (TextView) v.findViewById(R.id.credits_applied_number);

        additionalStudentsFee = Constants.getAdditionalStudentFee(getActivity());
        additionalStudentsRow = (RelativeLayout) v.findViewById(R.id.additional_students_row);
        additionalStudentsLabel = (TextView) v.findViewById(R.id.additional_students_label);
        additionalStudentsLabel.setText("Additional Students ($" + additionalStudentsFee + "/hr)");
        additionalStudentsNumber = (TextView) v.findViewById(R.id.additional_students_number);

        discountRow = (RelativeLayout) v.findViewById(R.id.discount_row);
        discountLabel = (TextView) v.findViewById(R.id.discount_label);
        discountNumber = (TextView) v.findViewById(R.id.discount_number);

        if (currentLearnRequest.discount != null) {
            discountLabel.setText(currentLearnRequest.discount.learnRequestTitle);
        }

        estimatedTotalLabel = (TextView) v.findViewById(R.id.estimated_total_label);
        estimatedTotalNumber = (TextView) v.findViewById(R.id.estimated_total_number);

        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Bold.otf");
        estimatedTotalLabel.setTypeface(bold);

        durationTextBox = (SeshEditText) v.findViewById(R.id.duration_edit_text);

        isCompleted = false;

        configMinutesViewWithView(v);
        configHoursViewWithView(v);

        TextView nextButton = (TextView)v.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seshViewPager.flingNextFragment();

            }
        });

        return v;
    }

    View.OnClickListener hoursClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LayoutUtils layoutUtils = new LayoutUtils(getActivity());
            for (TextView textView : hoursViews) {
                if (textView == v) {
                    textView.setTypeface(layoutUtils.getMediumGothamTypeface());
                    textView.setTextColor(getActivity().getResources().getColor(R.color.seshorange));
                } else {
                    textView.setTypeface(layoutUtils.getLightGothamTypeface());
                    textView.setTextColor(getActivity().getResources().getColor(R.color.seshlightgray));
                }
            }

            String hours = ((TextView)v).getText().toString();
            setCurrentHours(Integer.parseInt(hours));
        }
    };

    View.OnClickListener minutesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            // run code
            LayoutUtils layoutUtils = new LayoutUtils(getActivity());
            for (TextView textView : minutesViews) {
                v.setFocusableInTouchMode(false);
                v.setFocusable(false);
                if (textView == v) {
                    textView.setTypeface(layoutUtils.getMediumGothamTypeface());
                    textView.setTextColor(getActivity().getResources().getColor(R.color.seshorange));
                } else {
                    textView.setTypeface(layoutUtils.getLightGothamTypeface());
                    textView.setTextColor(getActivity().getResources().getColor(R.color.seshlightgray));
                }
            }
            String minutes = ((TextView) v).getText().toString();
            setCurrentMinutes(Integer.parseInt(minutes));
        }
    };

    private void configMinutesViewWithView(final View view) {
        this.minutesViews = new ArrayList<TextView>();

        this.minutesViews.add((TextView) view.findViewById(R.id.minutes_0));
        this.minutesViews.add((TextView)view.findViewById(R.id.minutes_1));
        this.minutesViews.add((TextView)view.findViewById(R.id.minutes_2));
        this.minutesViews.add((TextView)view.findViewById(R.id.minutes_3));

        for (int i = 0; i < this.minutesViews.size(); i++) {

            TextView minutesView = this.minutesViews.get(i);
            minutesView.setOnClickListener(minutesClickListener);
        }

        // Select 30 minutes by default
        minutesClickListener.onClick(this.minutesViews.get(2));
    }

    private void configHoursViewWithView(View v) {
        this.hoursViews = new ArrayList<TextView>();

        this.hoursViews.add((TextView) v.findViewById(R.id.hours_0));
        this.hoursViews.add((TextView)v.findViewById(R.id.hours_1));
        this.hoursViews.add((TextView)v.findViewById(R.id.hours_2));
        this.hoursViews.add((TextView)v.findViewById(R.id.hours_3));



        for (int i = 0; i < this.hoursViews.size(); i++) {

            TextView hoursView = this.hoursViews.get(i);
            hoursView.setOnClickListener(hoursClickListener);
        }

        // Select 0 hours by default
        hoursClickListener.onClick(this.hoursViews.get(0));
    }

    private void setCurrentHours(int currentHours) {
        this.currentHours = currentHours;
        onDurationChanged();

        // Set visibility of minutes views
        for (int i = 0; i <= 1; i++) {
            ((TextView)this.minutesViews.get(i)).setVisibility(this.currentHours == 0 ? View.GONE : View.VISIBLE);
        }

        // Select 30 minutes if current h
        if (this.currentHours == 0 && currentMinutes < 30) {
            minutesClickListener.onClick(minutesViews.get(2));
        }
    }

    private void setCurrentMinutes(int currentMinutes) {
        this.currentMinutes = currentMinutes;
        onDurationChanged();
    }


    public void onDurationChanged() {
        String seshDuration = "";

        if (currentHours > 0) {
            if (currentHours == 1) {
                seshDuration += currentHours + " hr ";
            } else {
                seshDuration += currentHours + " hrs ";
            }
        }
        if (currentMinutes > 0) {
            seshDuration += currentMinutes + " min";
        }

        durationTextBox.setText(seshDuration);
        updateCostValues(currentHours, currentMinutes);
    }

    private void updateCostValues(int hourValue, int minuteValue) {
        float timeCostFloat = CostUtils.calculateCostForDuration(hourValue, minuteValue, hourlyRate);
        float additionalStudentsFloat
                = CostUtils.calculateAdditionalStudentCharge(hourValue, minuteValue, currentLearnRequest.numPeople, additionalStudentsFee);
        float discountFloat = 0;
        if (currentLearnRequest.discount != null) {
            discountFloat = Math.min(currentLearnRequest.discount.creditAmount, timeCostFloat);
        }
        float creditsAppliedFloat = Math.min(timeCostFloat + additionalStudentsFloat - discountFloat, currentUser.getCreditSum());
        float estimatedTotalFloat = CostUtils.calculateEstimatedTotal(timeCostFloat, additionalStudentsFloat,
                discountFloat, creditsAppliedFloat);
        creditsAppliedNumber.setText("-$" + CostUtils.floatToString(creditsAppliedFloat, 2));
        timeCostNumber.setText("$" + CostUtils.floatToString(timeCostFloat, 2));
        discountNumber.setText("-$" + CostUtils.floatToString(discountFloat, 2));
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
                (currentHours * 60) + currentMinutes;
        parentActivity.getCurrentLearnRequest().setEstTimeString(durationTextBox.getText());
    }

    @Override
    public void attachSeshViewPager(SeshViewPager seshViewPager) {
        this.seshViewPager = seshViewPager;
    }

    @Override
    public void onFragmentInForeground() {
        parentActivity.showRequestFlowNextButton();
        parentActivity.hideKeyboard();
        isCompleted = true;
    }

    @Override
    public void beforeFragmentInForeground() {
        if (currentLearnRequest.numPeople > 1) {
            additionalStudentsRow.setVisibility(View.VISIBLE);
        } else {
            additionalStudentsRow.setVisibility(View.GONE);
        }

        if (currentLearnRequest.discount != null) {
            discountRow.setVisibility(View.VISIBLE);
        } else {
            discountRow.setVisibility(View.GONE);
        }

        onDurationChanged();
    }
}
