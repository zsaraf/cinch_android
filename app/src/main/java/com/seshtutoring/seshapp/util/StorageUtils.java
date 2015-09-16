package com.seshtutoring.seshapp.util;

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.nfc.Tag;
        import android.util.Log;

        import com.seshtutoring.seshapp.model.AvailableBlock;
        import com.seshtutoring.seshapp.model.AvailableJob;
        import com.seshtutoring.seshapp.model.Card;
        import com.seshtutoring.seshapp.model.Constants;
        import com.seshtutoring.seshapp.model.Course;
        import com.seshtutoring.seshapp.model.Discount;
        import com.seshtutoring.seshapp.model.LearnRequest;
        import com.seshtutoring.seshapp.model.Message;
        import com.seshtutoring.seshapp.model.Notification;
        import com.seshtutoring.seshapp.model.OutstandingCharge;
        import com.seshtutoring.seshapp.model.PastRequest;
        import com.seshtutoring.seshapp.model.PastSesh;
        import com.seshtutoring.seshapp.model.School;
        import com.seshtutoring.seshapp.model.Sesh;
        import com.seshtutoring.seshapp.model.Student;
        import com.seshtutoring.seshapp.model.Tutor;
        import com.seshtutoring.seshapp.model.User;

        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.OutputStream;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class StorageUtils {
    private static final String TAG = StorageUtils.class.getName();

    public static File storeTempImage(Context context, Bitmap image, String fileName) {
        File file = null;

        try {
            file = File.createTempFile(fileName, null, context.getCacheDir());
            file.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error accessing file: " + e.getMessage());
        }

        return file;
    }

    public static void clearAllSugarRecords() {
        AvailableBlock.deleteAll(AvailableBlock.class);
        Card.deleteAll(Card.class);
        Discount.deleteAll(Discount.class);
        LearnRequest.deleteAll(LearnRequest.class);
        Message.deleteAll(Message.class);
        Notification.deleteAll(Notification.class);
        OutstandingCharge.deleteAll(OutstandingCharge.class);
        PastRequest.deleteAll(PastRequest.class);
        PastSesh.deleteAll(PastSesh.class);
        School.deleteAll(School.class);
        Sesh.deleteAll(Sesh.class);
        Student.deleteAll(Student.class);
        Tutor.deleteAll(Tutor.class);
        User.deleteAll(User.class);
        Course.deleteAll(Course.class);
    }
}
