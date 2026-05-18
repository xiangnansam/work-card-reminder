package com.example.workcardreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public final class AlarmScheduler {
    private static final String TAG = "AlarmScheduler";
    private static final int REMINDER_REQUEST_CODE = 3001;
    private static final int REMINDER_SHOW_REQUEST_CODE = 3002;
    static final String ACTION_REMINDER_ALARM = "com.example.workcardreminder.REMINDER_ALARM";

    private AlarmScheduler() {
    }

    public static void scheduleNextDailyReminder(Context context) {
        scheduleReminderAt(context, nextReminderTimeMillis(context));
    }

    private static void scheduleReminderAt(Context context, long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        cancelLegacyBroadcastAlarm(context, alarmManager);
        cancelLegacyActivityAlarm(context, alarmManager);

        PendingIntent pendingIntent = createAlarmActivityPendingIntent(context, REMINDER_REQUEST_CODE);
        PendingIntent showIntent = createShowPendingIntent(context);
        Log.i(TAG, "Scheduling next reminder at " + triggerAtMillis);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                    triggerAtMillis,
                    showIntent
            );
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    private static PendingIntent createShowPendingIntent(Context context) {
        return createAlarmActivityPendingIntent(context, REMINDER_SHOW_REQUEST_CODE);
    }

    private static PendingIntent createAlarmActivityPendingIntent(Context context, int requestCode) {
        Intent intent = new Intent(context, ReminderActivity.class);
        intent.setAction(ACTION_REMINDER_ALARM);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }

    private static void cancelLegacyBroadcastAlarm(Context context, AlarmManager alarmManager) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent legacyIntent = PendingIntent.getBroadcast(context, REMINDER_REQUEST_CODE, intent, flags);
        if (legacyIntent != null) {
            alarmManager.cancel(legacyIntent);
            legacyIntent.cancel();
        }
    }

    private static void cancelLegacyActivityAlarm(Context context, AlarmManager alarmManager) {
        Intent intent = new Intent(context, ReminderActivity.class);
        intent.setAction(ACTION_REMINDER_ALARM);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent legacyIntent = PendingIntent.getActivity(context, REMINDER_SHOW_REQUEST_CODE, intent, flags);
        if (legacyIntent != null) {
            alarmManager.cancel(legacyIntent);
            legacyIntent.cancel();
        }
    }

    private static long nextReminderTimeMillis(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, ReminderSettings.getHour(context));
        calendar.set(Calendar.MINUTE, ReminderSettings.getMinute(context));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        for (int i = 0; i < 370; i++) {
            if (HolidayCalendar.isWorkday(context, calendar)) {
                break;
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return calendar.getTimeInMillis();
    }
}
