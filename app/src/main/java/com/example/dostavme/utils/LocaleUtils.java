package com.example.dostavme.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleUtils {
    private static final String PREF_LANGUAGE = "pref_language";
    private static final String PREF_NAME = "DostavMePrefs";

    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_LANGUAGE, languageCode).apply();
    }

    public static String getCurrentLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_LANGUAGE, Locale.getDefault().getLanguage());
    }

    public static void applySavedLocale(Context context) {
        String language = getCurrentLanguage(context);
        setLocale(context, language);
    }
} 