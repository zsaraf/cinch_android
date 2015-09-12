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
import com.seshtutoring.seshapp.model.Discount;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
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

import org.json.JSONException;
import org.json.JSONObject;

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

    public static final String DIALOG_TYPE_LEARN_REQUEST_SUCCESS = "learn_request_success";
    public static final String DIALOG_TYPE_LEARN_REQUEST_FAILURE = "learn_request_failure";
    private Fragment[] requestFlowFragments = {
            new LearnRequestCourseFragment(),
            new LearnRequestAssignmentFragment(),
            new LearnRequestNumberOfStudentsFragment(),
            new LearnRequestTimeFragment(),
            new LearnRequestConfirmFragment()
    };
    private FrameLayout[] fragmentContainers;

    public static final int ENTER_LEARN_REQUEST_FLOW = 1;
    public static final int LEARN_REQUEST_CREATE_SUCCESS = 2;
    public static final int LEARN_REQUEST_CREATE_FAILURE = 3;
    public static final int LEARN_REQUEST_CREATE_EXITED = 4;

    private int selectedFragmentIndex;

    @SuppressWarnings("all")
    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        setContentView(R.layout.request_transparent_layout);

        // configure transparent status bar
        if (Build.VERSION.SDK_INT >= 19) {
            LayoutUtils utils = new LayoutUtils(this);
            RelativeLayout container = (RelativeLayout) findViewById(R.id.request_layout_container);
            container.setPadding(0, utils.getStatusBarHeight(), 0, 0);
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

        selectedFragmentIndex = 0;

        this.requestFlowSlider = (SeshViewPager) findViewById(R.id.request_flow_slider);
        this.learnRequestTopBar = (RelativeLayout) findViewById(R.id.learn_request_top_bar);
        this.learnRequestProgressBar = (LearnRequestProgressBar) findViewById(R.id.learn_request_progress_bar);

        requestFlowSlider.attachToActivity(this);
        requestFlowSlider.setViewPagerFragments(requestFlowFragments);
        requestFlowSlider.setProgressBar(learnRequestProgressBar);

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
                spring.setEndValue(utils.getStatusBarHeight());
                if (Build.VERSION.SDK_INT < 16) {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
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
        requestFlowOverlay.animate().alpha(1).setDuration(300).start();

        (new CreateLearnRequestAsyncTask()).execute(this, currentLearnRequest);
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
        LearnRequestConfirmFragment fragment = (LearnRequestConfirmFragment) requestFlowFragments[4];
        fragment.enableRequestButton();
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
                                        Intent intent = new Intent(MainContainerActivity.DISPLAY_SIDE_MENU_UPDATE, null,
                                                getApplicationContext(),
                                                MainContainerActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    }
                                });
                                animatedCheckmark.startAnimation();

                            }
                        });
            }
        }
    }
}
