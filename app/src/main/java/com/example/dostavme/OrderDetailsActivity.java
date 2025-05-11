package com.example.dostavme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.Order;
import com.example.dostavme.models.User;
import com.example.dostavme.utils.GeocodingUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class OrderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private GoogleMap mMap;
    private Order currentOrder;
    private User client;
    private Button btnAcceptOrder;
    private Button btnCompleteOrder;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Детали заказа");

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnAcceptOrder = findViewById(R.id.btnAcceptOrder);
        btnCompleteOrder = findViewById(R.id.btnCompleteOrder);

        String orderId = getIntent().getStringExtra("order_id");
        if (orderId != null) {
            loadOrderDetails(orderId);
        } else {
            Toast.makeText(this, "Ошибка загрузки заказа", Toast.LENGTH_SHORT).show();
            finish();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void loadOrderDetails(String orderId) {
        Log.d("OrderDetailsActivity", "Loading order details for ID: " + orderId);
        currentOrder = dbHelper.getOrder(orderId);
        if (currentOrder != null) {
            Log.d("OrderDetailsActivity", "Order loaded: status=" + currentOrder.getStatus() + 
                ", courierId=" + currentOrder.getCourierId());
            client = dbHelper.getUser(currentOrder.getClientId());
            String userRole = sessionManager.getUserRole();
            String currentUserId = sessionManager.getUserId();
            Log.d("OrderDetailsActivity", "Current user: role=" + userRole + ", id=" + currentUserId);
            
            updateUI();
            setupButtons();
            
            new Handler().postDelayed(() -> {
                updateUI();
                setupButtons();
            }, 500);
        } else {
            Log.e("OrderDetailsActivity", "Order not found");
            Toast.makeText(this, "Заказ не найден", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateUI() {
        TextView tvOrderId = findViewById(R.id.tvOrderId);
        TextView tvClientName = findViewById(R.id.tvClientName);
        TextView tvFromAddress = findViewById(R.id.tvFromAddress);
        TextView tvToAddress = findViewById(R.id.tvToAddress);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvWeight = findViewById(R.id.tvWeight);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvStatus = findViewById(R.id.tvStatus);

        tvOrderId.setText("Заказ #" + currentOrder.getId());
        tvClientName.setText("Клиент: " + client.getFullName());
        tvFromAddress.setText("Откуда: " + currentOrder.getFromAddress());
        tvToAddress.setText("Куда: " + currentOrder.getToAddress());
        tvDescription.setText("Описание: " + currentOrder.getDescription());
        tvWeight.setText("Вес: " + currentOrder.getWeight() + " кг");
        tvPrice.setText("Цена: " + currentOrder.getPrice() + " ₽");
        tvStatus.setText("Статус: " + getStatusText(currentOrder.getStatus()));
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "Ожидает курьера";
            case "in_progress":
                return "В пути";
            case "completed":
                return "Завершен";
            default:
                return status;
        }
    }

    private void setupButtons() {
        String userRole = sessionManager.getUserRole();
        String orderStatus = currentOrder.getStatus();
        String courierId = currentOrder.getCourierId();
        String currentUserId = sessionManager.getUserId();

        if ("courier".equals(userRole)) {
            if ("pending".equals(orderStatus)) {
                btnAcceptOrder.setVisibility(View.VISIBLE);
                btnCompleteOrder.setVisibility(View.GONE);
                btnAcceptOrder.setOnClickListener(v -> acceptOrder());
            } else if ("in_progress".equals(orderStatus) && 
                      courierId != null && 
                      courierId.equals(currentUserId)) {
                btnAcceptOrder.setVisibility(View.GONE);
                btnCompleteOrder.setVisibility(View.VISIBLE);
                btnCompleteOrder.setOnClickListener(v -> completeOrder());
            } else {
                btnAcceptOrder.setVisibility(View.GONE);
                btnCompleteOrder.setVisibility(View.GONE);
            }
        } else {
            btnAcceptOrder.setVisibility(View.GONE);
            btnCompleteOrder.setVisibility(View.GONE);
        }
    }

    private void acceptOrder() {
        String courierId = sessionManager.getUserId();
        if (dbHelper.assignCourierToOrder(currentOrder.getId(), courierId)) {
            Toast.makeText(this, "Заказ принят", Toast.LENGTH_SHORT).show();
            currentOrder.setStatus("in_progress");
            currentOrder.setCourierId(courierId);
            updateUI();
            setupButtons();
        } else {
            Toast.makeText(this, "Ошибка при принятии заказа", Toast.LENGTH_SHORT).show();
        }
    }

    private void completeOrder() {
        Log.d("OrderDetailsActivity", "Starting order completion process");
        LatLng deliveryLocation = GeocodingUtils.getLocationFromAddress(this, currentOrder.getToAddress());
        if (deliveryLocation != null) {
            Log.d("OrderDetailsActivity", "Delivery location: " + deliveryLocation.latitude + ", " + deliveryLocation.longitude);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("OrderDetailsActivity", "Location permission not granted");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                return;
            }

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Log.d("OrderDetailsActivity", "Current location: " + location.getLatitude() + ", " + location.getLongitude());
                            LatLng courierLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            float[] results = new float[1];
                            android.location.Location.distanceBetween(
                                    courierLocation.latitude, courierLocation.longitude,
                                    deliveryLocation.latitude, deliveryLocation.longitude,
                                    results);

                            float distance = results[0];
                            Log.d("OrderDetailsActivity", "Distance to delivery point: " + distance + " meters");
                            if (distance <= 100) {
                                if (dbHelper.completeOrder(currentOrder.getId())) {
                                    Log.d("OrderDetailsActivity", "Order completed successfully");
                                    Toast.makeText(this, "Заказ успешно завершен", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Log.e("OrderDetailsActivity", "Failed to complete order in database");
                                    Toast.makeText(this, "Ошибка при завершении заказа", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d("OrderDetailsActivity", "Too far from delivery point");
                                Toast.makeText(this, 
                                    "Вы находитесь слишком далеко от точки доставки (" + 
                                    String.format("%.0f", distance) + " м)", 
                                    Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e("OrderDetailsActivity", "Unable to get current location");
                            Toast.makeText(this, "Не удалось определить ваше местоположение", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("OrderDetailsActivity", "Unable to get delivery location");
            Toast.makeText(this, "Не удалось определить координаты доставки", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (currentOrder != null) {
            LatLng fromLocation = GeocodingUtils.getLocationFromAddress(this, currentOrder.getFromAddress());
            LatLng toLocation = GeocodingUtils.getLocationFromAddress(this, currentOrder.getToAddress());

            if (fromLocation != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(fromLocation)
                        .title("Откуда: " + currentOrder.getFromAddress()));
            }

            if (toLocation != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(toLocation)
                        .title("Куда: " + currentOrder.getToAddress()));
            }

            if (fromLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fromLocation, 12));
            }
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