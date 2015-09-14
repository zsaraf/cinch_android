package com.seshtutoring.seshapp.view.components;

/**
 * Created by franzwarning on 9/11/15.
 */
public class SettingsMenuItem {

    public static final int HEADER_TYPE = 0;
    public static final int ROW_TYPE = 1;
    public static final int EXPLAIN_TYPE = 2;

    public String text;
    public int type;
    public String rightText;

    public SettingsMenuItem(String text, int type, String rightText) {
        this.text = text;
        this.type = type;
        this.rightText = rightText;

    }
}