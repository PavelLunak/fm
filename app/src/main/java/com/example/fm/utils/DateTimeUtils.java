package com.example.fm.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateTimeUtils {
    public static String getParsedDateFromApi(String value) {

        Date tempDate = parseStringDate(value);

        if (tempDate != null) {
            String parsedDate =  getDate(tempDate);
            return parsedDate;
        } else {
            return "null";
        }
    }

    public static String getParsedDateAndTimeFromApi(String value) {

        Date tempDate = parseStringDate(value);

        if (tempDate != null) {
            String parsedDate =  getDate(tempDate) + ", " + getTime(tempDate);
            return parsedDate;
        } else {
            return "null";
        }
    }

    //2017-03-01T14:50:39
    public static String dateTimeToString(Date date) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf1.format(date);
    }

    //Čas data nastaví na maximální hodnotu - 23:59:59 a vrátí jako řetězec
    public static String dateTimeToStringWithMaxTime(Date date) {
        Calendar maximizedCalendar = Calendar.getInstance();
        maximizedCalendar.setTime(date);
        maximizedCalendar.set(Calendar.HOUR_OF_DAY, 23);
        maximizedCalendar.set(Calendar.MINUTE, 59);
        maximizedCalendar.set(Calendar.SECOND, 59);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf1.format(maximizedCalendar.getTime());
    }

    //Čas data nastaví na minimální hodnotu - 00:00:00 a vrátí jako řetězec
    public static String dateTimeToStringWithMinTime(Date date) {
        Calendar minimizeddCalendar = Calendar.getInstance();
        minimizeddCalendar.setTime(date);
        minimizeddCalendar.set(Calendar.HOUR_OF_DAY, 0);
        minimizeddCalendar.set(Calendar.MINUTE, 0);
        minimizeddCalendar.set(Calendar.SECOND, 0);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf1.format(minimizeddCalendar.getTime());
    }

    //Čas data nastaví na maximální hodnotu - 23:59:59
    public static long dateWithMaxTime(Date date) {
        Calendar maximizedCalendar = Calendar.getInstance();
        maximizedCalendar.setTime(date);
        maximizedCalendar.set(Calendar.HOUR_OF_DAY, 23);
        maximizedCalendar.set(Calendar.MINUTE, 59);
        maximizedCalendar.set(Calendar.SECOND, 59);
        return maximizedCalendar.getTime().getTime();
    }

    //Čas data nastaví na minimální hodnotu - 00:00:00
    public static long dateWithMinTime(Date date) {
        Calendar minimizeddCalendar = Calendar.getInstance();
        minimizeddCalendar.setTime(date);
        minimizeddCalendar.set(Calendar.HOUR_OF_DAY, 0);
        minimizeddCalendar.set(Calendar.MINUTE, 0);
        minimizeddCalendar.set(Calendar.SECOND, 0);
        return minimizeddCalendar.getTime().getTime();
    }

    public static String getDate(Date date) {
        if (date == null) {
            return "???";
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.M. y");
        return sdf1.format(date);
    }

    public static String getDate(long date) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.M. y");
        return sdf1.format(new Date(date));
    }

    public static String getDateTime(Date date) {
        if (date == null) {
            return "???";
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.M.y (HH:mm:ss)");
        return sdf1.format(date);
    }

    public static String getDateTime(long date) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.M.y (HH:mm:ss)");
        return sdf1.format(new Date(date));
    }

    public static String getDateTimeForLog(long date) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy_MM_dd");
        return sdf1.format(new Date(date));
    }

    public static String getTime(Date date) {
        if (date == null) {
            return "???";
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
        return sdf1.format(date);
    }

    public static String getTimeZone(Date date) {
        if (date == null) {
            return "???";
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("ZZZZZ");
        return sdf1.format(date);
    }

    public static Date parseStringDate(String date) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date parsedDate =  sdf1.parse(date);
            return parsedDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long getTimeDistance(Date date1, Date date2) {
        if (date1 == null || date2 == null) return -1;
        return date1.getTime() - date2.getTime();
    }

    public static long getTimeDistance(long date1, long date2) {
        if (date1 == 0 || date2 == 0) return -1;
        return date1 - date2;
    }

    public static String getTimeDistanceString(long distance) {
        int hours = (int) distance / 1000 / 60 / 60;
        int minutes = (int) (distance - (hours * 1000 * 60 * 60)) / 1000 / 60;
        int seconds = (int) ((distance - (hours * 1000 * 60 * 60)) / 1000 / 60) / 60;
        return "" + addZero(hours) + ":" + addZero(minutes);
    }

    public static String addZero(int value) {
        String toReturn = "" + value;
        if (toReturn.length() == 1) {
            toReturn = "0" + toReturn;
        }
        return toReturn;
    }
}
