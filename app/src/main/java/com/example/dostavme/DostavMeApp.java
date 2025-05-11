package com.example.dostavme;

import android.app.Application;
import android.content.Context;

import com.example.dostavme.utils.LocaleUtils;

public class DostavMeApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LocaleUtils.applySavedLocale(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        LocaleUtils.applySavedLocale(base);
    }
} 