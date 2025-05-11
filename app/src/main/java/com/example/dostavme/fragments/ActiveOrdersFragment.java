package com.example.dostavme.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dostavme.OrderDetailsActivity;
import com.example.dostavme.R;
import com.example.dostavme.SessionManager;
import com.example.dostavme.adapters.OrderAdapter;
import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.Order;

import java.util.ArrayList;
import java.util.List;

public class ActiveOrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orders;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private View tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active_orders, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orders = new ArrayList<>();
        dbHelper = new DatabaseHelper(getContext());
        sessionManager = new SessionManager(getContext());
        String userRole = sessionManager.getUserRole();
        adapter = new OrderAdapter(orders, this, "courier".equals(userRole));
        recyclerView.setAdapter(adapter);

        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        loadOrders();

        return view;
    }

    private void loadOrders() {
        String userId = sessionManager.getUserId();
        String userRole = sessionManager.getUserRole();
        
        List<Order> orders;
        if ("courier".equals(userRole)) {
            orders = dbHelper.getCourierOrders(userId);
        } else {
            orders = dbHelper.getClientOrders(userId);
        }
        
        adapter.updateOrders(orders);
        
        if (orders.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(getContext(), OrderDetailsActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 