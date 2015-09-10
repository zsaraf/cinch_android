package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by franzwarning on 8/31/15.
 */
public class Message extends SugarRecord<Message> {
    private static final String TAG = Message.class.getName();

    private static final String CONTENT_KEY = "content";
    private static final String SESH_ID_KEY = "sesh_id";
    private static final String TO_USER_ID_KEY = "to_user_id";
    private static final String FROM_USER_ID_KEY = "from_user_id";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String HAS_BEEN_READ_KEY = "hasBeenRead";
    private static final String MESSAGE_ID_KEY = "id";

    public String content;
    public Sesh sesh;
    public long timestamp;
    public boolean hasBeenRead;
    public int messageId;
    public boolean fromYou;
    public boolean isPending;

    // empty constructor necessary for SugarORM to work
    public Message() {}

    public Message(String content, Sesh sesh, long timestmap, boolean hasBeenRead, int messageId, boolean fromYou, boolean isPending) {
        this.content = content;
        this.sesh = sesh;
        this.timestamp = timestmap;
        this.hasBeenRead = hasBeenRead;
        this.messageId = messageId;
        this.fromYou = fromYou;
        this.isPending = isPending;
    }

    public static Message createOrUpdateMessageWithJSON(JSONObject jsonObject, Sesh sesh, Context context) {

        Message message = null;
        try {

            int messageId = jsonObject.getInt(MESSAGE_ID_KEY);
            message = createOrUpdateMessageWithId(messageId);

            // Assign all the properties of the message
            String timestamp = jsonObject.getString(TIMESTAMP_KEY);
            if (!timestamp.equals("null")) {
                message.timestamp = longTime(timestamp);
            } else {
                message.timestamp = -1;
            }
            message.content = jsonObject.getString(CONTENT_KEY);
            message.sesh = sesh;
            message.hasBeenRead = (jsonObject.getInt(HAS_BEEN_READ_KEY) != 0);
            message.messageId = messageId;
            message.isPending = false;

            User currentUser = User.currentUser(context);
            message.fromYou = (jsonObject.getInt(FROM_USER_ID_KEY) == currentUser.userId);

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed: " + e.getMessage());
            return null;
        }
        return message;
    }

    public static int getUnreadMessagesCount(List<Message> messages) {
        int count = 0;

        for (int i = 0; i < messages.size(); i++) {
            Message currentMessage = messages.get(i);

            if (!currentMessage.hasBeenRead) {
                count++;
            }
        }

        return count;
    }

    public static void listMesssagesAsRead(List<Message> messages) {
        int count = 0;

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (!message.hasBeenRead) {
                message.hasBeenRead = true;
                message.save();
                count++;
            }
        }

        Message message = messages.get(0);
        message.sesh.numUnreadMessages -= count;
        message.sesh.save();
    }

    private static Message createOrUpdateMessageWithId(int messageId) {
        Message message = null;
        List<Message> messagesFound = Message.find(Message.class, "message_id = ?", Integer.toString(messageId));
        if (messagesFound.size() > 0) {
            message = messagesFound.get(0);
        } else {
            message = new Message();
            message.messageId = messageId;
        }

        return message;
    }

    private static long longTime(String rawTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
        return formatter.parseDateTime(rawTimeString).toDate().getTime();
    }


}
