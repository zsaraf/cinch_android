package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshAnimatedCheckmark;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class AddTutorClassesActivity extends AppCompatActivity {
    private static final String TAG = AddTutorClassesActivity.class.getName();

    private ArrayList<Course> classesToAdd;
    private ArrayList<Course> classesToDelete;
    private ArrayList<Course> courseResults;
    private SeshEditText courseEditText;
    private ListView courseResultsListView;
    private CourseResultsAdapter courseResultsAdapter;
    private List<Course> tutorClasses;
    private SeshNetworking seshNetworking;
    private RelativeLayout cancelButton;
    private RelativeLayout submitButton;
    private RelativeLayout requestFlowOverlay;
    private SeshActivityIndicator activityIndicator;
    private SeshAnimatedCheckmark animatedCheckmark;

    public static final int ADD_TUTOR_CLASSES_REFRESH = 9;
    public static final int ADD_TUTOR_CLASSES_NO_UPDATE = 10;
    public static final int ADD_TUTOR_CLASSES_CREATE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tutor_classes);

        // configure transparent status bar
        if (Build.VERSION.SDK_INT >= 19) {
            LayoutUtils utils = new LayoutUtils(this);
            RelativeLayout container = (RelativeLayout) findViewById(R.id.add_tutor_classes_container);
            container.setPadding(0, utils.getStatusBarHeight(), 0, 0);
        }

        this.tutorClasses = User.currentUser(this).tutor.getCourses();
        this.classesToAdd = new ArrayList<Course>();
        this.classesToDelete = new ArrayList<Course>();

        this.courseResults = new ArrayList<Course>();
        this.courseEditText = (SeshEditText) findViewById(R.id.course_edit_text);
        this.courseResultsListView = (ListView) findViewById(R.id.course_results_list);
        this.courseResultsAdapter = new CourseResultsAdapter(this, courseResults);

        this.requestFlowOverlay = (RelativeLayout) findViewById(R.id.add_tutor_classes_overlay);
        this.activityIndicator = (SeshActivityIndicator) findViewById(R.id.add_tutor_classes_activity_indicator);
        this.animatedCheckmark = (SeshAnimatedCheckmark) findViewById(R.id.animated_check_mark);

        this.submitButton = (RelativeLayout) findViewById(R.id.add_tutor_classes_check);
        this.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmitPressed();
            }
        });

        this.cancelButton = (RelativeLayout) findViewById(R.id.add_tutor_classes_close);
        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(ADD_TUTOR_CLASSES_NO_UPDATE, null);
                finish();
            }
        });

        this.courseResultsListView.setAdapter(courseResultsAdapter);
        this.courseResultsListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course selectedCourse = courseResults.get(position);

                if (tutorClasses.contains(selectedCourse)) {
                    if (classesToDelete.contains(selectedCourse)) {
                        classesToDelete.remove(selectedCourse);
                    } else {
                        classesToDelete.add(selectedCourse);
                    }
                } else {
                    if (classesToAdd.contains(selectedCourse)) {
                        classesToAdd.remove(selectedCourse);
                    } else {
                        classesToAdd.add(selectedCourse);
                    }
                }
                courseResultsAdapter.notifyDataSetChanged();
            }
        });
        this.seshNetworking = new SeshNetworking(this);

        courseEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (s.toString().equals("")) {
                            courseResults.clear();
                            return;
                        }
                    }
                });
                seshNetworking.searchForClassName(s.toString(), new Response.Listener<JSONObject>() {
                    public void onResponse(final JSONObject jsonResponse) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (jsonResponse.get("status").equals("SUCCESS")) {
                                        courseResults.clear();
                                        JSONArray classesArrayJson = jsonResponse.getJSONArray("classes");
                                        for (int i = 0; i < classesArrayJson.length(); i++) {
                                            courseResults.add(Course.createCourseFromSearchJSON((classesArrayJson.getJSONObject(i))));
                                        }
                                        courseResultsAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.e(TAG, jsonResponse.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        });
                    }


                }, new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, volleyError.getMessage());
                    }
                });
            }
        });
    }

    private class CourseResultsAdapter extends ArrayAdapter<Course> {
        private Context mContext;
        private LayoutInflater layoutInflater;

        public CourseResultsAdapter(Context context, ArrayList<Course> classResults) {
            super(context, R.layout.course_results_row, classResults);
            this.mContext = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return Math.min(courseResults.size(), maxRowsForScreenSize());
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Course courseRowData = getItem(position);
            View v = convertView;

            if (v == null) {
                v = layoutInflater.inflate(R.layout.course_results_row, null);
            }

            TextView classRowText = (TextView) v.findViewById(R.id.course_row_text);
            LayoutUtils layoutUtils = new LayoutUtils(AddTutorClassesActivity.this);
            classRowText.setTypeface(layoutUtils.getLightGothamTypeface());
            classRowText.setText(courseRowData.formatForTextView());

            if (shouldHighlightTutorCourse(courseRowData)) {
                classRowText.setTextColor(getResources().getColor(R.color.seshgreen));
            } else {
                classRowText.setTextColor(getResources().getColor(R.color.seshorange));
            }

            return v;
        }

        private int maxRowsForScreenSize() {
            return 5;
        }
    }

    private Boolean shouldHighlightTutorCourse(Course course) {
        return (tutorClasses.contains(course)) ?
                (!classesToDelete.contains(course)) :
                (classesToAdd.contains(course));
    }

    private void handleSubmitPressed() {
        setNetworking(true);

        seshNetworking.editTutorClasses(classesToAdd, classesToDelete, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.get("status").equals("SUCCESS")) {
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
                                                setResult(ADD_TUTOR_CLASSES_REFRESH, null);
                                                finish();
                                            }
                                        });
                                        animatedCheckmark.startAnimation();

                                    }
                                });
                    } else {
                        handleResponseError("Error!", jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    handleResponseError("Error", "We can't reach the servers. Try again in a few minutes!");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                handleResponseError("Error", "We can't reach the servers. Check your internet connection and try again!");
            }
        });
    }

    private void setNetworking(boolean networking) {
        if (networking) {
            InputMethodManager imm =
                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        cancelButton.setEnabled(!networking);
        submitButton.setEnabled(!networking);

        requestFlowOverlay.animate().alpha((networking) ? 1.0f : 0.0f).setDuration(300).start();
    }

    private void handleResponseError(String title, String message) {
        setNetworking(false);

        SeshDialog.showDialog(getFragmentManager(), title, message, "OKAY", null, "BLABLA");
    }

}
