package com.example.dostavme;

import android.os.Bundle;
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
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;

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

        loadProfileData();
    }

    private void loadProfileData() {
        if (currentUser != null) {
            tvFullName.setText(currentUser.getFullName());
            tvPhone.setText(currentUser.getPhone());
            
            double rating = dbHelper.getCourierRating(currentUser.getId());
            int completedOrders = dbHelper.getCompletedOrdersCount(currentUser.getId());
            
            tvRating.setText(String.format("%.1f", rating));
            tvCompletedOrders.setText(String.valueOf(completedOrders));
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