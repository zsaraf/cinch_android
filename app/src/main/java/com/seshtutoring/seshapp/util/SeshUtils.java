package com.seshtutoring.seshapp.util;

/**
 * Created by zacharysaraf on 9/13/15.
 */
public class SeshUtils {

    public static String abbreviatedNameForName(String name) {
        String[] components = name.split("\\s+");
        String capitalizedFirstName = components[0].substring(0,1).toUpperCase() + components[0].substring(1).toLowerCase();
        if (components.length > 1) {
            return capitalizedFirstName + " " + components[components.length-1].substring(0,1).toUpperCase() + ".";
        } else {
            return capitalizedFirstName;
        }
    }

    public static String firstName(String name) {
        String[] components = name.split("\\s+");
        return components[0].substring(0,1).toUpperCase() + components[0].substring(1).toLowerCase();
    }

}
