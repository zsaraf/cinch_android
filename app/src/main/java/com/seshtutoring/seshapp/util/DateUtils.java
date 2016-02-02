package com.seshtutoring.seshapp.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zacharysaraf on 8/26/15.
 */
public class DateUtils {

    public static String getSeshFormattedDate(DateTime date) {

        String day = getSeshFormattedDayString(date);
        String time = getSeshFormattedTimeString(date);

        return String.format("%s @ %s", day, time);
    }

    public static String getSeshFormattedTimeString(DateTime date) {
        DateFormat hourMinute = new SimpleDateFormat("hh:mm");
        int hour = date.getHourOfDay();
        int minute = date.getMinuteOfHour();
        String suffix = null;
        if (hour >= 12) {
            hour -= 12;
            suffix = "p";
        } else {
            suffix = "a";
        }
        if (hour == 0) {
            hour = 12;
        }
        String timeString = hour + "";
        if (minute != 0) {
            timeString += ":" + minute;
        }
        timeString += suffix;
        return timeString;
    }

    public static String getSeshFormattedDayString(DateTime date) {
        if (date.toLocalDate().equals(new LocalDate())) {
            return "TODAY";
        } else if (date.minusDays(1).equals(new LocalDate())) {
            return "TMRW";
        } else {
            int dayOfWeek = date.getDayOfWeek();
            switch (dayOfWeek) {
                case DateTimeConstants.SUNDAY:
                    return "SUN";
                case DateTimeConstants.MONDAY:
                    return "MON";
                case DateTimeConstants.TUESDAY:
                    return "TUES";
                case DateTimeConstants.WEDNESDAY:
                    return "WED";
                case DateTimeConstants.THURSDAY:
                    return "THURS";
                case DateTimeConstants.FRIDAY:
                    return "FRI";
                case DateTimeConstants.SATURDAY:
                    return "SAT";
            }
        }
        return "";
    }

    public static List<String> getSeshFormattedDaysOfTheWeek(DateTime startDay) {
        List<DateTime> daysOfWeek = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            daysOfWeek.add(startDay.plusDays(i));
        }

        List<String> formattedDays = new ArrayList<>();
        for (DateTime day : daysOfWeek) {
            formattedDays.add(getSeshFormattedDayString(day));
        }

        return formattedDays;
    }

    public static Date formattedTime(String rawTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
        return formatter.parseDateTime(rawTimeString).toDate();
    }

    public static Date djangoFormattedTime(String rawTimeString) {
        int indexOfPeriod = rawTimeString.lastIndexOf('.');
        if (indexOfPeriod != -1) {
            rawTimeString = rawTimeString.substring(0, indexOfPeriod);
        }
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss").withZoneUTC();
            return formatter.parseDateTime(rawTimeString).toDate();
        } catch (IllegalArgumentException ex) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss.SSS").withZoneUTC();
            return formatter.parseDateTime(rawTimeString).toDate();
        }
    }


}
