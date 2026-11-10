package com.cintamani.capture.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

class CoarseLocationProvider(private val context: Context) {

    data class Coordinates(
        val latitude: Double,
        val longitude: Double
    )

    fun getLastKnownCoordinates(): Coordinates? {
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCoarse && !hasFine) {
            return null
        }

        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null

            var bestLocation: Location? = null

            // Prioritaskan NETWORK_PROVIDER untuk lokasi kasar instan (0ms)
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            // Fallback ke PASSIVE_PROVIDER jika network provider belum memiliki cache
            if (bestLocation == null && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }

            // Fallback terakhir ke GPS_PROVIDER jika ada cache terakhir
            if (bestLocation == null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            bestLocation?.let {
                Coordinates(
                    latitude = Math.round(it.latitude * 10000.0) / 10000.0,
                    longitude = Math.round(it.longitude * 10000.0) / 10000.0
                )
            }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }
}
