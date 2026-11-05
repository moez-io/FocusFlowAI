package com.moez.focusflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/** A true visualization of saved scores; never inserts sample history. */
public final class TrendChartView extends View {
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Integer> scores = new ArrayList<>();
    public TrendChartView(Context context, AttributeSet attrs) { super(context, attrs); }
    public void setHistory(List<SessionStore.Session> history) {
        scores.clear();
        int count = Math.min(7, history.size());
        for (int i = count - 1; i >= 0; i--) scores.add(history.get(i).score);
        invalidate();
    }
    private float d(float dp) { return dp * getResources().getDisplayMetrics().density; }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = d(37), right = getWidth() - d(15);
        float top = d(16), bottom = getHeight() - d(30);
        if (right <= left || bottom <= top) return;
        label.setTextSize(d(10));
        label.setColor(Color.rgb(152, 162, 181));
        for (int level = 0; level <= 100; level += 50) {
            float y = bottom - (bottom - top) * level / 100f;
            line.setStyle(Paint.Style.STROKE); line.setStrokeWidth(d(1));
            line.setColor(Color.rgb(232, 236, 244));
            canvas.drawLine(left, y, right, y, line);
            canvas.drawText(String.valueOf(level), d(7), y + d(4), label);
        }
        if (scores.isEmpty()) {
            label.setTextSize(d(13)); label.setColor(Color.rgb(140, 151, 174));
            String message = "Your focus trend appears here";
            canvas.drawText(message, left + d(4), (top + bottom) / 2f, label);
            return;
        }
        Path path = new Path();
        Path area = new Path();
        for (int i = 0; i < scores.size(); i++) {
            float x = scores.size() == 1 ? (left + right) / 2f : left + (right - left) * i / (scores.size() - 1f);
            float y = bottom - (bottom - top) * scores.get(i) / 100f;
            if (i == 0) { path.moveTo(x, y); area.moveTo(x, bottom); area.lineTo(x, y); }
            else { path.lineTo(x, y); area.lineTo(x, y); }
            if (i == scores.size() - 1) { area.lineTo(x, bottom); area.close(); }
        }
        line.setStyle(Paint.Style.FILL);
        line.setShader(new LinearGradient(0, top, 0, bottom, 0x386A57ED, 0x006A57ED, Shader.TileMode.CLAMP));
        canvas.drawPath(area, line);
        line.setShader(null);
        line.setColor(0xFF6A57ED); line.setStrokeWidth(d(3)); line.setStrokeCap(Paint.Cap.ROUND);
        line.setStyle(Paint.Style.STROKE); canvas.drawPath(path, line);
        line.setStyle(Paint.Style.FILL);
        for (int i = 0; i < scores.size(); i++) {
            float x = scores.size() == 1 ? (left + right) / 2f : left + (right - left) * i / (scores.size() - 1f);
            float y = bottom - (bottom - top) * scores.get(i) / 100f;
            line.setColor(Color.WHITE); canvas.drawCircle(x, y, d(5), line);
            line.setColor(0xFF6A57ED); canvas.drawCircle(x, y, d(3.4f), line);
        }
    }
}
