package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.Department;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.ViewSeshSetTimeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lillioetting on 7/14/15.
 */
public class ViewClassesView extends RelativeLayout {
    private static final String TAG = ViewClassesView.class.getName();
    public static final int editClassesButtonIdx = 0;

    private MainContainerActivity mainContainerActivity;
    private ListView menu;
    private ArrayList<CourseHolder> tutorCourses;
    private ViewClassesAdapter classesAdapter;
    private SeshNetworking seshNetworking;
    private Context mContext;
    private User user;
    private View bottomBorder;

    public interface ViewClassesViewListener {
        public void viewClassesViewDidTapAddClasses();
    }

    public ViewClassesViewListener listener;

    private class CourseHolder {

        public Object object;
        public int type;

        public CourseHolder(Object object, int type) {
            this.type = type;
            this.object = object;
        }

    }

    public ViewClassesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.view_classes_view, this, true);
        menu = (ListView)v.findViewById(R.id.list);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ViewClassesView,
                0, 0);

        boolean bottomBorderVisible;
        boolean dividerVisible;
        try {
            bottomBorderVisible = a.getBoolean(R.styleable.ViewClassesView_bottom_border_visible, true);
            dividerVisible = a.getBoolean(R.styleable.ViewClassesView_divider_visible, false);
        } finally {
            a.recycle();
        }

        bottomBorder = findViewById(R.id.bottom_border);
        bottomBorder.setVisibility(bottomBorderVisible ? VISIBLE : GONE);

        if (!dividerVisible) {
            menu.setDivider(null);
            menu.setDividerHeight(0);
        }

        init(attrs, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        LayoutUtils layUtils = new LayoutUtils(mContext);

        this.seshNetworking = new SeshNetworking(mContext);

        this.tutorCourses = new ArrayList<CourseHolder>();

        this.classesAdapter = new ViewClassesAdapter(mContext, tutorCourses);
        menu.setAdapter(classesAdapter);

        this.user = User.currentUser(mContext);
        refreshClassesViewWithUser(user);

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                CourseHolder obj = (CourseHolder) menu.getItemAtPosition(position);

                if (obj.type == 2 && listener != null) {
                    listener.viewClassesViewDidTapAddClasses();
                }

            }
        });
    }

    public void refreshClassesViewWithUser(User user) {
        if (user != null) {
            this.tutorCourses.clear();
            this.tutorCourses.add(new CourseHolder(null, 2));
            List<Department> tutorDepartments = user.tutor.getDepartments();
            Collections.sort(tutorDepartments, new Comparator<Department>() {
                @Override
                public int compare(Department lhs, Department rhs) {
                    return lhs.abbrev.compareTo(rhs.abbrev);
                }
            });
            for (int i = 0; i < tutorDepartments.size(); i++) {
                Department department = tutorDepartments.get(i);
                this.tutorCourses.add(new CourseHolder(department, 1));
            }
            List<Course> tutorCourses = user.tutor.getCourses();
            Collections.sort(tutorCourses, new Comparator<Course>() {
                @Override
                public int compare(Course lhs, Course rhs) {
                    return (lhs.deptAbbrev + lhs.number).compareTo(rhs.deptAbbrev + rhs.number);
                }
            });
            for (int i = 0; i < tutorCourses.size(); i++) {
                Course course = tutorCourses.get(i);
                // Find out if the tutor has this course as a tutor department

                if (shouldDisplayClass(course, tutorDepartments)) {
                    this.tutorCourses.add(new CourseHolder(course, 1));
                }
            }
            this.classesAdapter.notifyDataSetChanged();
        }
    }

    private Boolean shouldDisplayClass(Course course, List<Department> tutorDepartments) {
        for (Department department : tutorDepartments) {
            if (course.deptId == department.departmentId) return false;
        }
        return true;
    }

    private class ViewHolder {

        public TextView mainTextView;

    }

    public class ViewClassesAdapter extends ArrayAdapter<CourseHolder> {

        private Context mContext;
        private LayoutInflater layoutInflater;

        public ViewClassesAdapter(Context context, ArrayList<CourseHolder> tutorCourses) {
            super(context, R.layout.view_classes_row, tutorCourses);
            this.mContext = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            CourseHolder item = (CourseHolder) getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_classes_row,
                        null);

                viewHolder = new ViewHolder();

                int textID = R.id.course_title;
                Typeface typeFace = Typeface.createFromAsset(mContext.getAssets(), "fonts/Gotham-Light.otf");

                if (item.type == 2) {
                    //do something special for button?
                }

                viewHolder.mainTextView = (TextView) convertView.findViewById(textID);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (item.type == 2) {
                viewHolder.mainTextView.setText("+/- CLASSES");
            } else {
                if (item.object instanceof Course) {
                    Course course = (Course) item.object;
                    viewHolder.mainTextView.setText(course.shortFormatForTextView());
                } else {
                    Department department = (Department) item.object;
                    viewHolder.mainTextView.setText("ALL " + department.abbrev + " CLASSES");
                }
            }
            return convertView;
        }

    }

}
