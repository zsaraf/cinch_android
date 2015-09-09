package com.seshtutoring.seshapp.view;

import android.animation.TimeInterpolator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.SeshNetworking.SynchronousRequest;
import com.seshtutoring.seshapp.view.components.LearnRequestProgressBar;
import com.seshtutoring.seshapp.view.components.RequestFlowScrollView;
import com.seshtutoring.seshapp.view.components.RequestFlowViewPager;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestAssignmentFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestConfirmFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestCourseFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestNumberOfStudentsFragment;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestTimeFragment;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

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
    private RequestFlowScrollView requestFlowSlider;
    private LearnRequestProgressBar learnRequestProgressBar;
    private RelativeLayout learnRequestTopBar;
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

    public interface InputFragment {
        boolean isCompleted();
        void saveValues();
        void attachRequestFlowScrollView(RequestFlowScrollView requestFlowScrollView);
        void onFragmentInForeground();
        void beforeFragmentInForeground();
    }

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

        this.fragmentContainers = new FrameLayout[]{
                (FrameLayout) findViewById(R.id.course_fragment),
                (FrameLayout) findViewById(R.id.assignment_fragment),
                (FrameLayout) findViewById(R.id.num_students_fragment),
                (FrameLayout) findViewById(R.id.duration_fragment),
                (FrameLayout) findViewById(R.id.confirm_fragment)
        };

        final LayoutUtils utils = new LayoutUtils(this);

        FrameLayout leftBufferZone = (FrameLayout) findViewById(R.id.leftBufferZone);
        FrameLayout rightBufferZone = (FrameLayout) findViewById(R.id.rightBufferZone);

        int screenWidth = utils.getScreenWidthPx(this);
        setFrameLayoutWidth(leftBufferZone, screenWidth);
        setFrameLayoutWidth(rightBufferZone, screenWidth);
        for (FrameLayout fragmentContainer : fragmentContainers) {
            setFrameLayoutWidth(fragmentContainer, screenWidth);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.course_fragment, requestFlowFragments[0]).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.assignment_fragment, requestFlowFragments[1]).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.num_students_fragment, requestFlowFragments[2]).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.duration_fragment, requestFlowFragments[3]).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.confirm_fragment, requestFlowFragments[4]).commit();

        selectedFragmentIndex = 0;

        this.requestFlowSlider = (RequestFlowScrollView) findViewById(R.id.request_flow_slider);
        this.learnRequestTopBar = (RelativeLayout) findViewById(R.id.learn_request_top_bar);
        this.learnRequestProgressBar = (LearnRequestProgressBar) findViewById(R.id.learn_request_progress_bar);

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
                }            }
        });

        requestFlowSlider.setRequestFlowFragments(requestFlowFragments);
    }

    public LearnRequest getCurrentLearnRequest() {
        return currentLearnRequest;
    }

    public void createLearnRequest() {
        // @TODO temp until scheduling implemented
//        if (currentLearnRequest.availableBlocks.size() == 0) {
//            currentLearnRequest.createAvailableBlockForNow(1);
//        }

        (new CreateLearnRequestAsyncTask()).execute(this, currentLearnRequest);
    }

    @Override
    public void onDialogSelection(int selection, String type) {
        if (type.equals(DIALOG_TYPE_LEARN_REQUEST_SUCCESS)) {
            Intent intent = new Intent(MainContainerActivity.DISPLAY_SIDE_MENU_UPDATE, null, this,
                    MainContainerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
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
        fragment.hideActivityIndicator();
    }

    private class CreateLearnRequestAsyncTask extends AsyncTask<Object, Void, Void> {
        private SeshDialog responseDialog;
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
                    responseDialog = SeshDialog.createDialog("Network Error",
                            "We couldn't reach the server.  Check your network settings and try again.",
                            "Got it", null,
                            DIALOG_TYPE_LEARN_REQUEST_FAILURE);
                    Log.e(TAG, "Network Error: " + e.getMessage());
                }
            };

            JSONObject jsonObject = request.execute();

            try {
                if (jsonObject.getString("status").equals("SUCCESS")) {
                    LearnRequest newLearnRequest
                            = LearnRequest.createOrUpdateLearnRequest(jsonObject.getJSONObject("learn_request"));
                    newLearnRequest.requiresAnimatedDisplay = true;
                    newLearnRequest.save();

                    responseDialog = SeshDialog.createDialog("Request Created",
                            "Help is on the way! We'll notify you as soon as a tutor has accepted your Sesh request.  Hold tight!",
                            "Got it", null,
                            DIALOG_TYPE_LEARN_REQUEST_SUCCESS);
                } else {
                    responseDialog = SeshDialog.createDialog("Whoops!",
                            jsonObject.getString("message"),
                            "Got it", null,
                            DIALOG_TYPE_LEARN_REQUEST_FAILURE);
                    Log.e(TAG, "Failed to create request, server error: " + jsonObject.getString("message"));
                }
            } catch (JSONException e) {
                responseDialog = SeshDialog.createDialog("Whoops!",
                        "Something went wrong.  Try again later.",
                        "Got it", null,
                        DIALOG_TYPE_LEARN_REQUEST_FAILURE);
                Log.e(TAG, "Failed to create request, json malformed: " + e);
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            if (responseDialog == null) {
                Log.e(TAG, "No response dialog has been set for CreateLearnRequestAsyncTask");
                return;
            }

            reenableConfirmFragmentUsage();
            responseDialog.show(getFragmentManager(), null);
        }
    }

    private void setFrameLayoutWidth(FrameLayout frameLayout, int width) {
        frameLayout.setLayoutParams(
                new LinearLayout.LayoutParams(width,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }
}
