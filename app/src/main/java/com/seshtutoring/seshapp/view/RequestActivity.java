package com.seshtutoring.seshapp.view;

import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableBlock;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.util.networking.SeshNetworking.SynchronousRequest;
import com.seshtutoring.seshapp.view.components.LearnRequestProgressBar;
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
public class RequestActivity extends SeshActivity implements EditText.OnEditorActionListener,
        SeshDialog.OnSelectionListener {
    private static final String TAG = RequestActivity.class.getName();
    private LearnRequestProgressBar progressBar;
    private RequestFlowViewPager viewPager;
    private ImageButton requestFlowNext;
    private ImageButton requestFlowClose;
    private LearnRequest currentLearnRequest;
    private Bitmap mapBackgroundInstance;
    public static final String DIALOG_TYPE_LEARN_REQUEST_SUCCESS = "learn_request_success";
    public static final String DIALOG_TYPE_LEARN_REQUEST_FAILURE = "learn_request_failure";

    private Fragment[] requestFlowFragments = {
            new LearnRequestCourseFragment(),
            new LearnRequestAssignmentFragment(),
            new LearnRequestNumberOfStudentsFragment(),
            new LearnRequestTimeFragment(),
            new LearnRequestConfirmFragment()
    };

    public interface InputFragment {
        public boolean isCompleted();
        public void saveValues();
    }

    public static final int ENTER_LEARN_REQUEST_FLOW = 1;
    public static final int LEARN_REQUEST_CREATE_SUCCESS = 2;
    public static final int LEARN_REQUEST_CREATE_FAILURE = 3;
    public static final int LEARN_REQUEST_CREATE_EXITED = 4;

    @SuppressWarnings("all")
    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        setContentView(R.layout.request_transparent_layout);

        Intent intent = getIntent();

        this.currentLearnRequest = new LearnRequest();
        currentLearnRequest.latitude = intent.getDoubleExtra(LearnViewFragment.CHOSEN_LOCATION_LAT, 0);
        currentLearnRequest.longitude = intent.getDoubleExtra(LearnViewFragment.CHOSEN_LOCATION_LONG, 0);

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

        this.progressBar = (LearnRequestProgressBar) findViewById(R.id.learn_request_progress_bar);
        this.viewPager = (RequestFlowViewPager) findViewById(R.id.learn_request_view_pager);

        progressBar.setActiveIconIndex(0);

        viewPager.setAdapter(new LearnRequestPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // do nothing
            }

            @Override
            public void onPageSelected(int position) {
                progressBar.setActiveIconIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        });

        this.requestFlowClose = (ImageButton) findViewById(R.id.request_flow_close);
        this.requestFlowNext = (ImageButton) findViewById(R.id.request_flow_next);
        requestFlowClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(LEARN_REQUEST_CREATE_EXITED);
                onBackPressed();
            }
        });


        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard();
            }
        }, 500);
    }

    private class LearnRequestPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_FRAGMENTS = 5;

        public LearnRequestPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return requestFlowFragments[position];
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }
    }

    public void nextFragment() {
        int currentItemIndex = viewPager.getCurrentItem();
        InputFragment currentFragment = (InputFragment) requestFlowFragments[currentItemIndex];

        // check if current fragment is completed
        if (currentFragment.isCompleted()) {
            if (currentItemIndex < 4) {
                currentFragment.saveValues();

                if (currentItemIndex == 3) {
                    LearnRequestConfirmFragment confirmFragment =
                            (LearnRequestConfirmFragment) requestFlowFragments[currentItemIndex + 1];
                    confirmFragment.fillInConfirmationBoxes();
                }

                viewPager.setCurrentItem(currentItemIndex + 1);
                progressBar.setActiveIconIndex(currentItemIndex + 1);

                if (currentItemIndex == 1) {
                    hideKeyboard();
                }
                // figure out
            }
        } else {
            // not allowed
        }
    }

    public LearnRequest getCurrentLearnRequest() {
        return currentLearnRequest;
    }

    public void createLearnRequest() {
        // temp until scheduling implemented
        currentLearnRequest.setIsInstant(true);

        if (currentLearnRequest.availableBlocks.size() == 0) {
            currentLearnRequest.createAvailableBlockForNow(1);
        }

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
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
            nextFragment();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard();
        overridePendingTransition(0, R.anim.fade_out);
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, 0);
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
}
