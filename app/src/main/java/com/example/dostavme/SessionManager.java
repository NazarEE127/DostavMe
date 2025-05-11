package com.example.dostavme;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.dostavme.models.User;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "DostavMeSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_LOGIN_TIME = "login_time";
    private static final long SESSION_DURATION = 2 * 24 * 60 * 60 * 1000;
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_RATING = "userRating";
    private static final String KEY_USER_COMPLETED_ORDERS = "userCompletedOrders";
    private static final String KEY_USER_BALANCE = "userBalance";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String role) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.commit();
        Log.d(TAG, "Сессия создана для пользователя: " + userId);
    }

    public boolean isLoggedIn() {
        if (!pref.contains(KEY_USER_ID)) {
            return false;
        }

        long loginTime = pref.getLong(KEY_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - loginTime > SESSION_DURATION) {
            Log.d(TAG, "Сессия истекла");
            logout();
            return false;
        }
        
        return true;
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, null);
    }

    public void logout() {
        editor.clear();
        editor.commit();
        Log.d(TAG, "Пользователь вышел из системы");
    }

    public void login(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putString(KEY_USER_NAME, user.getFullName());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putFloat(KEY_USER_RATING, (float) user.getRating());
        editor.putInt(KEY_USER_COMPLETED_ORDERS, user.getCompletedOrders());
        editor.putFloat(KEY_USER_BALANCE, (float) user.getBalance());
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }
} 