package com.seshtutoring.seshapp.model;

import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/24/15.
 */
public class Student {
    private int studentId;
    private int userId;
    private int hoursLearned;
    private int credits;
    private User user;

    public Student(int studentId, int userId, int hoursLearned, int credits, User user) {
        this.studentId = studentId;
        this.userId = userId;
        this.hoursLearned = hoursLearned;
        this.credits = credits;
        this.user = user;
    }

    public static void createOrUpdateStudentWithObject(JSONObject studentJson) {

    }
}
