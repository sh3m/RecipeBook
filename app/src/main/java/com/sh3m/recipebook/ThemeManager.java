package com.sh3m.recipebook;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREFS = "recipebook_prefs";
    private static final String KEY_DARK = "dark_mode";

    public static boolean isDark(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK, true); // dark by default
    }

    public static void toggle(Context context) {
        boolean wasDark = isDark(context);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_DARK, !wasDark).apply();
        applyTheme(!wasDark);
    }

    public static void apply(Context context) {
        applyTheme(isDark(context));
    }

    private static void applyTheme(boolean dark) {
        AppCompatDelegate.setDefaultNightMode(dark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
