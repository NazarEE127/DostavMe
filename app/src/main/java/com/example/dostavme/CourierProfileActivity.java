package com.example.dostavme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.User;

public class CourierProfileActivity extends AppCompatActivity {
    private TextView tvFullName;
    private TextView tvPhone;
    private TextView tvRating;
    private TextView tvCompletedOrders;
    private TextView tvBalance;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;
    private Button btnWithdraw;
    private Button btnOrderHistory;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Профиль курьера");
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

        tvFullName = findViewById(R.id.tvFullName);
        tvPhone = findViewById(R.id.tvPhone);
        tvRating = findViewById(R.id.tvRating);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);
        tvBalance = findViewById(R.id.tvBalance);

        btnWithdraw = findViewById(R.id.btnWithdraw);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnLogout = findViewById(R.id.btnLogout);

        setupClickListeners();
        loadProfileData();
    }

    private void loadProfileData() {
        if (currentUser != null) {
            tvFullName.setText("ФИО: " + currentUser.getFullName());
            tvPhone.setText("Номер телефона: "+ currentUser.getPhone());


            double rating = dbHelper.getCourierRating(currentUser.getId());
            int completedOrders = dbHelper.getCompletedOrdersCount(currentUser.getId());
            
            tvRating.setText("Рейтинг: " + String.format("%.1f", rating));
            tvCompletedOrders.setText("Выполнено заказов: " + String.valueOf(completedOrders));
            tvPhone.setText("Баланс: "+ String.format("%.1f", currentUser.getBalance()));
        }
    }

    private void setupClickListeners() {
        btnWithdraw.setOnClickListener(v -> {
            Toast.makeText(this, "Вывод средств пока не доступен", Toast.LENGTH_LONG).show();
        });

        btnOrderHistory.setOnClickListener(v -> {
            Toast.makeText(this, "История заказов пока не доступна", Toast.LENGTH_LONG).show();
        });


        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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