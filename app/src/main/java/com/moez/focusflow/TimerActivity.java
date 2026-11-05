package com.moez.focusflow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

/** Practical focus/break timer independent of ML predictions; persists through screen changes. */
public final class TimerActivity extends Activity {
    private static final String PREFS="focusflow_timer";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private long totalMs=25*60*1000L;
    private long remainingMs=totalMs;
    private long deadlineMs=0;
    private boolean running=false;
    private final Runnable tick=new Runnable(){@Override public void run(){
        if(!running)return;
        remainingMs=Math.max(0,deadlineMs-System.currentTimeMillis());
        if(remainingMs==0){running=false;save();update();Toast.makeText(TimerActivity.this,"Session complete. Take a breath and reset when ready.",Toast.LENGTH_LONG).show();return;}
        update(); handler.postDelayed(this,500);
    }};
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_timer);
        AppUi.systemBars(this,false);
        AppUi.bindNav(this,2);
        findViewById(R.id.goBack).setOnClickListener(v -> finish());
        findViewById(R.id.preset25).setOnClickListener(v -> setPreset(25));
        findViewById(R.id.preset45).setOnClickListener(v -> setPreset(45));
        findViewById(R.id.preset5).setOnClickListener(v -> setPreset(5));
        findViewById(R.id.timerAction).setOnClickListener(v -> toggle());
        findViewById(R.id.timerReset).setOnClickListener(v -> reset());
        SharedPreferences sp=getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        totalMs=Math.max(60_000L,sp.getLong("total",25*60*1000L));
        remainingMs=sp.getLong("remaining",totalMs);
        deadlineMs=sp.getLong("deadline",0);
        running=sp.getBoolean("running",false);
        if(running)remainingMs=Math.max(0,deadlineMs-System.currentTimeMillis());
        if(remainingMs==0){running=false;remainingMs=totalMs;}
        update();
    }
    private void setPreset(int minutes){
        handler.removeCallbacks(tick);running=false;totalMs=minutes*60_000L;remainingMs=totalMs;deadlineMs=0;save();update();
    }
    private void toggle(){
        if(running){remainingMs=Math.max(0,deadlineMs-System.currentTimeMillis());running=false;handler.removeCallbacks(tick);}
        else {if(remainingMs<=0)remainingMs=totalMs;running=true;deadlineMs=System.currentTimeMillis()+remainingMs;handler.post(tick);}
        save();update();
    }
    private void reset(){handler.removeCallbacks(tick);running=false;remainingMs=totalMs;deadlineMs=0;save();update();}
    private void update(){
        long seconds=Math.max(0,(remainingMs+999)/1000);
        AppUi.text(this,R.id.clockDisplay,String.format(Locale.US,"%02d:%02d",seconds/60,seconds%60));
        AppUi.text(this,R.id.timerMode,totalMs==5*60_000L?"SHORT BREAK":"FOCUS SESSION");
        AppUi.text(this,R.id.timerStatus,running?"Your focus block is running":"A little progress goes a long way");
        AppUi.text(this,R.id.timerAction,running?"Pause timer":"Start timer");
        findViewById(R.id.preset25).setAlpha(totalMs==25*60_000L?1f:.48f);
        findViewById(R.id.preset45).setAlpha(totalMs==45*60_000L?1f:.48f);
        findViewById(R.id.preset5).setAlpha(totalMs==5*60_000L?1f:.48f);
        int progress=Math.round(100*(1f-remainingMs/(float)totalMs));
        if(!running && remainingMs==totalMs)progress=0;
        ((android.widget.ProgressBar)findViewById(R.id.timerProgress)).setProgress(Math.min(100,Math.max(0,progress)));
    }
    private void save(){getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
       .putLong("total",totalMs).putLong("remaining",remainingMs).putLong("deadline",deadlineMs).putBoolean("running",running).apply();}
    @Override protected void onResume(){super.onResume();if(running){handler.removeCallbacks(tick);handler.post(tick);}}
    @Override protected void onPause(){handler.removeCallbacks(tick);if(running)remainingMs=Math.max(0,deadlineMs-System.currentTimeMillis());save();super.onPause();}
    @Override protected void onDestroy(){handler.removeCallbacks(tick);super.onDestroy();}
}
