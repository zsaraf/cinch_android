package com.seshtutoring.seshapp.model;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zacharysaraf on 1/27/16.
 */
public class Chatroom extends SugarRecord<Chatroom> {
    @Ignore
    private static final String TAG = Chatroom.class.getName();

    public int chatroomId;
    public String name;
    public int unreadActivityCount;

    public static Chatroom createOrUpdateChatroomWithObject(JSONObject chatroomJson, Context context) {
        Chatroom chatroom = null;
        try {
            int chatroomId = chatroomJson.getInt("id");
            String name = chatroomJson.getString("name");
            int unreadActivityCount = chatroomJson.getInt("unread_activity_count");

            if (Chatroom.listAll(Sesh.class).size() > 0) {
                List<Chatroom> chatroomsFound = Chatroom.find(Chatroom.class, "chatroom_id = ?", Integer.toString(chatroomId));
                if (chatroomsFound.size() > 0) {
                    chatroom = chatroomsFound.get(0);
                } else {
                    chatroom = new Chatroom();
                }
            } else {
                chatroom = new Chatroom();
            }

            chatroom.chatroomId = chatroomId;
            chatroom.name = name;
            chatroom.unreadActivityCount = unreadActivityCount;

            chatroom.save();

            Chatroom.deleteAll(ChatroomActivity.class, "chatroom = ?", Long.toString(chatroom.getId()));
            if (chatroomJson.get("chatroom_activities") != null) {
                JSONArray chatroomActivitiesJson = chatroomJson.getJSONArray("chatroom_activities");
                for (int i = 0; i < chatroomActivitiesJson.length(); i++) {
                    JSONObject chatroomActivityJsonObject = chatroomActivitiesJson.getJSONObject(i);
                    ChatroomActivity chatroomActivityObject = ChatroomActivity.createOrUpdateChatroomActivityWithJSON(chatroomActivityJsonObject, chatroom, context);
                    if (chatroomActivityObject != null) {
                        chatroomActivityObject.chatroom = chatroom;
                        chatroomActivityObject.save();
                    }
                }
            }

            chatroom.save();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed: " + e.getMessage());
            return null;
        }
        return chatroom;
    }

    public List<ChatroomActivity> getChatroomActivities() {
        return ChatroomActivity.find(ChatroomActivity.class, "chatroom = ?", Long.toString(this.getId()));
    }

}
