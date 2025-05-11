package com.example.dostavme;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.UUID;

public class CreateOrderActivity extends AppCompatActivity {
    private TextInputEditText etDescription, etFromAddress, etToAddress;
    private MaterialButton btnCreateOrder;
    private FloatingActionButton btnSelectFromLocation, btnSelectToLocation;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private static final double FIXED_PRICE = 120.0;
    private static final int PICK_FROM_LOCATION = 1;
    private static final int PICK_TO_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Создать заказ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etDescription = findViewById(R.id.etDescription);
        etFromAddress = findViewById(R.id.etFromAddress);
        etToAddress = findViewById(R.id.etToAddress);
        btnCreateOrder = findViewById(R.id.btnCreateOrder);
        btnSelectFromLocation = findViewById(R.id.btnSelectFromLocation);
        btnSelectToLocation = findViewById(R.id.btnSelectToLocation);
    }

    private void setupClickListeners() {
        btnCreateOrder.setOnClickListener(v -> createOrder());
        btnSelectFromLocation.setOnClickListener(v -> openMapForLocation(PICK_FROM_LOCATION));
        btnSelectToLocation.setOnClickListener(v -> openMapForLocation(PICK_TO_LOCATION));
    }

    private void openMapForLocation(int requestCode) {
        Intent intent = new Intent(this, MapPickerActivity.class);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("address");
            if (requestCode == PICK_FROM_LOCATION) {
                etFromAddress.setText(address);
            } else if (requestCode == PICK_TO_LOCATION) {
                etToAddress.setText(address);
            }
        }
    }

    private void createOrder() {
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String fromAddress = etFromAddress.getText() != null ? etFromAddress.getText().toString().trim() : "";
        String toAddress = etToAddress.getText() != null ? etToAddress.getText().toString().trim() : "";

        if (description.isEmpty() || fromAddress.isEmpty() || toAddress.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        String clientId = sessionManager.getUserId();
        if (clientId == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderId = UUID.randomUUID().toString();
        Order order = new Order(
            orderId,
            clientId,
            null,
            fromAddress,
            toAddress,
            description,
            1.0,
            FIXED_PRICE,
            "new",
            new Date().toString()
        );

        boolean result = dbHelper.addOrder(order);
        if (result) {
            Toast.makeText(this, "Заказ успешно создан", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка при создании заказа", Toast.LENGTH_SHORT).show();
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