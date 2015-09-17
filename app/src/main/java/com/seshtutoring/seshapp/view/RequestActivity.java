package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.RequestFuture;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Card;
import com.seshtutoring.seshapp.model.Discount;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.SeshMixpanelAPI;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.SeshNetworking.SynchronousRequest;
import com.seshtutoring.seshapp.view.components.LearnRequestProgressBar;
import com.seshtutoring.seshapp.view.components.SeshViewPager;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshAnimatedCheckmark;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestAssignmentFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestConfirmFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestCourseFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestNumberOfStudentsFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestTimeFragment;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;
import com.seshtutoring.seshapp.view.OnboardingActivity;
import com.seshtutoring.seshapp.view.OnboardingActivity.OnboardingRequirement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nadavhollander on 7/20/15.
 */
public class RequestActivity extends SeshActivity implements
        SeshDialog.OnSelectionListener {
    private static final String TAG = RequestActivity.class.getName();
    private RelativeLayout requestFlowNext;
    private RelativeLayout requestFlowClose;
    private LearnRequest currentLearnRequest;
    private Bitmap mapBackgroundInstance;
    private SeshViewPager requestFlowSlider;
    private LearnRequestProgressBar learnRequestProgressBar;
    private RelativeLayout learnRequestTopBar;
    private RelativeLayout requestFlowOverlay;
    private SeshActivityIndicator activityIndicator;
    private SeshAnimatedCheckmark animatedCheckmark;
    private boolean isKeyboardShowing;
    private SeshMixpanelAPI seshMixpanelAPI;

    public static final String DIALOG_TYPE_LEARN_REQUEST_FAILURE = "learn_request_failure";
    private List<Fragment> requestFlowFragments;

    public static final int CREATE_LEARN_REQUEST_REQUEST_CODE = 1;
    public static final int LEARN_REQUEST_CREATE_EXITED = 4;
    public static final int LEARN_REQUEST_CREATE_SUCCESSFUL_RESPONSE_CODE = 5;

    private int selectedFragmentIndex;

    @SuppressWarnings("all")
    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        setContentView(R.layout.request_transparent_layout);

        this.seshMixpanelAPI = ((SeshApplication)getApplication()).getSeshMixpanelAPI();

        // configure transparent status bar
        if (Build.VERSION.SDK_INT >= 19) {
            LayoutUtils utils = new LayoutUtils(this);
            RelativeLayout container = (RelativeLayout) findViewById(R.id.request_layout_container);
            container.setPadding(0, utils.getStatusBarHeight(), 0, 0);
        } else {

        }

        Intent intent = getIntent();

        this.currentLearnRequest = new LearnRequest();
        currentLearnRequest.latitude = intent.getDoubleExtra(LearnViewFragment.CHOSEN_LOCATION_LAT, 0);
        currentLearnRequest.longitude = intent.getDoubleExtra(LearnViewFragment.CHOSEN_LOCATION_LONG, 0);
        currentLearnRequest.setIsInstant(true);

        List<Discount> discounts = User.currentUser(this).getDiscounts();
        if (discounts.size() > 0) {
            currentLearnRequest.discount = discounts.get(0);
        }

        // set blurred background
        if (intent.hasExtra(LearnViewFragment.BLURRED_MAP_BITMAP_PATH_KEY)) {
            String path = getIntent().getStringExtra(LearnViewFragment.BLURRED_MAP_BITMAP_PATH_KEY);
            RelativeLayout requestLayoutBackground =
                    (RelativeLayout) findViewById(R.id.request_layout_background);

            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                requestLayoutBackground.setBackgroundDrawable(Drawable.createFromPath(path));
            } else {
                requestLayoutBackground.setBackground(Drawable.createFromPath(path));
            }

            this.mapBackgroundInstance = BitmapFactory.decodeFile(path);
        } else {
            Log.e(TAG, "Blurred background not included with intent to Request Layout");
        }

        this.requestFlowClose = (RelativeLayout) findViewById(R.id.request_flow_close);
        this.requestFlowNext = (RelativeLayout) findViewById(R.id.request_flow_next);

        requestFlowClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(LEARN_REQUEST_CREATE_EXITED);
                onBackPressed();
            }
        });

        requestFlowNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestFlowSlider.flingNextFragment();
            }
        });

        this.requestFlowFragments = new ArrayList<>();
        requestFlowFragments.add(new LearnRequestCourseFragment());
        requestFlowFragments.add(new LearnRequestAssignmentFragment());
        requestFlowFragments.add(new LearnRequestNumberOfStudentsFragment());
        requestFlowFragments.add(new LearnRequestTimeFragment());
        requestFlowFragments.add(new LearnRequestConfirmFragment());

        selectedFragmentIndex = 0;

        this.requestFlowSlider = (SeshViewPager) findViewById(R.id.request_flow_slider);
        this.learnRequestTopBar = (RelativeLayout) findViewById(R.id.learn_request_top_bar);
        this.learnRequestProgressBar = (LearnRequestProgressBar) findViewById(R.id.learn_request_progress_bar);

        requestFlowSlider.attachToActivity(this);
        requestFlowSlider.setViewPagerFragments(requestFlowFragments);
        requestFlowSlider.setProgressBar(learnRequestProgressBar);
        requestFlowSlider.setSwipingAllowed(true);

        final LayoutUtils utils = new LayoutUtils(this);
        final View rootView = (findViewById(android.R.id.content));
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                requestFlowSlider.startEntranceAnimation();
                learnRequestTopBar.setY(-1 * learnRequestTopBar.getHeight());

                Spring spring = SpringSystem.create().createSpring();
                spring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));
                spring.addListener(new SimpleSpringListener() {
                    public void onSpringUpdate(Spring spring) {
                        learnRequestTopBar.setY((int) spring.getCurrentValue());
                    }
                });

                spring.setCurrentValue(learnRequestTopBar.getY());
                double endValue = 0;
                if (Build.VERSION.SDK_INT >= 19) {
                    endValue += utils.getStatusBarHeight();
                }
                spring.setEndValue(endValue);
                rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        this.requestFlowOverlay = (RelativeLayout) findViewById(R.id.request_flow_overlay);
        this.activityIndicator = (SeshActivityIndicator) findViewById(R.id.request_activity_indicator);
        this.animatedCheckmark = (SeshAnimatedCheckmark) findViewById(R.id.animated_check_mark);
    }

    public LearnRequest getCurrentLearnRequest() {
        return currentLearnRequest;
    }

    public void createLearnRequest() {
        (new VerifyStudentOnboardingCompleteAsyncTask() {
            @Override
            protected void onPostExecute(ArrayList<OnboardingRequirement> onboardingRequirements) {
                if (onboardingRequirements.size() > 0) {
                    seshMixpanelAPI.track("Entered Student Onboarding");
                    showOnboardingDialog(onboardingRequirements);
                } else {
                    seshMixpanelAPI.track("Created Learn Request");
                    requestFlowOverlay.animate().alpha(1).setDuration(300).start();
                    (new CreateLearnRequestAsyncTask()).execute(getApplicationContext(), currentLearnRequest);
                }
            }
        }).execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == OnboardingActivity.ONBOARDING_REQUEST_CODE) {
            if (resultCode == OnboardingActivity.ONBOARDING_SUCCESSFUL_RESPONSE_CODE) {
                createLearnRequest();
            } else {
                onBackPressed();
            }
        }
    }

    @Override
    public void onDialogSelection(int selection, String type) {
        // do nothing
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard();
        overridePendingTransition(0, R.anim.fade_out);
    }

    public void showKeyboard() {
        if (!isKeyboardShowing) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(0, 0);
            isKeyboardShowing = true;
        }
    }

    public void hideKeyboard() {
        if (isKeyboardShowing) {
            // Check if no view has focus:
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            isKeyboardShowing = false;
        }
    }

    public void showRequestFlowNextButton() {
        if (requestFlowNext.getVisibility() == View.INVISIBLE) {
            requestFlowNext.setVisibility(View.VISIBLE);
        }
    }

    public void hideRequestFlowNextButton() {
        if (requestFlowNext.getVisibility() == View.VISIBLE) {
            requestFlowNext.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void reenableConfirmFragmentUsage() {
        LearnRequestConfirmFragment fragment = (LearnRequestConfirmFragment) requestFlowFragments.get(4);
        fragment.enableRequestButton();
    }

    private void showOnboardingDialog(final ArrayList<OnboardingRequirement> onboardingRequirements) {
        final RequestActivity requestActivity = this;

        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setDialogType(SeshDialog.SeshDialogType.TWO_BUTTON);
        seshDialog.setTitle("Onboarding");
        seshDialog.setMessage("Hey, we need to know a few things about you first!");
        seshDialog.setFirstChoice("OKAY");
        seshDialog.setSecondChoice("CANCEL");
        seshDialog.setType("onboarding");
        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seshDialog.dismiss(1);
                Intent intent = new Intent(getApplicationContext(), OnboardingActivity.class);
                intent.putExtra(OnboardingActivity.ONBOARDING_REQS_KEY, onboardingRequirements);
                intent.putExtra(OnboardingActivity.IS_STUDENT_ONBOARDING_KEY, true);
                startActivityForResult(intent, OnboardingActivity.ONBOARDING_REQUEST_CODE);
                overridePendingTransition(R.anim.fade_in, R.anim.hold);
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
                        requestActivity.onBackPressed();
                    }
                }, 1000);
            }
        });
        seshDialog.show(getFragmentManager(), null);
    }

    private abstract class VerifyStudentOnboardingCompleteAsyncTask extends AsyncTask<Void, Void, ArrayList<OnboardingRequirement>> {
        @Override
        protected ArrayList<OnboardingRequirement> doInBackground(Void... params) {
            User currentUser = User.currentUser(getApplicationContext());

            ArrayList<OnboardingRequirement> onboardingRequirements = new ArrayList<>();
            if (currentUser.profilePictureUrl == null || currentUser.profilePictureUrl.equals("")) {
                onboardingRequirements.add(OnboardingRequirement.PROFILE_PICTURE);
            }

            List<Card> cards = currentUser.getCards();
            if (cards == null || cards.size() == 0) {
                onboardingRequirements.add(OnboardingRequirement.CREDIT_CARD);
            }

            return onboardingRequirements;
        }
    }

    private class CreateLearnRequestAsyncTask extends AsyncTask<Object, Void, Void> {
        private SeshDialog errorDialog;
        private Context mContext;

        /**
         *
         * @param params (Context context, LearnRequest learnRequest)
         * @return
         */
        protected Void doInBackground(Object... params) {
            this.mContext = (Context) params[0];
            LearnRequest learnRequest = (LearnRequest) params[1];

            final SeshNetworking seshNetworking = new SeshNetworking(mContext);
            SynchronousRequest request = new SynchronousRequest() {
                @Override
                public void request(RequestFuture<JSONObject> blocker) {
                    seshNetworking
                            .createRequestWithLearnRequest(currentLearnRequest, blocker, blocker);
                }

                @Override
                public void onErrorException(Exception e) {
                    errorDialog = SeshDialog.createDialog("Network Error",
                            "We couldn't reach the server.  Check your network settings and try again.",
                            "Got it", null,
                            DIALOG_TYPE_LEARN_REQUEST_FAILURE);
                    Log.e(TAG, "Network Error: " + e.getMessage());
                }
            };

            JSONObject jsonObject = request.execute();

            if (jsonObject == null) return null;

            try {
                if (jsonObject.getString("status").equals("SUCCESS")) {
                    LearnRequest newLearnRequest
                            = LearnRequest.createOrUpdateLearnRequest(jsonObject.getJSONObject("learn_request"));
                    newLearnRequest.requiresAnimatedDisplay = true;
                    newLearnRequest.save();
                } else {
                    errorDialog = SeshDialog.createDialog("Whoops!",
                            jsonObject.getString("message"),
                            "Got it", null,
                            DIALOG_TYPE_LEARN_REQUEST_FAILURE);
                    Log.e(TAG, "Failed to create request, server error: " + jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                errorDialog = SeshDialog.createDialog("Whoops!",
                        "Something went wrong.  Try again later.",
                        "Got it", null,
                        DIALOG_TYPE_LEARN_REQUEST_FAILURE);
                Log.e(TAG, "Failed to create request, json malformed: " + e);
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            if (errorDialog != null) {
                requestFlowOverlay
                        .animate()
                        .setListener(null)
                        .alpha(0)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                errorDialog.show(getFragmentManager(), null);
                                reenableConfirmFragmentUsage();
                            }
                        });
                return;
            } else {
                activityIndicator
                        .animate()
                        .alpha(0)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                animatedCheckmark.setListener(new SeshAnimatedCheckmark.AnimationCompleteListener() {
                                    @Override
                                    public void onAnimationComplete() {
                                        setResult(LEARN_REQUEST_CREATE_SUCCESSFUL_RESPONSE_CODE);
                                        finish();
                                    }
                                });
                                animatedCheckmark.startAnimation();

                            }
                        });
            }
        }
    }
}
