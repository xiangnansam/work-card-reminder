package com.example.workcardreminder;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Locale;

public final class HolidayCalendar {
    private static final String PREFS_NAME = "holiday_calendar";
    private static final String KEY_LAST_SYNC = "last_sync";

    private HolidayCalendar() {
    }

    public static boolean isWorkday(Context context, Calendar calendar) {
        int storedType = prefs(context).getInt(dateKey(calendar), -1);
        if (storedType == 0 || storedType == 3) {
            return true;
        }
        if (storedType == 1 || storedType == 2) {
            return false;
        }

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }

    public static void saveDayType(Context context, String date, int type) {
        prefs(context).edit().putInt(date, type).apply();
    }

    public static void saveLastSync(Context context, String text) {
        prefs(context).edit().putString(KEY_LAST_SYNC, text).apply();
    }

    public static String getLastSync(Context context) {
        return prefs(context).getString(KEY_LAST_SYNC, context.getString(R.string.never_synced));
    }

    private static String dateKey(Calendar calendar) {
        return String.format(
                Locale.US,
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
