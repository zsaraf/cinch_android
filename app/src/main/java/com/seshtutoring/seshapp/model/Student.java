package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 7/24/15.
 */
public class Student extends SugarRecord<Student> {
    @Ignore
    private static final String TAG = Student.class.getName();

    public int studentId;
    public int userId;
    public float hoursLearned;
    public float credits;

    // empty constructor necessary for SugarORM to work
    public Student() {}

    public Student(int studentId, int userId, int hoursLearned, float credits, User user) {
        this.studentId = studentId;
        this.userId = userId;
        this.hoursLearned = hoursLearned;
        this.credits = credits;
    }

    public static Student createOrUpdateStudentWithObject(JSONObject studentJson) {
        Student student;

        try {
            int studentId = studentJson.getInt("id");

            if (Student.listAll(Student.class).size() > 0) {
                List<Student> studentsFound = Student.find(Student.class, "student_id = ?", Integer.toString(studentId));
                if (studentsFound.size() > 0) {
                    student = studentsFound.get(0);
                } else {
                    student = new Student();
                }
            } else {
                student = new Student();
            }

            student.studentId = studentId;
            student.userId = studentJson.getInt("user");
            JSONObject stats = studentJson.getJSONObject("stats");
            student.hoursLearned = (float) stats.getDouble("hours_learned");
            student.credits = (float) stats.getDouble("credits");

            student.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update student in db; JSON user object from server is malformed.");
            return null;
        }
        return student;
    }
}
