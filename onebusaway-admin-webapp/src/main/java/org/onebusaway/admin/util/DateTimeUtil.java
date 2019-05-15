package org.onebusaway.admin.util;

import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {


    public static Date getStartOfDay(Date serviceDate) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(serviceDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 000);
        return cal.getTime();
    }

    public static Date getEndOfDay(Date serviceDate) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(serviceDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static Date getStartOfServiceDay(Date serviceDate) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(serviceDate);

        if(cal.get(Calendar.HOUR_OF_DAY) < 3) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            cal.set(Calendar.HOUR_OF_DAY, 3);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 3);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal.getTime();
    }

    public static Date getEndOfServiceDay(Date serviceDate) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(serviceDate);

        if(cal.get(Calendar.HOUR_OF_DAY) < 3) {
            cal.set(Calendar.HOUR_OF_DAY, 2);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 26);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        }
        return cal.getTime();
    }
}
