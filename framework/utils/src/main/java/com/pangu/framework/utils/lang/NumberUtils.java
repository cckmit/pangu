package com.pangu.framework.utils.lang;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NumberUtils {

    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Type resultType, Number value) {
        if (resultType == null) {
            String msg = value.getClass().getSimpleName() + " -> NULL";
            throw new NullPointerException(msg);
        }
        if (resultType == Date.class) {
            return (T) new Date(value.longValue());
        } else if (resultType == int.class || resultType == Integer.class) {
            return (T) Integer.valueOf(value.intValue());
        } else if (resultType == double.class || resultType == Double.class) {
            return (T) Double.valueOf(value.doubleValue());
        } else if (resultType == boolean.class || resultType == Boolean.class) {
            return (T) Boolean.valueOf(value.intValue() == 0);
        } else if (resultType == byte.class || resultType == Byte.class) {
            return (T) Byte.valueOf(value.byteValue());
        } else if (resultType == long.class || resultType == Long.class) {
            return (T) Long.valueOf(value.longValue());
        } else if (resultType == short.class || resultType == Short.class) {
            return (T) Short.valueOf(value.shortValue());
        } else if (resultType == float.class || resultType == Float.class) {
            return (T) Float.valueOf(value.floatValue());
        } else if (resultType == Number.class) {
            return (T) value;
        } else if (resultType == AtomicInteger.class) {
            return (T) new AtomicInteger(value.intValue());
        } else if (resultType == AtomicLong.class) {
            return (T) new AtomicLong(value.longValue());
        } else {
            String msg = value.getClass().getSimpleName() + " -> " + resultType;
            throw new IllegalArgumentException(new ClassCastException(msg));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Type resultType, String value) {
        if (resultType == null) {
            String msg = value.getClass().getSimpleName() + " -> NULL";
            throw new NullPointerException(msg);
        }

        if (value.contains(".")) {
            Double dvalue = Double.valueOf(value);
            return valueOf(resultType, dvalue);
        }

        if (resultType == int.class || resultType == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (resultType == double.class || resultType == Double.class) {
            return (T) value;
        } else if (resultType == boolean.class || resultType == Boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (resultType == byte.class || resultType == Byte.class) {
            return (T) Byte.valueOf(value);
        } else if (resultType == long.class || resultType == Long.class) {
            return (T) Long.valueOf(value);
        } else if (resultType == short.class || resultType == Short.class) {
            return (T) Short.valueOf(value);
        } else if (resultType == float.class || resultType == Float.class) {
            return (T) Float.valueOf(value);
        } else if (resultType == Number.class) {
            return (T) value;
        } else {
            String msg = value.getClass().getSimpleName() + " -> " + resultType;
            throw new IllegalArgumentException(new ClassCastException(msg));
        }
    }

}
