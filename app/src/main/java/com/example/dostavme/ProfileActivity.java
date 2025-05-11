package com.example.dostavme;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvFullName;
    private TextInputEditText etName, etEmail, etPhone, etCurrentPassword, etNewPassword;
    private MaterialButton btnEditProfile, btnSave, btnLogout;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Профиль");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        tvFullName = findViewById(R.id.tvFullName);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        btnSave.setVisibility(View.GONE);
    }

    private void loadUserData() {
        if (currentUser != null) {
            if (tvFullName != null) {
                tvFullName.setText(currentUser.getFullName());
            }
            if (etName != null) {
                etName.setText(currentUser.getFullName());
                etName.setEnabled(false);
            }
            if (etEmail != null) {
                etEmail.setText(currentUser.getEmail());
                etEmail.setEnabled(false);
            }
            if (etPhone != null) {
                etPhone.setText(currentUser.getPhone());
                etPhone.setEnabled(false);
            }
        }
    }

    private void setupClickListeners() {
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                if (etName != null) etName.setEnabled(true);
                if (etEmail != null) etEmail.setEnabled(true);
                if (etPhone != null) etPhone.setEnabled(true);
                if (btnSave != null) btnSave.setVisibility(View.VISIBLE);
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String newName = etName.getText().toString().trim();
                String newEmail = etEmail.getText().toString().trim();
                String newPhone = etPhone.getText().toString().trim();
                String currentPassword = etCurrentPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();

                if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                    Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!currentPassword.isEmpty() || !newPassword.isEmpty()) {
                    if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                        Toast.makeText(this, "Для смены пароля заполните оба поля", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!currentUser.getPassword().equals(currentPassword)) {
                        Toast.makeText(this, "Неверный текущий пароль", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                currentUser.setFullName(newName);
                currentUser.setEmail(newEmail);
                currentUser.setPhone(newPhone);
                if (!newPassword.isEmpty()) {
                    currentUser.setPassword(newPassword);
                }

                if (dbHelper.updateUser(currentUser)) {
                    Toast.makeText(this, "Профиль успешно обновлен", Toast.LENGTH_SHORT).show();
                    etName.setEnabled(false);
                    etEmail.setEnabled(false);
                    etPhone.setEnabled(false);
                    btnSave.setVisibility(View.GONE);
                    etCurrentPassword.setText("");
                    etNewPassword.setText("");
                } else {
                    Toast.makeText(this, "Ошибка при обновлении профиля", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                sessionManager.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 