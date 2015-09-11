package com.seshtutoring.seshapp.view.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerStateManager;
import com.seshtutoring.seshapp.view.MainContainerStateManager.NavigationItemState;
import com.seshtutoring.seshapp.view.ViewSeshMapActivity;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class ViewRequestFragment extends Fragment implements MainContainerActivity.FragmentOptionsReceiver, OnMapReadyCallback {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String ARG_REQUEST_ID = "request";
    public static final String REQUEST_KEY = "request_key";

    private int requestId;
    private LearnRequest request;
    SeshButton cancelRequestButton;
    SeshActivityIndicator seshActivityIndicator;
    LinearLayout middleContentView;
    EditText locationNotesEditText;
    RelativeLayout topLayout;

    private GoogleMap mMap;

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
        timeLabel.setText(df.format(numHours) + suffix);

        // Handle display of either set time or location notes
        SeshInformationLabel availableBlocksLabel = (SeshInformationLabel)view.findViewById(R.id.available_blocks_label);
        List<AvailableBlock> availableBlockList = AvailableBlock.find(AvailableBlock.class, "learn_request = ?", Long.toString(request.getId()));
        if (request.isInstant() == true) {
            availableBlocksLabel.setText("NOW");
        } else {
            availableBlocksLabel.setText(Html.fromHtml(AvailableBlock.getReadableBlocks(availableBlockList)));
        }

        cancelRequestButton = (SeshButton) view.findViewById(R.id.cancel_request_button);
        cancelRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButtonClicked();
            }
        });

        topLayout = (RelativeLayout) view.findViewById(R.id.top_layout);
        topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewSeshMapActivity.class);
                intent.putExtra(ViewSeshMapActivity.LATITUDE, request.latitude);
                intent.putExtra(ViewSeshMapActivity.LONGITUDE, request.longitude);
                String classString = request.classString.replace(" ", "");
                intent.putExtra(ViewSeshMapActivity.TITLE, classString + " Request Location");
                startActivityForResult(intent, 1);
            }
        });

        setUpMapIfNeeded();

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
                                MainContainerActivity mainContainerActivity = (MainContainerActivity) getActivity();
                                MainContainerStateManager containerStateManager
                                        = mainContainerActivity.getContainerStateManager();
                                containerStateManager.setContainerStateForNavigation(NavigationItemState.HOME);
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

    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
//        if (mMap == null) {
        // Try to obtain the map from the SupportMapFragment.
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);
//        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
    }

    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(request.latitude, request.longitude), 18));
    }

}
