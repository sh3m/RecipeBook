package com.sh3m.recipebook;

import android.app.Activity;
import android.content.Context;

public class ThemeManager {
    private static final String PREFS = "recipebook_prefs";
    private static final String KEY_DARK = "dark_mode";

    public static boolean isDark(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK, true);
    }

    public static void toggle(Context context) {
        boolean wasDark = isDark(context);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_DARK, !wasDark).apply();
    }

    public static void apply(Activity activity) {
        if (isDark(activity)) {
            activity.setTheme(R.style.Theme_RecipeBook);
        } else {
            activity.setTheme(R.style.Theme_RecipeBook_Light);
        }
    }
}
