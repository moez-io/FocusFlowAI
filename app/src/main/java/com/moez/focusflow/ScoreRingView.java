package com.moez.focusflow;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Accessible semicircular visual indicator for a model's educational match score. */
public final class ScoreRingView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float displayed;
    private ValueAnimator animation;
    public ScoreRingView(Context context, AttributeSet attrs) { super(context, attrs); }
    private float d(float dp) { return dp * getResources().getDisplayMetrics().density; }
    public void setScore(int percent) {
        percent = Math.max(0, Math.min(100, percent));
        if (animation != null) animation.cancel();
        animation = ValueAnimator.ofFloat(0f, percent);
        animation.setDuration(900);
        animation.addUpdateListener(a -> { displayed = (float)a.getAnimatedValue(); invalidate(); });
        animation.start();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        float margin = d(20), stroke = d(14);
        float cx = getWidth()/2f, cy = getHeight()/2f;
        float r = size/2f - margin;
        RectF b = new RectF(cx-r, cy-r, cx+r, cy+r);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeWidth(stroke);
        paint.setColor(0xFFE7E8FA); canvas.drawArc(b,-90,360,false,paint);
        paint.setColor(0xFF6A57ED); canvas.drawArc(b,-90,displayed*3.6f,false,paint);
    }
    @Override protected void onDetachedFromWindow() {
        if (animation != null) animation.cancel();
        super.onDetachedFromWindow();
    }
}
