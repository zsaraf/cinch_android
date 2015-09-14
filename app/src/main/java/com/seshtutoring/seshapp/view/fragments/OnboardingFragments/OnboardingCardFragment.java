package com.seshtutoring.seshapp.view.fragments.OnboardingFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.AddCardActivity;
import com.seshtutoring.seshapp.view.OnboardingActivity;
import com.seshtutoring.seshapp.view.OnboardingActivity.OnboardingRequirement;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshViewPager;

/**
 * Created by nadavhollander on 9/13/15.
 */
public class OnboardingCardFragment extends SeshViewPager.InputFragment {
    private boolean isCompleted;
    private SeshViewPager seshViewPager;
    private SeshButton goToPaymentButton;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.onboarding_card_fragment, container, false);
        this.goToPaymentButton = (SeshButton) view.findViewById(R.id.payment_button);
        goToPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddCardActivity.class);
                intent.putExtra(AddCardActivity.IS_RECIPIENT_INTENT_KEY, false);
                startActivityForResult(intent, AddCardActivity.ADD_CARD_REQUEST_CODE);
            }
        });
        isCompleted = false;
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == AddCardActivity.ADD_CARD_REQUEST_CODE
                && responseCode == AddCardActivity.CARD_ADDED_SUCCESSFULLY_RESPONSE_CODE) {
            isCompleted = true;
            ((OnboardingActivity) getActivity()).setRequirementFulfilled(OnboardingRequirement.CREDIT_CARD);
        }
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void attachSeshViewPager(SeshViewPager seshViewPager) {
        this.seshViewPager = seshViewPager;
    }
}
