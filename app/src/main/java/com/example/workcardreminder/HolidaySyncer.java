package com.example.workcardreminder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class HolidaySyncer {
    private static final String TAG = "HolidaySyncer";

    public interface Callback {
        void onResult(boolean success, String message);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private HolidaySyncer() {
    }

    public static void syncCurrentAndNextYear(Context context, Callback callback) {
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            try {
                Calendar now = Calendar.getInstance();
                int currentYear = now.get(Calendar.YEAR);
                syncYear(appContext, currentYear);
                syncYear(appContext, currentYear + 1);

                String syncText = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                        .format(now.getTime());
                HolidayCalendar.saveLastSync(appContext, syncText);
                AlarmScheduler.scheduleNextDailyReminder(appContext);
                postResult(callback, true, appContext.getString(R.string.sync_success, syncText));
            } catch (Exception exception) {
                Log.e(TAG, "Holiday sync failed", exception);
                postResult(callback, false, appContext.getString(
                        R.string.sync_failed_with_reason,
                        exception.getClass().getSimpleName()
                ));
            }
        });
    }

    private static void syncYear(Context context, int year) throws Exception {
        URL url = new URL("https://timor.tech/api/holiday/year/" + year + "?type=Y&week=Y");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "WorkCardReminder/1.0");

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IllegalStateException("HTTP " + responseCode);
            }

            String response = readResponse(connection);
            JSONObject root = new JSONObject(response);
            if (root.optInt("code", -1) != 0) {
                throw new IllegalStateException("Holiday API returned an error");
            }

            JSONObject types = root.optJSONObject("type");
            if (types == null) {
                return;
            }

            Iterator<String> keys = types.keys();
            while (keys.hasNext()) {
                String date = keys.next();
                JSONObject value = types.optJSONObject(date);
                if (value != null) {
                    HolidayCalendar.saveDayType(context, date, value.optInt("type", -1));
                }
            }
        } finally {
            connection.disconnect();
        }
    }

    private static String readResponse(HttpURLConnection connection) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream(),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static void postResult(Callback callback, boolean success, String message) {
        MAIN_HANDLER.post(() -> callback.onResult(success, message));
    }
}
