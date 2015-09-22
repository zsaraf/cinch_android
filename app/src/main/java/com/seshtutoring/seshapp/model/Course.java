package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 7/22/15.
 */
public class Course extends SugarRecord<Course> {
    @Ignore
    private static final String TAG = Course.class.getName();

    private static final String NAME_KEY = "name";
    private static final String NUMBER_KEY = "number";
    private static final String DEPT_KEY = "department";
    private static final String ABBREV_KEY = "abbrev";
    private static final String COURSE_ID_KEY = "id";

    private static final String SEARCH_DEPT_ABBREV_KEY = "deptAbbrev";
    private static final String SEARCH_NUMBER_KEY = "classNumber";
    private static final String SEARCH_COURSE_ID_KEY = "classId";
    private static final String SEARCH_NAME_KEY = "className";



    public String name;
    public String number;
    public String deptAbbrev;
    public int courseId;
    public Tutor tutor;

    public Course() {}

    public Course(String name, String number, String deptAbbrev, int courseId, Tutor tutor) {
        this.name = name;
        this.number = number;
        this.deptAbbrev = deptAbbrev;
        this.courseId = courseId;
        this.tutor = tutor;
    }

    public static Course createCourseFromSearchJSON(JSONObject jsonObject) {
        Course returnCourse = new Course();

        try {
            returnCourse.name = jsonObject.getString(SEARCH_NAME_KEY);
            returnCourse.number = jsonObject.getString(SEARCH_NUMBER_KEY);
            returnCourse.deptAbbrev = jsonObject.getString(SEARCH_DEPT_ABBREV_KEY);
            returnCourse.courseId = jsonObject.getInt(SEARCH_COURSE_ID_KEY);

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return returnCourse;
    }

    public static Course createOrUpdateCourseWithJSON(JSONObject jsonObject, Tutor tutor, boolean isTemporary) {
        Course course = null;

        try {
            int courseId = jsonObject.getInt(COURSE_ID_KEY);
            if (!isTemporary) {
                course = createOrUpdateCourseWithId(courseId);
            } else {
                course = new Course();
                course.courseId = courseId;
            }

            course.name = jsonObject.getString(NAME_KEY);
            course.number = jsonObject.getString(NUMBER_KEY);
            course.deptAbbrev = jsonObject.getJSONObject(DEPT_KEY).getString(ABBREV_KEY);
            course.tutor = tutor;

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return course;
    }

    private static Course createOrUpdateCourseWithId(int courseId) {
        Course course = null;
        List<Course> coursesFound = Course.find(Course.class, "course_id = ?", Integer.toString(courseId));
        if (coursesFound.size() > 0) {
            course = coursesFound.get(0);
        } else {
            course = new Course();
            course.courseId = courseId;
        }

        return course;
    }

    @Override
    public boolean equals(Object other) {
        return this.courseId == ((Course)other).courseId;
    }

    public String formatForTextView() {
        return deptAbbrev + " " + number + ": " + name;
    }

    public String shortFormatForTextView() {
        return deptAbbrev + " " + number;
    }

}
