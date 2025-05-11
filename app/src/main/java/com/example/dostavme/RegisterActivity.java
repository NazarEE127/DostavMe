package com.example.dostavme;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.User;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private EditText etFullName, etPhone, etPassword, etConfirmPassword;
    private RadioGroup rgUserType;
    private Button btnRegister;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Регистрация");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgUserType = findViewById(R.id.rgUserType);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    registerUser();
                }
            }
        });
    }

    private boolean validateInput() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Введите ФИО");
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Введите номер телефона");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Введите пароль");
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Подтвердите пароль");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            return false;
        }

        if (rgUserType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Выберите тип пользователя", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerUser() {
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        final String phoneNumber = phone.startsWith("+") ? phone : "+7" + phone;
        
        RadioButton selectedRadioButton = findViewById(rgUserType.getCheckedRadioButtonId());
        String userType = "";
        if (selectedRadioButton.getText().toString().toLowerCase().equals("курьер")){
            userType = "courier";
        } else {
            userType = "client";
        }

        try {
            Log.d(TAG, "Начало регистрации пользователя: " + fullName);
            if (dbHelper.isUserExists(phoneNumber)) {
                Log.w(TAG, "Номер телефона уже зарегистрирован: " + phoneNumber);
                Toast.makeText(this, "Этот номер телефона уже зарегистрирован", Toast.LENGTH_LONG).show();
                return;
            }

            User newUser = new User(
                UUID.randomUUID().toString(),
                fullName,
                phoneNumber,
                null,
                password,
                userType,
                0.0,
                0, 
                0.0 
            );

            Log.d(TAG, "Попытка добавления пользователя в базу данных");
            long userId = dbHelper.addUser(newUser);
            
            if (userId != -1) {
                Log.d(TAG, "Пользователь успешно зарегистрирован с ID: " + userId);
                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                
                SessionManager sessionManager = new SessionManager(this);
                sessionManager.createLoginSession(newUser.getId(), userType);
                
                redirectToAppropriateScreen(userType);
                finish();
            } else {
                Log.e(TAG, "Ошибка при регистрации пользователя: userId = -1");
                Toast.makeText(this, "Ошибка при регистрации", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при регистрации", e);
            Toast.makeText(this, "Ошибка при регистрации: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
        }
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