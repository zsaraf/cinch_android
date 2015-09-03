package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.components.RequestFlowScrollView;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class LearnRequestNumberOfStudentsFragment extends Fragment implements RequestActivity.InputFragment {
    private int numStudents = -1;
    private RequestActivity parentActivity;
    private RequestFlowScrollView requestFlowScrollView;
    private Button[] numStudentsButtons;
    private Button selectedButton;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_number_of_students_fragment, container, false);

        this.parentActivity = (RequestActivity) getActivity();

        numStudentsButtons = new Button[6];

        numStudentsButtons[0] = (Button) v.findViewById(R.id.one_student_button);
        numStudentsButtons[1] = (Button) v.findViewById(R.id.two_students_button);
        numStudentsButtons[2] = (Button) v.findViewById(R.id.three_students_button);
        numStudentsButtons[3] = (Button) v.findViewById(R.id.four_students_button);
        numStudentsButtons[4] = (Button) v.findViewById(R.id.five_students_button);
        numStudentsButtons[5] = (Button) v.findViewById(R.id.six_students_button);

        for (int i = 0; i < numStudentsButtons.length; i++) {
            final int index = i;

            numStudentsButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectedButton != null) {
                        deselectButton(selectedButton);
                    }

                    selectedButton = numStudentsButtons[index];
                    selectButton(numStudentsButtons[index]);

                    numStudents = index + 1;
                    saveValues();

                    // give user time to see selection has been made
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestFlowScrollView.flingNextFragment();
                        }
                    }, 300);
                }
            });
        }
        return v;
    }

    private void selectButton(Button button) {
        if (Build.VERSION.SDK_INT < 16) {
            button.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.num_students_btn_selected));
        } else if (Build.VERSION.SDK_INT < 21) {
            button.setBackground(getResources()
                    .getDrawable(R.drawable.num_students_btn_selected));
        } else  {
            button.setBackground(getResources()
                    .getDrawable(R.drawable.num_students_btn_selected));
        }

        button.setTextColor(getResources().getColor(R.color.seshcharcoal));
    }

    private void deselectButton(Button button) {
        if (Build.VERSION.SDK_INT < 16) {
            button.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.num_students_btn_unselected));
        } else if (Build.VERSION.SDK_INT < 21) {
            button.setBackground(getResources()
                    .getDrawable(R.drawable.num_students_btn_unselected));
        } else  {
            button.setBackground(getResources()
                    .getDrawable(R.drawable.num_students_btn_unselected));
        }

        button.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void saveValues() {
        parentActivity.getCurrentLearnRequest().numPeople = numStudents;
    }

    @Override
    public boolean isCompleted() {
        return (numStudents != -1);
    }

    @Override
    public void attachRequestFlowScrollView(RequestFlowScrollView requestFlowScrollView) {
        this.requestFlowScrollView = requestFlowScrollView;
    }

    @Override
    public void onFragmentInForeground() {
        parentActivity.hideKeyboard();
        parentActivity.showRequestFlowNextButton();
    }

    @Override
    public void beforeFragmentInForeground() {
        // do nothing
    }
}
