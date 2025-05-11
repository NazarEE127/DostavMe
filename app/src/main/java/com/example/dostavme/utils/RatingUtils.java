package com.example.dostavme.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class RatingUtils {
    private static final String PREF_NAME = "ratings";
    private static final String KEY_COURIER_RATING = "courier_rating_";
    private static final String KEY_ORDER_RATING = "order_rating_";

    public static void updateCourierRating(Context context, String courierId, int rating) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = KEY_COURIER_RATING + courierId;
        prefs.edit().putInt(key, rating).apply();
    }

    public static void updateOrderRating(Context context, String orderId, int rating) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = KEY_ORDER_RATING + orderId;
        prefs.edit().putInt(key, rating).apply();
    }

    public static double calculateAverageRating(int[] ratings) {
        if (ratings == null || ratings.length == 0) {
            return 0.0;
        }

        double sum = 0;
        for (int rating : ratings) {
            sum += rating;
        }
        return sum / ratings.length;
    }

    public static int getCourierRating(Context context, String courierId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = KEY_COURIER_RATING + courierId;
        return prefs.getInt(key, 0);
    }

    public static int getOrderRating(Context context, String orderId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = KEY_ORDER_RATING + orderId;
        return prefs.getInt(key, 0);
    }
} 