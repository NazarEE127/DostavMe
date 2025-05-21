package com.example.dostavme.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class CourierOrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private OrderAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courier_orders, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        
        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());
        
        setupRecyclerView();
        loadOrders();
        
        return view;
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(new ArrayList<>(), this, true);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadOrders() {
        String courierId = sessionManager.getUserId();
        List<Order> orders = dbHelper.getCourierOrders(courierId);
        
        if (orders.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateOrders(orders);
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