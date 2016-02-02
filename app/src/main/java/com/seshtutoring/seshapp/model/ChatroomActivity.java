package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.seshtutoring.seshapp.util.DateUtils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by zacharysaraf on 1/27/16.
 */
public class ChatroomActivity extends SugarRecord<ChatroomActivity> {
    @Ignore
    private static final String TAG = Chatroom.class.getName();

    public int chatroomActivityId;
    public String message;
    public Boolean fromYou;
    public Boolean isPending;
    public long timestamp;
    public Chatroom chatroom;

    public static ChatroomActivity createOrUpdateChatroomActivityWithJSON(JSONObject jsonObject, Chatroom chatroom, Context context) {
        ChatroomActivity chatroomActivity = null;
        try {

            String identifier = jsonObject.getJSONObject("chatroom_activity_type").getString("identifier");
            if (!identifier.equals("message")) {
                return null;
            }

            JSONObject messageObject = jsonObject.getJSONObject("activity");
            Boolean fromYou = messageObject.getJSONObject("chatroom_member").getJSONObject("user").getInt("id") == User.currentUser(context).userId;

            int chatroomActivityId = jsonObject.getInt("id");
            chatroomActivity = createOrUpdateChatroomActivityWithId(chatroomActivityId);

            // Assign all the properties of the message
            String timestamp = jsonObject.getString("timestamp");
            if (!timestamp.equals("null")) {
                chatroomActivity.timestamp = DateUtils.djangoFormattedTime(jsonObject.getString("timestamp")).getTime();
            } else {
                chatroomActivity.timestamp = -1;
            }
            chatroomActivity.message = messageObject.getString("message");
            chatroomActivity.chatroom = chatroom;
            chatroomActivity.fromYou = fromYou;
            chatroomActivity.isPending = false;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed: " + e.getMessage());
            return null;
        }
        return chatroomActivity;
    }

    private static ChatroomActivity createOrUpdateChatroomActivityWithId(int chatroomActivityId) {
        ChatroomActivity chatroomActivity = null;
        List<ChatroomActivity> chatroomActivitiesFound = ChatroomActivity.find(ChatroomActivity.class, "chatroom_activity_id = ?", Integer.toString(chatroomActivityId));
        if (chatroomActivitiesFound.size() > 0) {
            chatroomActivity = chatroomActivitiesFound.get(0);
        } else {
            chatroomActivity = new ChatroomActivity();
            chatroomActivity.chatroomActivityId = chatroomActivityId;
        }

        return chatroomActivity;
    }

    private static long longTime(String rawTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
        return formatter.parseDateTime(rawTimeString).toDate().getTime();
    }

}
