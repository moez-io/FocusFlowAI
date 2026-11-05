package com.moez.focusflow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/** Small no-dependency UI utilities and persistent bottom-tab navigation. */
public final class AppUi {
    private AppUi() {}
    public static final int DARK = Color.rgb(19, 31, 60);
    public static final int PURPLE = Color.rgb(105, 88, 232);
    public static final int MUTED = Color.rgb(116, 131, 156);
    public static void systemBars(Activity activity, boolean darkHeader) {
        activity.getWindow().setStatusBarColor(darkHeader ? DARK : Color.rgb(245, 247, 252));
        activity.getWindow().setNavigationBarColor(Color.WHITE);
        int flags = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        if (!darkHeader) flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    public static void bindNav(Activity activity, int activeTab) {
        final int[] ids = {R.id.navHome, R.id.navAnalyze, R.id.navTimer, R.id.navHistory};
        final Class<?>[] screens = {MainActivity.class, AnalyzerActivity.class, TimerActivity.class, HistoryActivity.class};
        for (int i = 0; i < ids.length; i++) {
            final int index = i;
            View tab = activity.findViewById(ids[i]);
            if (tab == null) continue;
            ImageView icon = tab.findViewById(R.id.navIcon);
            TextView label = tab.findViewById(R.id.navLabel);
            int tint = (i == activeTab) ? PURPLE : MUTED;
            if (icon != null) icon.setColorFilter(tint);
            if (label != null) {
                label.setTextColor(tint);
                label.setTypeface(null, i == activeTab ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            }
            if (i == activeTab) tab.setBackgroundResource(R.drawable.bg_tab_active);
            tab.setOnClickListener(v -> {
                if (activeTab == index) return;
                Intent intent = new Intent(activity, screens[index]);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }
    public static void text(Activity activity, int id, String value) {
        TextView t = activity.findViewById(id);
        if (t != null) t.setText(value);
    }
}
