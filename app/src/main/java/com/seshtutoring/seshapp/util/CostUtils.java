package com.seshtutoring.seshapp.util;

import java.math.BigDecimal;

/**
 * Created by nadavhollander on 7/23/15.
 */
public class CostUtils {
    public static float calculateCostForDuration(int hours, int minutes, float hourlyRate) {
        float result = (hours * hourlyRate) + (minutes * hourlyRate / 60);
        return round(result, 2);
    }

    public static String floatToString(float d, int numDecimals) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(numDecimals, BigDecimal.ROUND_HALF_UP);
        return bd.toString();
    }

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
