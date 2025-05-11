package com.example.dostavme;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dostavme.adapters.OrderAdapter;
import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {
    private RecyclerView rvOrders;
    private TextView tvEmptyState;
    private OrderAdapter orderAdapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Order> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("История заказов");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        orders = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        loadOrders();
    }

    private void initializeViews() {
        rvOrders = findViewById(R.id.rvOrders);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        String userRole = sessionManager.getUserRole();
        orderAdapter = new OrderAdapter(orders, this, "courier".equals(userRole));
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        String userId = sessionManager.getUserId();
        String userRole = sessionManager.getUserRole();

        if (userRole.equals("courier")) {
            orders = dbHelper.getCourierOrders(userId);
        } else {
            orders = dbHelper.getClientOrders(userId);
        }

        if (orders.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
            orderAdapter.updateOrders(orders);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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