package com.seshtutoring.seshapp.view.fragments.OnboardingFragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.SeshNetworking.SynchronousRequest;
import com.seshtutoring.seshapp.view.OnboardingActivity;
import com.seshtutoring.seshapp.view.OnboardingActivity.OnboardingRequirement;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshEditText;
import com.seshtutoring.seshapp.view.components.SeshViewPager;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 9/12/15.
 */
public class OnboardingMajorFragment extends SeshViewPager.InputFragment implements
        EditText.OnEditorActionListener {
    private static final String TAG = OnboardingMajorFragment.class.getName();

    private SeshViewPager seshViewPager;
    private SeshEditText majorEditText;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.onboarding_major_fragment, container, false);
        this.majorEditText = (SeshEditText) view.findViewById(R.id.major_edit_text);
        majorEditText.setOnEditorActionListener(this);
        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
            updateMajor();
            return true;
        }
        return false;
    }

    private void updateMajor() {
        (new UpdateMajorAsyncTask()).execute();
    }

    private SeshDialog getErrorDialog(String title, String message) {
        ((OnboardingActivity) getActivity()).hideKeyboard();
        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setDialogType(SeshDialog.SeshDialogType.TWO_BUTTON);
        seshDialog.setTitle(title);
        seshDialog.setMessage(message);
        seshDialog.setType("onboarding_error");
        seshDialog.setFirstChoice("TRY AGAIN");
        seshDialog.setSecondChoice("CANCEL");
        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seshDialog.dismiss(1);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateMajor();
                    }
                }, 500);
            }
        });
        seshDialog.setSecondButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seshDialog.dismiss(2);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((OnboardingActivity) getActivity()).cancelOnboarding();
                    }
                }, 500);
            }
        });

        return seshDialog;
    }

    @Override
    public boolean isCompleted() {
        return (majorEditText.getText().length() > 0);
    }

    @Override
    public void attachSeshViewPager(SeshViewPager seshViewPager) {
        this.seshViewPager = seshViewPager;
    }

    public void onFragmentInForeground() {
        majorEditText.requestEditTextFocus();
        ((OnboardingActivity) getActivity()).showKeyboard();
    }

    private class UpdateMajorAsyncTask extends AsyncTask<Void, Void, Void> {
        private SeshDialog errorDialog;

        @Override
        public Void doInBackground(Void... params) {
            final User user = User.currentUser(getActivity());
            user.major = majorEditText.getText().toString();
            final SeshNetworking seshNetworking = new SeshNetworking(getActivity());
            SynchronousRequest request = new SynchronousRequest() {
                @Override
                public void request(RequestFuture<JSONObject> blocker) {
                    seshNetworking.updateUserInformationWithMajorAndBio(user.major, user.bio, blocker, blocker);
                }

                @Override
                public void onErrorException(Exception e) {
                    Log.e(TAG, "Failed to update major; network error: " + e);
                    errorDialog = getErrorDialog("Network Error", "Check your network connection and try again!");
                }
            };

            JSONObject jsonObject = request.execute();

            if (jsonObject != null) {
                user.save();
            }

            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            if (errorDialog == null) {
                ((OnboardingActivity) getActivity()).setRequirementFulfilled(OnboardingRequirement.MAJOR);
            } else {
                errorDialog.show(getActivity().getFragmentManager(), null);
            }
        }
    }
}
