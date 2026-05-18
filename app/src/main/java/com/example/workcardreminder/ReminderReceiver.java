package com.example.workcardreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Reminder alarm received");
        AlarmScheduler.scheduleNextDailyReminder(context);
        ReminderNotifier.show(context);
        openReminderPopup(context);
    }

    private void openReminderPopup(Context context) {
        try {
            Intent popupIntent = new Intent(context, ReminderActivity.class);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(popupIntent);
            Log.i(TAG, "Reminder popup activity requested");
        } catch (RuntimeException exception) {
            Log.e(TAG, "Could not open reminder popup activity", exception);
        }
    }
}
