package com.seshtutoring.seshapp.model;

import com.orm.SugarRecord;

import java.sql.Date;
import java.util.Set;

/**
 * Created by nadavhollander on 7/25/15.
 */
public class Sesh extends SugarRecord<Sesh> {
    public String class_name;
    public boolean has_been_seen;
    public boolean has_started;
    public boolean is_student;
    public double latitude;
    public String locationNotes;
    public double longitude;
    public int past_request_id;
    public String sesh_description;
    public int sesh_est_time;
    public int sesh_id;
    public int sesh_num_students;
    public Date sesh_set_time;
    public Date start_time;
    public double tutor_latitude;
    public double tutor_longitude;
    public String user_description;
    public String user_image_url;
    public String user_major;
    public String user_name;
    public String user_school;
    public boolean is_instant;
    public Set<AvailableBlock> availableBlocks;
    public Set<String> messages;
}
