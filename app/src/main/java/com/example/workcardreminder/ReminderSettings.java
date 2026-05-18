package com.example.workcardreminder;

import android.content.Context;
import android.content.SharedPreferences;

public final class ReminderSettings {
    private static final String PREFS_NAME = "reminder_settings";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";
    private static final int DEFAULT_HOUR = 8;
    private static final int DEFAULT_MINUTE = 20;

    private ReminderSettings() {
    }

    public static int getHour(Context context) {
        return prefs(context).getInt(KEY_HOUR, DEFAULT_HOUR);
    }

    public static int getMinute(Context context) {
        return prefs(context).getInt(KEY_MINUTE, DEFAULT_MINUTE);
    }

    public static void setReminderTime(Context context, int hour, int minute) {
        prefs(context)
                .edit()
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .apply();
    }

    public static String getTimeText(Context context) {
        return String.format("%02d:%02d", getHour(context), getMinute(context));
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
