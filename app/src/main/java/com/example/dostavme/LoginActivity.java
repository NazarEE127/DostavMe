package com.example.dostavme;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.User;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Вход");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            redirectToAppropriateScreen(sessionManager.getUserRole());
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar == null) {
            Log.e(TAG, "ProgressBar не найден в layout");
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etEmail == null || etPassword == null) {
                    Toast.makeText(LoginActivity.this, "Ошибка инициализации полей ввода", Toast.LENGTH_SHORT).show();
                    return;
                }

                String phone = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

                if (phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String phoneNumber = phone.startsWith("+") ? phone : "+7" + phone;

                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                btnLogin.setEnabled(false);

                try {
                    User user = dbHelper.getUserByPhone(phoneNumber, password);
                    
                    if (user != null) {
                        Log.d(TAG, "Вход выполнен успешно для пользователя: " + user.getFullName());
                        Toast.makeText(LoginActivity.this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                        
                        sessionManager.login(user);
                        
                        redirectToAppropriateScreen(user.getRole());
                        finish();
                    } else {
                        Log.e(TAG, "Неверный номер телефона или пароль");
                        Toast.makeText(LoginActivity.this, "Неверный номер телефона или пароль", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при входе", e);
                    Toast.makeText(LoginActivity.this, "Ошибка при входе: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    btnLogin.setEnabled(true);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Введите email");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            return false;
        }
        return true;
    }

    private void redirectToAppropriateScreen(String userType) {
        Intent intent;
        if ("courier".equals(userType)) {
            intent = new Intent(this, CourierActivity.class);
        } else {
            intent = new Intent(this, ClientActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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