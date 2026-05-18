package com.example.workcardreminder;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReminderActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AlarmScheduler.ACTION_REMINDER_ALARM.equals(getIntent().getAction())) {
            ReminderNotifier.show(this);
        }
        AlarmScheduler.scheduleNextDailyReminder(this);
        setContentView(createContentView());
    }

    private View createContentView() {
        int padding = dp(24);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(padding, padding, padding, padding);
        layout.setBackgroundResource(R.drawable.dialog_background);

        TextView question = new TextView(this);
        question.setText(R.string.reminder_question);
        question.setTextColor(0xFF1F2933);
        question.setTextSize(22);
        question.setGravity(Gravity.CENTER);

        Button doneButton = new Button(this);
        doneButton.setText(R.string.card_ready);
        doneButton.setAllCaps(false);
        doneButton.setOnClickListener(view -> {
            ReminderNotifier.cancel(this);
            finish();
        });

        LinearLayout.LayoutParams questionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        questionParams.setMargins(0, 0, 0, dp(20));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        );

        layout.addView(question, questionParams);
        layout.addView(doneButton, buttonParams);
        return layout;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
