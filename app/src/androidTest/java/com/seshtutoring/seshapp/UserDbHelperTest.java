package com.seshtutoring.seshapp;

import android.test.AndroidTestCase;

import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.db.DbOpenHelper;
import com.seshtutoring.seshapp.util.db.UserDbHelper;

import junit.framework.Assert;

/**
 * Created by nadavhollander on 7/7/15.
 */
public class UserDbHelperTest extends AndroidTestCase {
    private UserDbHelper userDbHelper;
    private User user;

    @Override
    protected void setUp() throws Exception {
        userDbHelper = new UserDbHelper(getContext());
        user = new User(6, "pratice420@gmail.com", "12345abcde", "Patrick Bateman");
    }

    @Override
    protected void tearDown() throws Exception {
        userDbHelper.deleteAllUsers();
    }

    public void testUserCreateSuccessful() {
        userDbHelper.createUser(user);
        User dbUser = userDbHelper.getCurrentUser();
        Assert.assertNotNull("Failed to retrieve new user from db.", dbUser);
        Assert.assertEquals(user.getUserId(), dbUser.getUserId());
        Assert.assertEquals(user.getEmail(), dbUser.getEmail());
        Assert.assertEquals(user.getFullName(), dbUser.getFullName());
        Assert.assertEquals(user.getSessionId(), dbUser.getSessionId());
    }

    public void testUserDeletion() {
        userDbHelper.createUser(user);
        Assert.assertEquals("User did not insert into DB properly.", 1, userDbHelper.getUserRowCount());
        userDbHelper.deleteAllUsers();
        Assert.assertEquals("Failed to delete all users from DB.", 0, userDbHelper.getUserRowCount());
    }
}
