package com.example.dostavme.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.dostavme.OrderDetailsActivity;
import com.example.dostavme.R;
import com.example.dostavme.SessionManager;
import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.Order;
import com.example.dostavme.utils.GeocodingUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourierMapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private Map<String, Order> orderMarkers = new HashMap<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Order currentOrder;
    private Button btnCompleteDelivery;
    private ImageButton btnZoomIn, btnZoomOut, btnMyLocation;
    private static final float DELIVERY_RADIUS = 100;
    private static final String TAG = "CourierMapFragment";
    private AlertDialog dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courier_map, container, false);

        btnCompleteDelivery = view.findViewById(R.id.btnCompleteDelivery);
        btnCompleteDelivery.setOnClickListener(v -> completeDelivery());
        
        View mapControls = view.findViewById(R.id.mapControls);
        btnZoomIn = mapControls.findViewById(R.id.btnZoomIn);
        btnZoomOut = mapControls.findViewById(R.id.btnZoomOut);
        btnMyLocation = mapControls.findViewById(R.id.btnMyLocation);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);
        
        setupMapControls();
        
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        loadAndDisplayOrders();
                    }
                });

        mMap.setOnMarkerClickListener(marker -> {
            Order order = orderMarkers.get(marker.getId());
            if (order != null) {
                if (order.getStatus().equals("new")) {
                    showOrderDetailsDialog(order);
                } else {
                    Intent intent = new Intent(requireContext(), OrderDetailsActivity.class);
                    intent.putExtra("order_id", order.getId());
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });
    }

    private void setupMapControls() {
        btnZoomIn.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        btnZoomOut.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        btnMyLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(requireActivity(), location -> {
                            if (location != null) {
                                LatLng userLocation = new LatLng(
                                        location.getLatitude(), 
                                        location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                            }
                        });
            }
        });
    }

    private void loadAndDisplayOrders() {
        List<Order> orders = dbHelper.getNewOrders();
        Log.d(TAG, "Получено заказов: " + orders.size());
        
        mMap.clear();
        orderMarkers.clear();

        for (Order order : orders) {
            if (order == null) {
                Log.e(TAG, "loadAndDisplayOrders: получен null заказ");
                continue;
            }

            String orderId = order.getId();
            if (orderId == null) {
                Log.e(TAG, "loadAndDisplayOrders: у заказа отсутствует ID");
                continue;
            }

            Log.d(TAG, "Обработка заказа: " + orderId + ", статус: " + order.getStatus());
            
            LatLng fromLocation = GeocodingUtils.getLocationFromAddress(requireContext(), order.getFromAddress());
            if (fromLocation != null) {
                Log.d(TAG, "Координаты получены: " + fromLocation.latitude + ", " + fromLocation.longitude);
                
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(fromLocation)
                        .title("Заказ #" + orderId)
                        .snippet("От: " + order.getFromAddress() + "\nДо: " + order.getToAddress() + 
                                "\nЦена: " + order.getPrice() + " ₽");

                Marker marker = mMap.addMarker(markerOptions);
                if (marker != null) {
                    orderMarkers.put(marker.getId(), order);
                    Log.d(TAG, "Маркер добавлен на карту для заказа " + orderId);
                } else {
                    Log.e(TAG, "Не удалось добавить маркер на карту для заказа " + orderId);
                }
            } else {
                Log.e(TAG, "Не удалось получить координаты для адреса: " + order.getFromAddress());
            }
        }
    }

    private void showOrderDetailsDialog(Order order) {
        if (order == null) {
            Log.e(TAG, "showOrderDetailsDialog: order is null");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_order_details, null);
        
        TextView tvFromAddress = view.findViewById(R.id.tvFromAddress);
        TextView tvToAddress = view.findViewById(R.id.tvToAddress);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvWeight = view.findViewById(R.id.tvWeight);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        Button btnTakeOrder = view.findViewById(R.id.btnTakeOrder);
        Button btnCompleteOrder = view.findViewById(R.id.btnCompleteOrder);

        tvFromAddress.setText(order.getFromAddress());
        tvToAddress.setText(order.getToAddress());
        tvDescription.setText(order.getDescription());
        tvWeight.setText(String.valueOf(order.getWeight()));
        tvPrice.setText(String.valueOf(order.getPrice()));
        tvStatus.setText(order.getStatus());

        if ("new".equals(order.getStatus())) {
            btnTakeOrder.setVisibility(View.VISIBLE);
            btnCompleteOrder.setVisibility(View.GONE);
        } else if ("in_progress".equals(order.getStatus()) && 
                   order.getCourierId() != null && 
                   order.getCourierId().equals(getCurrentUserId())) {
            btnTakeOrder.setVisibility(View.GONE);
            btnCompleteOrder.setVisibility(View.VISIBLE);
        } else {
            btnTakeOrder.setVisibility(View.GONE);
            btnCompleteOrder.setVisibility(View.GONE);
        }

        builder.setView(view)
               .setTitle("Детали заказа")
               .setPositiveButton("Закрыть", null);

        AlertDialog dialog = builder.create();

        btnTakeOrder.setOnClickListener(v -> {
            String courierId = getCurrentUserId();
            Log.d(TAG, "btnTakeOrder onClick: courierId=" + courierId);
            
            if (courierId != null) {
                Log.d(TAG, "btnTakeOrder onClick: orderId=" + order.getId());
                DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
                boolean success = dbHelper.assignCourierToOrder(order.getId(), courierId);
                Log.d(TAG, "btnTakeOrder onClick: success=" + success);
                
                if (success) {
                    Toast.makeText(requireContext(), "Заказ успешно взят", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadAndDisplayOrders();
                } else {
                    Toast.makeText(requireContext(), "Ошибка при взятии заказа", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "btnTakeOrder onClick: courierId is null");
                Toast.makeText(requireContext(), "Ошибка: не удалось определить ID курьера", Toast.LENGTH_SHORT).show();
            }
        });

        btnCompleteOrder.setOnClickListener(v -> {
            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
            boolean success = dbHelper.completeOrder(order.getId());
            if (success) {
                Toast.makeText(requireContext(), "Заказ успешно завершен", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadAndDisplayOrders();
            } else {
                Toast.makeText(requireContext(), "Ошибка при завершении заказа", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private String getCurrentUserId() {
        return sessionManager.getUserId();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(requireActivity(), location -> {
                                    if (location != null) {
                                        LatLng userLocation = new LatLng(
                                                location.getLatitude(), 
                                                location.getLongitude());
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                                        loadAndDisplayOrders();
                                    }
                                });
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Для работы карты необходим доступ к местоположению",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            loadAndDisplayOrders();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void completeDelivery() {
        if (currentOrder == null) {
            Toast.makeText(requireContext(), "Нет активного заказа", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng deliveryLocation = GeocodingUtils.getLocationFromAddress(
                                requireContext(), 
                                currentOrder.getToAddress()
                            );
                            
                            if (deliveryLocation != null) {
                                Location deliveryLoc = new Location("");
                                deliveryLoc.setLatitude(deliveryLocation.latitude);
                                deliveryLoc.setLongitude(deliveryLocation.longitude);

                                float distance = location.distanceTo(deliveryLoc);
                                
                                if (distance <= DELIVERY_RADIUS) {
                                    if (dbHelper.completeOrder(currentOrder.getId())) {
                                        if (dbHelper.updateCourierBalance(
                                            sessionManager.getUserId(), 
                                            currentOrder.getPrice()
                                        )) {
                                            Toast.makeText(requireContext(), 
                                                "Доставка завершена! Баланс обновлен", 
                                                Toast.LENGTH_SHORT).show();
                                            currentOrder = null;
                                            btnCompleteDelivery.setVisibility(View.GONE);
                                            loadAndDisplayOrders();
                                        } else {
                                            Toast.makeText(requireContext(), 
                                                "Ошибка при обновлении баланса", 
                                                Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), 
                                            "Ошибка при завершении заказа", 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(requireContext(), 
                                        "Вы находитесь слишком далеко от точки доставки", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(requireContext(), 
                                    "Ошибка при определении координат доставки", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
        if (order != null) {
            btnCompleteDelivery.setVisibility(View.VISIBLE);
            LatLng deliveryLocation = GeocodingUtils.getLocationFromAddress(
                requireContext(), 
                order.getToAddress()
            );
            if (deliveryLocation != null) {
                mMap.addMarker(new MarkerOptions()
                    .position(deliveryLocation)
                    .title("Точка доставки"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliveryLocation, 15));
            }
        } else {
            btnCompleteDelivery.setVisibility(View.GONE);
        }
    }
} 