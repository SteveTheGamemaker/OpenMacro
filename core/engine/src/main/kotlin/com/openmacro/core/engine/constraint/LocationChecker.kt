package com.openmacro.core.engine.constraint

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.openmacro.core.model.config.LocationConstraintConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LocationChecker @Inject constructor() : ConstraintChecker {
    override val constraintTypeId = "location"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<LocationConstraintConfig>(configJson)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        @Suppress("DEPRECATION")
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: return false

        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            location.latitude, location.longitude,
            config.latitude, config.longitude,
            results
        )

        return results[0] <= config.radiusMeters
    }
}
