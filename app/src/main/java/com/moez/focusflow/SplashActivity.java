package com.moez.focusflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/** Short branded launch screen; does not block model initialization or require network. */
public final class SplashActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navigate = () -> {
        if (!isFinishing() && !isDestroyed()) {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    };
    @Override protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_splash);
        getWindow().setStatusBarColor(0xFF0B1630);
        getWindow().setNavigationBarColor(0xFF0B1630);
        View mark = findViewById(R.id.splashMark);
        mark.setAlpha(0f); mark.setScaleX(.78f); mark.setScaleY(.78f);
        mark.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(650).start();
        handler.postDelayed(navigate, 1250);
    }
    @Override protected void onDestroy() {
        handler.removeCallbacks(navigate);
        super.onDestroy();
    }
}
