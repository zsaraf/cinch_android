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

    public static float calculateAdditionalStudentCharge(int hours, int minutes, int numStudents,
                                                         float additionalStudentCharge) {
        float result = (float) (hours + (minutes / 60.0)) * (numStudents - 1) * additionalStudentCharge;
        return round(result, 2);
    }

    public static float calculateEstimatedTotal(float totalCost, float additionalStudentsCharge, float discount, float creditsApplied) {
        return Math.max(0f, totalCost + additionalStudentsCharge - discount - creditsApplied);
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
