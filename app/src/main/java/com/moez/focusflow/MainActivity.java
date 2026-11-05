package com.moez.focusflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Dashboard with real local-history summaries and a no-sample-data trend chart. */
public final class MainActivity extends Activity {
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_main);
        AppUi.systemBars(this, true);
        AppUi.bindNav(this, 0);
        findViewById(R.id.startAnalysis).setOnClickListener(v -> startActivity(new Intent(this, AnalyzerActivity.class)));
        findViewById(R.id.openTimer).setOnClickListener(v -> startActivity(new Intent(this, TimerActivity.class)));
        findViewById(R.id.openHistory).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
    }
    @Override protected void onResume() {
        super.onResume();
        List<SessionStore.Session> history = SessionStore.all(this);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "GOOD MORNING" : (hour < 17 ? "GOOD AFTERNOON" : "GOOD EVENING");
        AppUi.text(this, R.id.greeting, greeting);
        AppUi.text(this, R.id.statCount, String.valueOf(history.size()));
        AppUi.text(this, R.id.statAverage, history.isEmpty()?"—":SessionStore.average(history)+"%");
        AppUi.text(this, R.id.statBest, history.isEmpty()?"—":SessionStore.best(history)+"%");
        ((TrendChartView)findViewById(R.id.homeTrend)).setHistory(history);
        View recent = findViewById(R.id.recentData);
        if (history.isEmpty()) {
            recent.setVisibility(View.GONE);
            findViewById(R.id.emptyRecent).setVisibility(View.VISIBLE);
        } else {
            recent.setVisibility(View.VISIBLE);
            findViewById(R.id.emptyRecent).setVisibility(View.GONE);
            SessionStore.Session last = history.get(0);
            AppUi.text(this,R.id.recentScore,last.score+"% match");
            AppUi.text(this,R.id.recentWhen,new SimpleDateFormat("MMM d · h:mm a",Locale.getDefault()).format(new Date(last.timestamp)));
            AppUi.text(this,R.id.recentSummary,last.minutes+" min study  •  "+last.interruptions+" interruptions");
        }
    }
}
