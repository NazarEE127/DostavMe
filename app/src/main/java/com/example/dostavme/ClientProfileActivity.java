package com.example.dostavme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.User;

public class ClientProfileActivity extends AppCompatActivity {
    private TextView tvFullName;
    private TextView tvPhone;
    private TextView tvEmail;
    private TextView tvOrderHistory;
    private Button btnCreateOrder;
    private Button btnOrderHistory;
    private Button btnLogout;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Профиль клиента");
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
        setupClickListeners();
        loadProfileData();
    }

    private void initializeViews() {
        tvFullName = findViewById(R.id.tvFullName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvOrderHistory = findViewById(R.id.tvOrderHistory);
        btnCreateOrder = findViewById(R.id.btnCreateOrder);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnCreateOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateOrderActivity.class);
            startActivity(intent);
        });

        btnOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        });


        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfileData() {
        if (currentUser != null) {
            tvFullName.setText("ФИО: " + currentUser.getFullName());
            tvPhone.setText("Номер телефона: " + currentUser.getPhone());
            tvEmail.setText("Email: " + currentUser.getEmail());
            
            int completedOrders = dbHelper.getCompletedOrdersCount(currentUser.getId());
            tvOrderHistory.setText("Завершено заказов: " + completedOrders);
        }
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