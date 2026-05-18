package com.example.workcardreminder;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1001;
    private TextView statusText;
    private Button timeButton;
    private Button syncButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReminderNotifier.ensureChannel(this);
        ReminderNotifier.cancelLegacyStatus(this);
        requestNotificationPermissionIfNeeded();
        AlarmScheduler.scheduleNextDailyReminder(this);
        setContentView(createContentView());
        refreshStatus();
        if (getString(R.string.never_synced).equals(HolidayCalendar.getLastSync(this))) {
            syncHolidays(false);
        }
    }

    private View createContentView() {
        int padding = getResources().getDimensionPixelSize(R.dimen.screen_padding);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(padding, padding, padding, padding);
        layout.setBackgroundColor(0xFFF7F3EA);

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextColor(0xFF1F2933);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        statusText = subtitle;
        subtitle.setTextColor(0xFF52606D);
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, dp(16), 0, dp(28));

        timeButton = new Button(this);
        timeButton.setAllCaps(false);
        timeButton.setOnClickListener(view -> showTimePicker());

        syncButton = new Button(this);
        syncButton.setText(R.string.sync_holidays);
        syncButton.setAllCaps(false);
        syncButton.setOnClickListener(view -> syncHolidays(true));

        Button alarmButton = new Button(this);
        alarmButton.setText(R.string.exact_alarm_settings);
        alarmButton.setAllCaps(false);
        alarmButton.setOnClickListener(view -> openExactAlarmSettings());

        Button batteryButton = new Button(this);
        batteryButton.setText(R.string.battery_settings);
        batteryButton.setAllCaps(false);
        batteryButton.setOnClickListener(view -> openBatterySettings());

        Button fullScreenButton = new Button(this);
        fullScreenButton.setText(R.string.full_screen_settings);
        fullScreenButton.setAllCaps(false);
        fullScreenButton.setOnClickListener(view -> openFullScreenSettings());

        layout.addView(title);
        layout.addView(subtitle, subtitleParams);
        layout.addView(timeButton, buttonParams());
        layout.addView(syncButton, buttonParams());
        layout.addView(alarmButton, buttonParams());
        layout.addView(batteryButton, buttonParams());
        layout.addView(fullScreenButton, buttonParams());
        return layout;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        params.setMargins(0, dp(8), 0, 0);
        return params;
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    ReminderSettings.setReminderTime(this, hourOfDay, minute);
                    AlarmScheduler.scheduleNextDailyReminder(this);
                    refreshStatus();
                    Toast.makeText(this, R.string.time_saved, Toast.LENGTH_SHORT).show();
                },
                ReminderSettings.getHour(this),
                ReminderSettings.getMinute(this),
                true
        );
        dialog.show();
    }

    private void syncHolidays(boolean showToast) {
        syncButton.setEnabled(false);
        syncButton.setText(R.string.syncing_holidays);

        HolidaySyncer.syncCurrentAndNextYear(this, (success, message) -> {
            syncButton.setEnabled(true);
            syncButton.setText(R.string.sync_holidays);
            refreshStatus();
            if (showToast || success) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refreshStatus() {
        if (statusText == null || timeButton == null) {
            return;
        }

        String timeText = ReminderSettings.getTimeText(this);
        statusText.setText(getString(
                R.string.main_status,
                timeText,
                HolidayCalendar.getLastSync(this)
        ));
        timeButton.setText(getString(R.string.choose_time, timeText));
    }

    private void openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, R.string.exact_alarm_allowed, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_exact_alarm_needed, Toast.LENGTH_SHORT).show();
        }
    }

    private void openBatterySettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
        Toast.makeText(this, R.string.battery_hint, Toast.LENGTH_LONG).show();
    }

    private void openFullScreenSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
        Toast.makeText(this, R.string.full_screen_hint, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
