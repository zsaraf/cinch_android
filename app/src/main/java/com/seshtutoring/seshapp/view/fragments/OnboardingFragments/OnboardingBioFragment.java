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

import com.android.volley.toolbox.RequestFuture;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.OnboardingActivity;
import com.seshtutoring.seshapp.view.OnboardingActivity.OnboardingRequirement;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshViewPager;
import com.stripe.android.compat.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 9/12/15.
 */
public class OnboardingBioFragment extends SeshViewPager.InputFragment implements EditText.OnEditorActionListener {
    private static final String TAG = OnboardingBioFragment.class.getName();

    private SeshViewPager seshViewPager;
    private EditText bioEditText;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.onboarding_bio_fragment, container, false);
        this.bioEditText = (EditText) view.findViewById(R.id.bio_edit_text);
        bioEditText.setOnEditorActionListener(this);
        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
            updateBio();
            return true;
        }
        return false;
    }

    public boolean isCompleted() {
        return (bioEditText.getText().length() > 0);
    }
    public void saveValues() {
        (new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... params) {
                User user = User.currentUser(getActivity());
                user.bio = bioEditText.getText().toString();
                user.save();
                return null;
            }
        }).execute();
    }

    public void attachSeshViewPager(SeshViewPager seshViewPager) {
        this.seshViewPager = seshViewPager;
    }

    public void onFragmentInForeground() {
        bioEditText.requestFocus();
        ((OnboardingActivity)getActivity()).showKeyboard();
    }

    public void beforeFragmentInForeground() {
        // do nothing
    }

    private void updateBio() {
        (new UpdateBioAsyncTask()).execute();
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
                        updateBio();
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

    private class UpdateBioAsyncTask extends AsyncTask<Void, Void, Void> {
        private SeshDialog errorDialog;

        @Override
        public Void doInBackground(Void... params) {
            final User user = User.currentUser(getActivity());
            user.bio = bioEditText.getText().toString();
            final SeshNetworking seshNetworking = new SeshNetworking(getActivity());
            SeshNetworking.SynchronousRequest request = new SeshNetworking.SynchronousRequest() {
                @Override
                public void request(RequestFuture<JSONObject> blocker) {
                    seshNetworking.updateUserInformationWithMajorAndBio(user.major, user.bio, blocker, blocker);
                }

                @Override
                public void onErrorException(Exception e) {
                    Log.e(TAG, "Failed to update bio; network error: " + e);
                    errorDialog = getErrorDialog("Network Error", "Check your network connection and try again!");
                }
            };

            JSONObject jsonObject = request.execute();

            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        user.save();
                    } else {
                        Log.e(TAG, "Failed to update bio; " + jsonObject.getString("message"));
                        errorDialog = getErrorDialog("Error!", jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to update bio; json malformed " + e);
                    errorDialog = getErrorDialog("Network Error", "Check your network connection and try again!");
                }
            }

            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            if (errorDialog == null) {
                ((OnboardingActivity) getActivity()).setRequirementFulfilled(OnboardingRequirement.BIO);
            } else {
                errorDialog.show(getActivity().getFragmentManager(), null);
            }
        }
    }
}
