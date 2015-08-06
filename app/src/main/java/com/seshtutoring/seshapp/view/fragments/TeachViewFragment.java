package com.seshtutoring.seshapp.view.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ViewAvailableJobsFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ViewClassesFragment;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import me.brendanweinstein.util.ToastUtils;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class TeachViewFragment extends Fragment {

    private View view;
    private MainContainerActivity activity;
    private boolean canSeeClasses;
    private TextView classesButtonText;
    private View tutorViewFrame;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        view = layoutInflater.inflate(R.layout.teach_view_fragment, container, false);
        LayoutUtils layUtils = new LayoutUtils(getActivity());
        view.setPadding(0, layUtils.getActionBarHeightPx() + layUtils.dpToPixels(40f) - 1, 0, 0);

        View viewClassesButton = view.findViewById(R.id.view_classes_button);
        classesButtonText = (TextView) view.findViewById(R.id.classes_button_text);
        tutorViewFrame = view.findViewById(R.id.tutor_view_frame);

        ImageView image = (ImageView) view.findViewById(R.id.tutor_classes_icon);
        image.setImageResource(R.drawable.book);
        activity = (MainContainerActivity) getActivity();

        classesButtonText.setOnClickListener(toggleViewClasses);

        canSeeClasses = false;
        setCurrentView();

        return view;
    }

    private View.OnClickListener toggleViewClasses = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            canSeeClasses = !canSeeClasses;
            setCurrentView();
        }
    };

    private void setCurrentView() {

        if (canSeeClasses) {
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.tutor_view_frame, new ViewClassesFragment(), "ViewClassesFragment")
                    .commit();

        }else {
            classesButtonText.setText(R.string.view_classes_off_text);
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.tutor_view_frame, new ViewAvailableJobsFragment(), "ViewJobsFragment")
                    .commit();

        }

    }


}
