package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by zacharysaraf on 9/29/15.
 */
public class Department extends SugarRecord<Course> {
    @Ignore
    private static final String TAG = Department.class.getName();

    private static final String ABBREV_KEY = "abbrev";
    private static final String ID_KEY = "id";
    private static final String NAME_KEY = "name";

    public int departmentId;
    public String abbrev;
    public String departmentName;
    public Tutor tutor;

    public Department() {}

    public Department(int departmentId, String abbrev, String departmentName) {
        this.departmentName = departmentName;
        this.abbrev = abbrev;
        this.departmentId = departmentId;
    }

    public static Department createOrUpdateDepartmentWithJSON(JSONObject jsonObject, Boolean isTemporary) {
        Department department = null;

        try {
            int departmentId = jsonObject.getInt(ID_KEY);
            if (!isTemporary) {
                department = createOrUpdateDepartmentWithId(departmentId);
            } else {
                department = new Department();
                department.departmentId = departmentId;
            }

            department.departmentName = jsonObject.getString(NAME_KEY);
            department.abbrev = jsonObject.getString(ABBREV_KEY);

            if (!isTemporary) {
                department.save();
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return department;
    }

    private static Department createOrUpdateDepartmentWithId(int departmentId) {
        Department department = null;
        List<Department> departmentsFound = Department.find(Department.class, "department_id = ?", Integer.toString(departmentId));
        if (departmentsFound.size() > 0) {
            department = departmentsFound.get(0);
        } else {
            department = new Department();
            department.departmentId = departmentId;
        }

        return department;
    }

    @Override
    public boolean equals(Object other) {
        return this.departmentId == ((Department)other).departmentId;
    }


}
