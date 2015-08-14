package com.seshtutoring.seshapp.view.fragments.WarmWelcomeFragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.WarmWelcomeActivity;
import com.seshtutoring.seshapp.view.components.SeshButton;

/**
 * Created by nadavhollander on 8/10/15.
 */
public class SecondWelcomeFragment extends Fragment {
    private ImageView backgroundImage;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        View v = layoutInflater.inflate(R.layout.second_welcome_fragment, null);

        return v;
    }
}
