package com.example.dostavme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.User;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "DostavMePrefs";
    private static final String KEY_ORDER_NOTIFICATIONS = "order_notifications";
    private static final String KEY_PROMO_NOTIFICATIONS = "promo_notifications";
    private static final String KEY_DARK_THEME = "dark_theme";

    private Switch switchOrderNotifications;
    private Switch switchPromoNotifications;
    private Switch switchDarkTheme;
    private Button btnChangePassword;
    private Button btnDeleteAccount;
    private Button btnPrivacyPolicy;
    private Button btnTermsOfService;
    private TextView tvVersion;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Настройки");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        String userId = sessionManager.getUserId();
        if (userId != null) {
            currentUser = dbHelper.getUser(userId);
        }

        if (currentUser == null) {
            Toast.makeText(this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        switchOrderNotifications = findViewById(R.id.switchOrderNotifications);
        switchPromoNotifications = findViewById(R.id.switchPromoNotifications);
        switchDarkTheme = findViewById(R.id.switchDarkTheme);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnTermsOfService = findViewById(R.id.btnTermsOfService);
        tvVersion = findViewById(R.id.tvVersion);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        switchOrderNotifications.setChecked(prefs.getBoolean(KEY_ORDER_NOTIFICATIONS, true));
        switchPromoNotifications.setChecked(prefs.getBoolean(KEY_PROMO_NOTIFICATIONS, true));
        switchDarkTheme.setChecked(prefs.getBoolean(KEY_DARK_THEME, false));
    }

    private void setupClickListeners() {
        switchOrderNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings(KEY_ORDER_NOTIFICATIONS, isChecked);
        });

        switchPromoNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings(KEY_PROMO_NOTIFICATIONS, isChecked);
        });

        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings(KEY_DARK_THEME, isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        btnPrivacyPolicy.setOnClickListener(v -> openUrl("https://dostavme.ru/privacy"));
        btnTermsOfService.setOnClickListener(v -> openUrl("https://dostavme.ru/terms"));
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменение пароля");

        final TextInputEditText etCurrentPassword = new TextInputEditText(this);
        etCurrentPassword.setHint("Текущий пароль");
        builder.setView(etCurrentPassword);

        builder.setPositiveButton("Продолжить", (dialog, which) -> {
            String currentPassword = etCurrentPassword.getText().toString();
            if (currentPassword.equals(currentUser.getPassword())) {
                showNewPasswordDialog();
            } else {
                Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showNewPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Новый пароль");

        final TextInputEditText etNewPassword = new TextInputEditText(this);
        etNewPassword.setHint("Новый пароль");
        builder.setView(etNewPassword);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newPassword = etNewPassword.getText().toString();
            if (newPassword.length() >= 6) {
                currentUser.setPassword(newPassword);
                if (dbHelper.updateUser(currentUser)) {
                    Toast.makeText(this, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ошибка при изменении пароля", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Удаление аккаунта")
            .setMessage("Вы уверены, что хотите удалить свой аккаунт? Это действие нельзя отменить.")
            .setPositiveButton("Удалить", (dialog, which) -> {
                if (dbHelper.deleteUser(currentUser.getId())) {
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Ошибка при удалении аккаунта", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void saveSettings(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(key, value).apply();
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 