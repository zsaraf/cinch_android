package com.seshtutoring.seshapp.view.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.seshtutoring.seshapp.model.Message;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.DateUtils;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.StorageUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MessagingActivity;
import com.seshtutoring.seshapp.view.ReportProblemActivity;
import com.seshtutoring.seshapp.view.ViewSeshMapActivity;
import com.seshtutoring.seshapp.view.ViewSeshSetTimeActivity;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragments.ViewSeshSeshDescriptionFragment;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragments.ViewSeshUserDescriptionFragment;
import com.squareup.picasso.Callback;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ViewSeshFragment extends Fragment implements MainContainerActivity.FragmentOptionsReceiver, OnMapReadyCallback {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SESH_ID = "sesh";
    public static final String SESH_KEY = "sesh_key";
    public static final String OPEN_MESSAGING = "open_messaging";
    static final int MESSAGE_ACTIVITY_CLOSED = 1;


    private int seshId;
    private Sesh sesh;

    private ViewPager viewPager;
    private SeshActivityIndicator seshActivityIndicator;
    private FragmentActivity myContext;

    private SeshButton cancelSeshButton;
    private SeshButton messageButton;
    private SeshButton startSeshButton;

    RelativeLayout topLayout;

    private GoogleMap mMap;

    private Map<String, Object> options;

    private BroadcastReceiver broadcastReceiver;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param seshId Sesh to view.
     * @return A new instance of fragment ViewSeshFragment.
     */
    public static ViewSeshFragment newInstance(int seshId, boolean openMessaging) {
        ViewSeshFragment fragment = new ViewSeshFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SESH_ID, seshId);
        args.putBoolean(OPEN_MESSAGING, openMessaging);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            seshId = getArguments().getInt(ARG_SESH_ID);
            List<Sesh> seshesFound = Sesh.find(Sesh.class, "sesh_id = ?", Integer.toString(new Integer(seshId)));
            sesh = seshesFound.get(0);

            boolean openMessaging = getArguments().getBoolean(OPEN_MESSAGING);
            if (openMessaging) {
                messageButtonClicked();
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v;
        if (sesh.isStudent) {
            v = inflater.inflate(R.layout.fragment_view_sesh_student, container, false);
            final EditText editText = (EditText) v.findViewById(R.id.icon_text_view_text);
            editText.setText(sesh.locationNotes);
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    updateSeshWithLocationNotes(editText.getText().toString());
                    return true;
                }
            });
        } else {
            v = inflater.inflate(R.layout.fragment_view_sesh_tutor, container, false);
            final RelativeLayout middleBar = (RelativeLayout) v.findViewById(R.id.middleBar);
            final TextView textView = (TextView) v.findViewById(R.id.icon_text_view_text);
            if (sesh.seshSetTime > 0) {
                textView.setText(DateUtils.getSeshFormattedDate(new DateTime(sesh.seshSetTime)));
            }
            middleBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startSetTimeActivityWithBlurTransition();
                }
            });
        }

        final ImageView profileImageView = (ImageView) v.findViewById(R.id.profile_image);
        SeshNetworking seshNetworking = new SeshNetworking(myContext);
        seshNetworking.downloadProfilePictureAsync(sesh.userImageUrl, profileImageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });

        cancelSeshButton = (SeshButton) v.findViewById(R.id.cancel_sesh_button);
        cancelSeshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButtonClicked();
            }
        });

        messageButton = (SeshButton) v.findViewById(R.id.message_button);
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageButtonClicked();
            }
        });

        refreshMessageButtonText();

        if (!sesh.isStudent) {
            startSeshButton = (SeshButton) v.findViewById(R.id.start_sesh_button);
            startSeshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startSeshButtonClicked();
                }
            });
        }

        broadcastReceiver = actionBroadcastReceiver;

        seshActivityIndicator = (SeshActivityIndicator) v.findViewById(R.id.view_sesh_activity_indicator);
        seshActivityIndicator.setAlpha(0);

        topLayout = (RelativeLayout) v.findViewById(R.id.top_layout);
        topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(myContext, ViewSeshMapActivity.class);
                intent.putExtra(ViewSeshMapActivity.LATITUDE, sesh.latitude);
                intent.putExtra(ViewSeshMapActivity.LONGITUDE, sesh.longitude);
                String classString = sesh.className.replace(" ", "");
                intent.putExtra(ViewSeshMapActivity.TITLE, classString + " Sesh Location");
                startActivityForResult(intent, 1);
            }
        });

        viewPager = (ViewPager) v.findViewById(R.id.view_sesh_view_pager);

        viewPager.setAdapter(new ViewSeshPagerAdapter(myContext.getSupportFragmentManager(), sesh));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        });

        setUpMapIfNeeded();

        refresh();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if the RequestCode = RESULT_DIAL then do waht you want
        if (requestCode == MESSAGE_ACTIVITY_CLOSED) {
            refreshMessageButtonText();
        }
    }

    public void refreshMessageButtonText() {
        // Check to see if any messages are unread
        List<Message> messages = sesh.getMessages();
        int unreadMessages  = Message.getUnreadMessagesCount(messages);
        String messageText = "MESSAGE";
        if (unreadMessages > 0 && unreadMessages < 10) {
            messageText += " (" + unreadMessages + ")";
        } else if (unreadMessages >= 10) {
            messageText += " (10+)";
        }

        messageButton.setText(messageText);

    }


    @Override
    public void onStart() {
        super.onStart();

        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessagingActivity.REFRESH_MESSAGES);
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshMessageButtonText();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    private void updateNavBarTitle() {
        String classString = sesh.className.replace(" ", "");
        String navigationItemTitle = "";
        if (sesh.isInstant) {
            navigationItemTitle = classString + " @ NOW";
        } else if (sesh.seshSetTime > 0) {
            DateTime dateTime = new DateTime(sesh.seshSetTime);
            navigationItemTitle = classString + " " + DateUtils.getSeshFormattedDate(dateTime);
        } else {
            String firstName = sesh.firstName();
            navigationItemTitle = classString + " with " + firstName;
        }

        ((MainContainerActivity)getActivity()).setActionBarTitle(navigationItemTitle);
    }

    public void refresh() {
        updateNavBarTitle();
        ((ViewSeshPagerAdapter)viewPager.getAdapter()).refresh();
    }


    private class ViewSeshPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_FRAGMENTS = 2;
        private Sesh sesh;
        private Fragment viewSeshFragments[];
        private ViewSeshUserDescriptionFragment viewSeshUserDescriptionFragment;
        private ViewSeshSeshDescriptionFragment viewSeshSeshDescriptionFragment;

        public ViewSeshPagerAdapter(FragmentManager fm, Sesh sesh) {
            super(fm);
            this.sesh = sesh;
            viewSeshFragments = new Fragment[2];
            if (sesh.isStudent) {
                viewSeshFragments[0] = ViewSeshUserDescriptionFragment.newInstance(sesh.seshId);
                viewSeshFragments[1] = ViewSeshSeshDescriptionFragment.newInstance(sesh.seshId);
            } else {
                viewSeshFragments[0] = ViewSeshSeshDescriptionFragment.newInstance(sesh.seshId);
                viewSeshFragments[1] = ViewSeshUserDescriptionFragment.newInstance(sesh.seshId);
            }
        }

        @Override
        public Fragment getItem(int position) {
            return viewSeshFragments[position];
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }

        public void refresh() {
            if (viewSeshUserDescriptionFragment != null && viewSeshSeshDescriptionFragment != null) {
                viewSeshUserDescriptionFragment.refresh();
                viewSeshSeshDescriptionFragment.refresh();
            }
        }
    }

    @Override
    public void updateFragmentOptions(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public void clearFragmentOptions() {
        this.options = null;
    }

    private void cancelButtonClicked() {
        Boolean isStudent = true;
        String message = isStudent ? "Are you sure you would like to cancel your Sesh?" : "Please note that if you cancel too many times, you will lose tutoring privileges.";
        createDialog("Cancel Sesh?", message, "CANCEL SESH", "NEVERMIND", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNetworking(true);
                SeshNetworking seshNetworking = new SeshNetworking(myContext);
                seshNetworking.cancelSeshWithSeshId(sesh.seshId, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        setNetworking(false);
                        sesh.delete();
                        MainContainerActivity mainContainerActivity = (MainContainerActivity) getActivity();
                        mainContainerActivity.setCurrentState(mainContainerActivity.HOME, null);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        setNetworking(false);
                    }
                });
            }
        });
    }

    private void startSeshButtonClicked() {
        createDialog("Start Sesh?", "You should be sitting with your student at this point!", "BEGIN", "CANCEL", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: handle start sesh action
                setNetworking(true);
                SeshNetworking seshNetworking = new SeshNetworking(myContext);
                seshNetworking.startSeshWithSeshId(sesh.seshId, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        setNetworking(false);
                    }
                });
            }
        });
    }

    private void messageButtonClicked() {
        Intent intent = new Intent(myContext, MessagingActivity.class);
        intent.putExtra(MessagingActivity.SESH_ID, this.sesh.seshId);
        startActivityForResult(intent, MESSAGE_ACTIVITY_CLOSED);
    }

    private void createDialog(String title, String message, String firstChoice, String secondChoice, final View.OnClickListener firstClickListener) {
        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setDialogType(SeshDialog.SeshDialogType.TWO_BUTTON);
        seshDialog.setTitle(title);
        seshDialog.setMessage(message);
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
        seshDialog.setType("SESH_STARTING");

        seshDialog.show(getActivity().getFragmentManager(), "SESH_STARTING");
    }

    private void setNetworking(Boolean networking) {
        cancelSeshButton.setEnabled(!networking);
        messageButton.setEnabled(!networking);
        if (startSeshButton != null) {
            startSeshButton.setEnabled(!networking);
        }

        viewPager
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

    private void startSetTimeActivityWithBlurTransition() {
        Intent intent = new Intent(getActivity(), ViewSeshSetTimeActivity.class);
        intent.putExtra(ViewSeshSetTimeActivity.SET_TIME_SESH_ID_KEY, sesh.seshId);
        startActivityForResult(intent, 0);
        getActivity().overridePendingTransition(R.anim.fade_in, 0);
    }

    private void updateSeshWithLocationNotes(String locationNotes) {
        final String oldLocationNotes = sesh.locationNotes;
        sesh.setLocationNotes(locationNotes);
        SeshNetworking seshNetworking = new SeshNetworking(getActivity());
        seshNetworking.setLocationNotesForSesh(sesh.seshId, locationNotes, new Response.Listener<JSONObject>() {
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
        sesh.locationNotes = oldLocationNotes;
        sesh.save();
        SeshDialog.showDialog(getActivity().getFragmentManager(), "Whoops!", message,
                "OKAY", null, "view_request_network_error");
        final EditText editText = (EditText) getView().findViewById(R.id.icon_text_view_text);
        editText.setText(oldLocationNotes);
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
                new LatLng(sesh.latitude, sesh.longitude), 15));
    }

}