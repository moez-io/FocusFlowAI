package com.moez.focusflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.util.Locale;

/** Plain-language interpretation; tips are deterministic, score is genuine model output. */
public final class ResultActivity extends Activity {
    private int score, minutes, interruptions, breaks, energy;
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_result);
        AppUi.systemBars(this,false);
        score=getIntent().getIntExtra("score",0);
        minutes=getIntent().getIntExtra("minutes",0);
        interruptions=getIntent().getIntExtra("interruptions",0);
        breaks=getIntent().getIntExtra("breaks",0);
        energy=getIntent().getIntExtra("energy",0);
        AppUi.text(this,R.id.scoreNumber,String.valueOf(score));
        AppUi.text(this,R.id.resDuration,minutes+" min");
        AppUi.text(this,R.id.resInterruptions,String.valueOf(interruptions));
        AppUi.text(this,R.id.resBreaks,breaks+" min");
        AppUi.text(this,R.id.resEnergy,energy+" / 5");
        String title,detail;
        if(score>=65) {
            title="A balanced study pattern";
            detail="Your entries resemble the focused examples in this educational AI model.";
        } else if(score<=35) {
            title="Room to improve";
            detail="Your entries resemble the less focused examples in the model's synthetic dataset.";
        } else {
            title="A mixed focus pattern";
            detail="Your session shares features with both groups of synthetic training examples.";
        }
        AppUi.text(this,R.id.resTitle,title);
        AppUi.text(this,R.id.resDescription,detail);
        String first = interruptions >= 6 ? "Try keeping your phone out of reach; frequent interruptions can fragment a study block." :
                "You recorded relatively few interruptions; protecting that quiet time is a useful habit.";
        String second = energy <= 2 ? "Your energy rating was low. Consider a shorter session or a more suitable time of day." :
                "Your energy rating was moderate to high; keep water nearby and plan regular rest.";
        String third = breaks == 0 && minutes >= 45 ? "Add a short planned break to longer sessions instead of studying continuously." :
                "Review your break length and adjust it based on how comfortable you feel.";
        AppUi.text(this,R.id.tipOne,first);
        AppUi.text(this,R.id.tipTwo,second);
        AppUi.text(this,R.id.tipThree,third);
        findViewById(R.id.goBack).setOnClickListener(v -> finish());
        findViewById(R.id.analyzeAgain).setOnClickListener(v -> {startActivity(new Intent(this,AnalyzerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish();});
        findViewById(R.id.viewHistory).setOnClickListener(v -> startActivity(new Intent(this,HistoryActivity.class)));
        findViewById(R.id.shareResult).setOnClickListener(v -> share());
        // Start the animated ring after view layout has finished.
        findViewById(R.id.resultRing).post(() -> ((ScoreRingView)findViewById(R.id.resultRing)).setScore(score));
    }
    private void share() {
        String message=String.format(Locale.US,"FocusFlow AI — Study session\nDuration: %d min\nInterruptions: %d\nBreaks: %d min\nEnergy: %d/5\nEducational AI pattern-match score: %d/100\nNote: based on synthetic training data.",minutes,interruptions,breaks,energy,score);
        Intent send=new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,message);
        startActivity(Intent.createChooser(send,"Share your study summary"));
    }
}
