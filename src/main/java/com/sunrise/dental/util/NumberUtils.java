package com.sunrise.dental.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money formatting / rounding helpers.
 */
public final class NumberUtils {

    private NumberUtils() {
    }

    public static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static String formatCurrency(double value) {
        return String.format("%,.2f", round(value));
    }
}
