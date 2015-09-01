package com.seshtutoring.seshapp.view.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.StorageUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.ViewSeshSetTimeActivity;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragments.ViewSeshSeshDescriptionFragment;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragments.ViewSeshUserDescriptionFragment;
import com.squareup.picasso.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ViewSeshFragment extends Fragment implements MainContainerActivity.FragmentOptionsReceiver {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SESH_ID = "sesh";
    public static final String SESH_KEY = "sesh_key";

    private int seshId;
    private Sesh sesh;

    private ViewPager viewPager;
    private SeshActivityIndicator seshActivityIndicator;
    private FragmentActivity myContext;

    private SeshButton cancelSeshButton;
    private SeshButton messageButton;
    private SeshButton startSeshButton;

    private Map<String, Object> options;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param seshId Sesh to view.
     * @return A new instance of fragment ViewSeshFragment.
     */
    public static ViewSeshFragment newInstance(int seshId) {
        ViewSeshFragment fragment = new ViewSeshFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SESH_ID, seshId);
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

        if (!sesh.isStudent) {
            startSeshButton = (SeshButton) v.findViewById(R.id.start_sesh_button);
            startSeshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startSeshButtonClicked();
                }
            });
        }

        seshActivityIndicator = (SeshActivityIndicator) v.findViewById(R.id.view_sesh_activity_indicator);
        seshActivityIndicator.setAlpha(0);

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

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    private class ViewSeshPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_FRAGMENTS = 2;
        private Sesh sesh;
        private Fragment viewSeshFragments[];

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
                        MainContainerActivity mainContainerActivity = (MainContainerActivity)getActivity();
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
        // TODO: handle message action
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

}