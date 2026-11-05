package com.moez.focusflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Persistent private history of actual analyses; no fake demonstration history. */
public final class HistoryActivity extends Activity {
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_history);
        AppUi.systemBars(this,false);
        AppUi.bindNav(this,3);
        findViewById(R.id.goBack).setOnClickListener(v -> finish());
        findViewById(R.id.clearHistory).setOnClickListener(v -> confirmClear());
        findViewById(R.id.newAnalysis).setOnClickListener(v -> startActivity(new Intent(this,AnalyzerActivity.class)));
    }
    private void confirmClear() {
        if(SessionStore.all(this).isEmpty())return;
        new AlertDialog.Builder(this).setTitle("Clear all sessions?")
            .setMessage("This removes your saved focus analyses from this device. It cannot be undone.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Clear",(d,w)->{SessionStore.clear(this);render();}).show();
    }
    @Override protected void onResume(){ super.onResume();render(); }
    private void render() {
        List<SessionStore.Session> entries=SessionStore.all(this);
        AppUi.text(this,R.id.historyCount,entries.size()+" saved sessions");
        AppUi.text(this,R.id.historyAvg,entries.isEmpty()?"—":SessionStore.average(entries)+"%");
        ((TrendChartView)findViewById(R.id.historyTrend)).setHistory(entries);
        findViewById(R.id.historyEmpty).setVisibility(entries.isEmpty()?View.VISIBLE:View.GONE);
        findViewById(R.id.clearHistory).setVisibility(entries.isEmpty()?View.GONE:View.VISIBLE);
        LinearLayout list=findViewById(R.id.historyList);
        list.removeAllViews();
        SimpleDateFormat format=new SimpleDateFormat("EEE, MMM d  ·  h:mm a",Locale.getDefault());
        for(SessionStore.Session session:entries) {
            View item=getLayoutInflater().inflate(R.layout.item_session,list,false);
            TextView score=item.findViewById(R.id.itemScore);
            TextView date=item.findViewById(R.id.itemDate);
            TextView details=item.findViewById(R.id.itemDetails);
            score.setText(session.score+"%");
            date.setText(format.format(new Date(session.timestamp)));
            details.setText(session.minutes+" min study · "+session.interruptions+" interruptions · "+session.energy+"/5 energy");
            item.setOnClickListener(v -> {
                Intent intent=new Intent(this,ResultActivity.class);
                intent.putExtra("score",session.score);
                intent.putExtra("minutes",session.minutes);
                intent.putExtra("interruptions",session.interruptions);
                intent.putExtra("breaks",session.breaks);
                intent.putExtra("energy",session.energy);
                startActivity(intent);
            });
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            p.bottomMargin=(int)(12*getResources().getDisplayMetrics().density);
            list.addView(item,p);
        }
    }
}
