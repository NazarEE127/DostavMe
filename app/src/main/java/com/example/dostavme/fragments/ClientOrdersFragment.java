package com.example.dostavme.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class ClientOrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private RecyclerView rvOrders;
    private TextView tvEmptyState;
    private OrderAdapter orderAdapter;
    private List<Order> orders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_orders, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupRecyclerView();
        loadOrders();

        return view;
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(orders, this, false);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        String clientId = sessionManager.getUserId();
        List<Order> clientOrders = dbHelper.getClientOrders(clientId);
        orders.clear();
        orders.addAll(clientOrders);
        orderAdapter.notifyDataSetChanged();

        if (orders.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(requireContext(), OrderDetailsActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 