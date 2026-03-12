package com.onlystudents.common.utils;

import java.math.BigDecimal;

public class TypeConvertUtils {

    public static Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }

    public static Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Float) return ((Float) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof Float) return BigDecimal.valueOf((Float) value);
        if (value instanceof Number) return new BigDecimal(((Number) value).doubleValue());
        if (value instanceof String) return new BigDecimal((String) value);
        return BigDecimal.ZERO;
    }
}
