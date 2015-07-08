package com.seshtutoring.seshapp.model;

/**
 * Created by nadavhollander on 7/6/15.
 */
public class User {
    private String bio;
    private String fullLegalName;
    private String fullName;
    private String email;
    private int classYear;
    private boolean completedAppTour = false;
    private boolean completedOnBoarding = false;
    private boolean isVerified = false;
    private String major;
    private String profilePictureUrl;
    private int schoolId;
    private String sessionId;
    private String shareCode;
    private boolean tutorOfflinePing = false;
    private int userId;
    private int customerId;
    // private Student student;
    // private Tutor tutor;

    public User(int userId, String email, String sessionId, String fullName) {
        this.userId = userId;
        this.email = email;
        this.sessionId = sessionId;
        this.fullName = fullName;
    }

    public static User getCurrentUser() {
        return new User(0, null, null, null);
    }

    public String getBio() {
        return bio;
    }

    public String getFullLegalName() {
        return fullLegalName;
    }

    public String getFullName() {
        return fullName;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getSessionId() {
        return sessionId;
    }


}
