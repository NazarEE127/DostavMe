package com.example.dostavme.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.dostavme.R;
import com.example.dostavme.SessionManager;
import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.Order;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.Date;
import java.util.UUID;

public class ClientMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "ClientMapFragment";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ImageButton btnZoomIn, btnZoomOut, btnMyLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_map, container, false);
        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());
        
        View mapControls = view.findViewById(R.id.mapControls);
        btnZoomIn = mapControls.findViewById(R.id.btnZoomIn);
        btnZoomOut = mapControls.findViewById(R.id.btnZoomOut);
        btnMyLocation = mapControls.findViewById(R.id.btnMyLocation);
        
        Button btnCreateOrder = view.findViewById(R.id.btnCreateOrder);
        btnCreateOrder.setOnClickListener(v -> showCreateOrderDialog());
        
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    }
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
                                    }
                                });
                    }
                }
            }
        }
    }

    private void showCreateOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Создать заказ");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_order, null);
        builder.setView(dialogView);
        EditText fromAddressInput = dialogView.findViewById(R.id.fromAddressInput);
        EditText toAddressInput = dialogView.findViewById(R.id.toAddressInput);
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        EditText weightInput = dialogView.findViewById(R.id.weightInput);
        EditText priceInput = dialogView.findViewById(R.id.priceInput);

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String fromAddress = fromAddressInput.getText().toString().trim();
            String toAddress = toAddressInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String weightStr = weightInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();

            if (fromAddress.isEmpty() || toAddress.isEmpty() || description.isEmpty() || 
                weightStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(requireContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double weight = Double.parseDouble(weightStr);
                double price = Double.parseDouble(priceStr);

                String userId = sessionManager.getUserId();
                if (userId == null) {
                    Log.e(TAG, "showCreateOrderDialog: userId is null");
                    Toast.makeText(requireContext(), "Ошибка: не удалось определить пользователя", Toast.LENGTH_SHORT).show();
                    return;
                }


                String orderId = UUID.randomUUID().toString();
                Log.d(TAG, "Создаем заказ с ID=" + orderId + " для пользователя " + userId);
                
                Order order = new Order(
                    orderId,
                    userId,
                    null, // courierId будет установлен позже
                    fromAddress,
                    toAddress,
                    description,
                    weight,
                    price,
                    "new",
                    new Date().toString()
                );

                boolean result = dbHelper.addOrder(order);
                if (result) {
                    Log.d(TAG, "Заказ успешно создан с ID=" + orderId);
                    Toast.makeText(requireContext(), "Заказ успешно создан", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Не удалось создать заказ с ID=" + orderId);
                    Toast.makeText(requireContext(), "Ошибка при создании заказа", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Ошибка при парсинге чисел", e);
                Toast.makeText(requireContext(), "Пожалуйста, введите корректные числа", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
} 