package com.example.dostavme;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.Order;
import com.example.dostavme.utils.RatingUtils;

public class RateOrderActivity extends AppCompatActivity {
    private TextView tvOrderInfo;
    private RatingBar ratingBar;
    private Button btnSubmit;
    private DatabaseHelper dbHelper;
    private String orderId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Оценка заказа");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DatabaseHelper(this);
        orderId = getIntent().getStringExtra("orderId");

        initializeViews();
        loadOrderInfo();
        setupClickListeners();
    }

    private void initializeViews() {
        tvOrderInfo = findViewById(R.id.tvOrderInfo);
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void loadOrderInfo() {
        if (orderId != null) {
            currentOrder = dbHelper.getOrder(orderId);
            if (currentOrder != null) {
                String orderInfo = String.format("Заказ #%s\nОткуда: %s\nКуда: %s\nСумма: %d ₽",
                        currentOrder.getId(),
                        currentOrder.getFromAddress(),
                        currentOrder.getToAddress(),
                        currentOrder.getPrice());
                tvOrderInfo.setText(orderInfo);
            } else {
                Toast.makeText(this, "Ошибка загрузки информации о заказе", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Не указан ID заказа", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        btnSubmit.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        if (currentOrder != null) {
            float rating = ratingBar.getRating();
            if (rating > 0) {
                RatingUtils.updateOrderRating(this, orderId, (int)rating);
                if (currentOrder.getCourierId() != null) {
                    RatingUtils.updateCourierRating(this, currentOrder.getCourierId(), (int)rating);
                }

                Toast.makeText(this, "Спасибо за вашу оценку!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Пожалуйста, поставьте оценку", Toast.LENGTH_SHORT).show();
            }
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