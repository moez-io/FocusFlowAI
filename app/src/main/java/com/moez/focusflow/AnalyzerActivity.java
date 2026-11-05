package com.moez.focusflow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

/** Validation + REAL on-device TFLite inference, not rule-based predictions. */
public final class AnalyzerActivity extends Activity {
    private EditText minutes, interruptions, breaks, energy;
    private FocusPredictor model;
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_analyzer);
        AppUi.systemBars(this, false);
        AppUi.bindNav(this,1);
        minutes = findViewById(R.id.inputMinutes);
        interruptions = findViewById(R.id.inputInterruptions);
        breaks = findViewById(R.id.inputBreaks);
        energy = findViewById(R.id.inputEnergy);
        findViewById(R.id.goBack).setOnClickListener(v -> finish());
        findViewById(R.id.fillFocused).setOnClickListener(v -> sample(75,1,12,4));
        findViewById(R.id.fillDistracted).setOnClickListener(v -> sample(35,10,0,2));
        findViewById(R.id.clearInputs).setOnClickListener(v -> {minutes.setText("");interruptions.setText("");breaks.setText("");energy.setText("");});
        android.view.View analyzeButton = findViewById(R.id.runModel);
        analyzeButton.setEnabled(false);
        try {
            model = new FocusPredictor(this);
            AppUi.text(this,R.id.modelLabel,"AI ENGINE READY  •  ON-DEVICE");
            analyzeButton.setEnabled(true);
        } catch (Exception e) {
            AppUi.text(this,R.id.modelLabel,"MODEL LOAD FAILED — CHECK INSTALLATION");
            Toast.makeText(this,"Model error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        analyzeButton.setOnClickListener(v -> analyze());
    }
    private void sample(int m,int i,int b,int e) {
        minutes.setText(String.valueOf(m)); interruptions.setText(String.valueOf(i));
        breaks.setText(String.valueOf(b)); energy.setText(String.valueOf(e));
        InputMethodManager keyboard=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(keyboard!=null) keyboard.hideSoftInputFromWindow(minutes.getWindowToken(),0);
    }
    private Integer read(EditText field,int lo,int hi) {
        String raw=field.getText().toString().trim();
        if(raw.isEmpty()) {field.setError("This field is required");field.requestFocus();return null;}
        try { int n=Integer.parseInt(raw);if(n>=lo && n<=hi) return n; }
        catch(NumberFormatException ignored) {}
        field.setError("Enter a whole number from "+lo+" to "+hi);
        field.requestFocus();return null;
    }
    private void analyze() {
        Integer m=read(minutes,10,120);if(m==null)return;
        Integer i=read(interruptions,0,15);if(i==null)return;
        Integer b=read(breaks,0,25);if(b==null)return;
        Integer e=read(energy,1,5);if(e==null)return;
        if(model==null) { Toast.makeText(this,"AI model is unavailable",Toast.LENGTH_SHORT).show();return; }
        try {
            FocusPredictor.Prediction prediction=model.predict(m,i,b,e);
            int score=Math.max(0,Math.min(100,Math.round(prediction.balancedScore*100f)));
            SessionStore.add(this,m,i,b,e,score);
            Intent intent=new Intent(this, ResultActivity.class);
            intent.putExtra("minutes",m);
            intent.putExtra("interruptions",i);
            intent.putExtra("breaks",b);
            intent.putExtra("energy",e);
            intent.putExtra("score",score);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        } catch(Exception ex) {Toast.makeText(this,"Unable to run offline model: "+ex.getMessage(),Toast.LENGTH_LONG).show();}
    }
    @Override protected void onDestroy() {
        if(model!=null){model.close();model=null;}
        super.onDestroy();
    }
}
