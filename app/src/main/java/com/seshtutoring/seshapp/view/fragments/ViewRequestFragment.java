package com.seshtutoring.seshapp.view.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class ViewRequestFragment extends Fragment implements MainContainerActivity.FragmentOptionsReceiver  {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String ARG_REQUEST_ID = "request";
    public static final String REQUEST_KEY = "request_key";

    private int requestId;
    private LearnRequest request;
    SeshButton cancelRequestButton;
    SeshActivityIndicator seshActivityIndicator;
    LinearLayout middleContentView;
    EditText locationNotesEditText;

    private Map<String, Object> options;

    public static ViewRequestFragment newInstance(int requestId) {
        ViewRequestFragment fragment = new ViewRequestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_ID, requestId);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            requestId = getArguments().getInt(ARG_REQUEST_ID);
            List<LearnRequest> requestsFound = LearnRequest.find(LearnRequest.class, "learn_request_id = ?", Integer.toString(new Integer(requestId)));
            request = requestsFound.get(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_request, container, false);

        middleContentView = (LinearLayout) view.findViewById(R.id.middle_content_view);

        seshActivityIndicator = (SeshActivityIndicator)view.findViewById(R.id.view_request_networking_activity_indicator);
        seshActivityIndicator.setAlpha(0);

        locationNotesEditText = (EditText) view.findViewById(R.id.update_location_notes_edit_text);
        locationNotesEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                                            @Override
                                                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                                                v.clearFocus();
                                                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                                                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                                                updateRequestWithLocationNotes(locationNotesEditText.getText().toString());
                                                                return true;
                                                            }
                                                        });
        locationNotesEditText.setText(request.locationNotes);

        // Set class text
        SeshInformationLabel classLabel = (SeshInformationLabel)view.findViewById(R.id.class_label);
        classLabel.setText(request.classString);

        // Set subject text
        SeshInformationLabel subjectLabel = (SeshInformationLabel)view.findViewById(R.id.subject_label);
        subjectLabel.setText(request.descr);

        // Set time text
        SeshInformationLabel timeLabel = (SeshInformationLabel)view.findViewById(R.id.time_label);
        /* Round to nearest half hour */
        Double numHours = (request.estTime/30) / 2.0;
        DecimalFormat df = new DecimalFormat("0.#");
        String suffix = (numHours == 1.0) ? " Hour" : " Hours";
        classLabel.setText(df.format(numHours) + suffix);

        // Handle display of either set time or location notes
        SeshInformationLabel availableBlocksLabel = (SeshInformationLabel)view.findViewById(R.id.available_blocks_label);
        List<AvailableBlock> availableBlockList = AvailableBlock.find(AvailableBlock.class, "learn_request = ?", Long.toString(request.getId()));
        availableBlocksLabel.setText(AvailableBlock.getReadableBlocks(availableBlockList));

        cancelRequestButton = (SeshButton) view.findViewById(R.id.cancel_request_button);
        cancelRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButtonClicked();
            }
        });

        return view;
    }

    public void cancelButtonClicked() {
        String message = "Are you sure you would like to cancel your request?";
        createDialog("Cancel Request?", message, "CANCEL REQUEST", "NEVERMIND", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNetworking(true);
                SeshNetworking seshNetworking = new SeshNetworking(getActivity());
                seshNetworking.cancelRequestWithRequestId(request.learnRequestId, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        setNetworking(false);
                        try {
                            if (jsonObject.getString("status").equals("SUCCESS")) {
                                request.delete();
                                MainContainerActivity mainContainerActivity = (MainContainerActivity)getActivity();
                                mainContainerActivity.setCurrentState(mainContainerActivity.HOME, null);
                            } else {
                                SeshDialog.showDialog(getActivity().getFragmentManager(), "Whoops!", jsonObject.getString("message"),
                                        "OKAY", null, "view_request_network_error");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        setNetworking(false);
                        SeshDialog.showDialog(getActivity().getFragmentManager(), "Whoops!", "Please check your internet connection and try again!",
                                "OKAY", null, "view_request_network_error");
                    }
                });
            }
        });
    }

    private void setNetworking(Boolean networking) {
        cancelRequestButton.setEnabled(!networking);

        middleContentView
                .animate()
                .alpha(networking ? 0f : 1f)
                .setDuration(300)
                .setStartDelay(0)
                .start();

        seshActivityIndicator
                .animate()
                .alpha(networking ? 1f : 0f)
                .setDuration(300)
                .setStartDelay(0)
                .start();
    }

    private void createDialog(String title, String message, String firstChoice, String secondChoice, final View.OnClickListener firstClickListener) {
        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setDialogType(SeshDialog.SeshDialogType.TWO_BUTTON);
        seshDialog.setTitle(title);
        seshDialog.setMessage(message);
        seshDialog.setType("VIEW REQUEST CANCELLATION");
        seshDialog.setFirstChoice(firstChoice);
        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstClickListener.onClick(v);
                seshDialog.dismiss();
            }
        });
        seshDialog.setSecondChoice(secondChoice);
        seshDialog.setSecondButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seshDialog.dismiss();
            }
        });
        seshDialog.show(getActivity().getFragmentManager(), "REQUEST_CANCELLATION");
    }

    private void updateRequestWithLocationNotes(String locationNotes) {
        final String oldLocationNotes = request.locationNotes;
        request.locationNotes = (locationNotes);
        request.save();
        SeshNetworking seshNetworking = new SeshNetworking(getActivity());
        seshNetworking.setLocationNotesForRequest(request.learnRequestId, locationNotes, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {

                    } else {
                        presentErrorUpdatingLocationNotes(oldLocationNotes, jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                presentErrorUpdatingLocationNotes(oldLocationNotes, "Please check your internet connection and try again!");
            }
        });
    }

    private void presentErrorUpdatingLocationNotes(String oldLocationNotes, String message) {
        request.locationNotes = oldLocationNotes;
        request.save();
        SeshDialog.showDialog(getActivity().getFragmentManager(), "Whoops!", message,
                "OKAY", null, "view_request_network_error");
        locationNotesEditText.setText(oldLocationNotes);
    }

    @Override
    public void updateFragmentOptions(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public void clearFragmentOptions() {
        this.options = null;
    }

}
