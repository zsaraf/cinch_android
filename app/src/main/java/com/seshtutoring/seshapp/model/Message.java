package com.seshtutoring.seshapp.model;

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

    // empty constructor necessary for SugarORM to work
    public Message() {}

    public Message(String content, Sesh sesh, long timestmap, boolean hasBeenRead, int messageId) {
        this.content = content;
        this.sesh = sesh;
        this.timestamp = timestmap;
        this.hasBeenRead = hasBeenRead;
        this.messageId = messageId;
    }

    public static Message createOrUpdateMessageWithJSON(JSONObject jsonObject, Sesh sesh) {

        Message message = null;
        try {
            int messageId = jsonObject.getInt(MESSAGE_ID_KEY);

            if (Message.listAll(Message.class).size() > 0) {
                List<Message> messagesFound = Message.find(Message.class, "message_id = ?", Integer.toString(messageId));
                if (messagesFound.size() > 0) {
                    message = messagesFound.get(0);
                } else {
                    message = new Message();
                }
            } else {
                message = new Message();
        }

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

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed: " + e.getMessage());
            return null;
        }
        return message;
    }

    private static long longTime(String rawTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
        return formatter.parseDateTime(rawTimeString).toDate().getTime();
    }


}
