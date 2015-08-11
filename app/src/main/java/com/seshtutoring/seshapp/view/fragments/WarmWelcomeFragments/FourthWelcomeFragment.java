package com.seshtutoring.seshapp.view.fragments.WarmWelcomeFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.AuthenticationActivity;
import com.seshtutoring.seshapp.view.components.SeshButton;

/**
 * Created by nadavhollander on 8/10/15.
 */
public class FourthWelcomeFragment  extends Fragment {
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        View v = layoutInflater.inflate(R.layout.fourth_welcome_fragment, null);

        SeshButton signupButton = (SeshButton) v.findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (getActivity(), AuthenticationActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }
}
