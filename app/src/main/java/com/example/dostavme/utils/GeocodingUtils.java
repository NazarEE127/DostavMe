package com.example.dostavme.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodingUtils {
    private static final String TAG = "GeocodingUtils";

    public static String getAddressFromLocation(Context context, LatLng location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
            );
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }
                return sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address from location", e);
        }
        return null;
    }

    public static LatLng getLocationFromAddress(Context context, String address) {
        Log.d(TAG, "Попытка получить координаты для адреса: " + address);
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                LatLng result = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "Координаты получены: " + result.latitude + ", " + result.longitude);
                return result;
            } else {
                Log.e(TAG, "Не найдено адресов для: " + address);
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при получении координат для адреса: " + address, e);
        }
        return null;
    }

    public static double calculateDistance(LatLng from, LatLng to) {
        if (from == null || to == null) {
            return 0;
        }

        double earthRadius = 6371;

        double lat1 = Math.toRadians(from.latitude);
        double lat2 = Math.toRadians(to.latitude);
        double lon1 = Math.toRadians(from.longitude);
        double lon2 = Math.toRadians(to.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return earthRadius * c;
    }
} 