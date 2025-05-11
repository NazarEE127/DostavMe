package com.example.dostavme.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dostavme.R;
import com.example.dostavme.models.Order;
import com.example.dostavme.models.User;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private OnOrderClickListener listener;
    private boolean isCourier;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(List<Order> orders, OnOrderClickListener listener, boolean isCourier) {
        this.orders = orders;
        this.listener = listener;
        this.isCourier = isCourier;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view, listener, isCourier);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId;
        private TextView tvStatus;
        private TextView tvPrice;
        private TextView tvFromAddress;
        private TextView tvToAddress;
        private Button btnTakeOrder;
        private OnOrderClickListener listener;
        private boolean isCourier;

        public OrderViewHolder(@NonNull View itemView, OnOrderClickListener listener, boolean isCourier) {
            super(itemView);
            this.listener = listener;
            this.isCourier = isCourier;
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvFromAddress = itemView.findViewById(R.id.tvFromAddress);
            tvToAddress = itemView.findViewById(R.id.tvToAddress);
            btnTakeOrder = itemView.findViewById(R.id.btnTakeOrder);
        }

        public void bind(Order order) {
            tvOrderId.setText("Заказ #" + order.getId());
            tvStatus.setText("Статус: " + order.getStatus());
            tvPrice.setText(String.format("Цена: %.2f ₽", order.getPrice()));
            tvFromAddress.setText("Откуда: " + order.getFromAddress());
            tvToAddress.setText("Куда: " + order.getToAddress());

            if (isCourier && "new".equals(order.getStatus())) {
                btnTakeOrder.setVisibility(View.VISIBLE);
                btnTakeOrder.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onOrderClick(order);
                    }
                });
            } else {
                btnTakeOrder.setVisibility(View.GONE);
            }
        }
    }
} 