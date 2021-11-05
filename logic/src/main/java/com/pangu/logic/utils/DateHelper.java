package com.pangu.logic.utils;

import java.util.Date;
public abstract class DateHelper {

    /**
     * 获取最大日期
     */
    public static Date max(Date... dates) {
        Date result = null;
        for (Date d : dates) {
            if (d == null) {
                continue;
            }
            if (result == null || result.before(d)) {
                result = d;
            }
        }
        return result;
    }

    /**
     * 获取最小日期
     */
    public static Date mix(Date... dates) {
        Date result = null;
        for (Date d : dates) {
            if (d == null) {
                continue;
            }
            if (result == null || result.after(d)) {
                result = d;
            }
        }
        return result;
    }
}
