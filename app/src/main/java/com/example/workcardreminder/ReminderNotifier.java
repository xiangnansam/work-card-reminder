package com.example.workcardreminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public final class ReminderNotifier {
    private static final String TAG = "ReminderNotifier";
    private static final String CHANNEL_ID = "work_card_reminder";
    private static final String STATUS_CHANNEL_ID = "work_card_status";
    private static final int NOTIFICATION_ID = 4001;
    private static final int STATUS_NOTIFICATION_ID = 4002;

    private ReminderNotifier() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(context.getString(R.string.channel_description));
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);

            NotificationChannel statusChannel = new NotificationChannel(
                    STATUS_CHANNEL_ID,
                    context.getString(R.string.status_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            statusChannel.setDescription(context.getString(R.string.status_channel_description));
            statusChannel.setShowBadge(false);
            manager.createNotificationChannel(statusChannel);
        }
    }

    public static void showStatus(Context context) {
        ensureChannel(context);

        PendingIntent popupIntent = createPopupPendingIntent(context);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, STATUS_CHANNEL_ID)
                : new Notification.Builder(context);

        Notification notification = builder
                .setSmallIcon(R.drawable.ic_work_card)
                .setContentTitle(context.getString(R.string.status_title))
                .setContentText(context.getString(
                        R.string.status_text,
                        ReminderSettings.getTimeText(context)
                ))
                .setContentIntent(popupIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_LOW)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(STATUS_NOTIFICATION_ID, notification);
        }
    }

    public static void show(Context context) {
        Log.i(TAG, "Showing reminder notification");
        ensureChannel(context);
        vibrate(context);

        PendingIntent popupIntent = createPopupPendingIntent(context);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);

        Notification notification = builder
                .setSmallIcon(R.drawable.ic_work_card)
                .setContentTitle(context.getString(R.string.reminder_title))
                .setContentText(context.getString(R.string.reminder_question))
                .setStyle(new Notification.BigTextStyle()
                        .bigText(context.getString(R.string.reminder_question)))
                .setContentIntent(popupIntent)
                .setFullScreenIntent(popupIntent, true)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .setPriority(Notification.PRIORITY_MAX)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_ALARM)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    public static void cancel(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }
    }

    private static PendingIntent createPopupPendingIntent(Context context) {
        Intent intent = new Intent(context, ReminderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context, 5001, intent, flags);
    }

    private static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(600);
        }
    }
}
