package com.day.mate.ui.theme.screens.prayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlin.math.*

@SuppressLint("MissingPermission")
@Composable
fun QiblaCompass(
    modifier: Modifier = Modifier,
    compassSize: Dp = 300.dp
) {
    val context = LocalContext.current

    // ✅ Azimuth (0..360) بعد الفلترة
    var currentAzimuth by remember { mutableFloatStateOf(0f) }

    // ✅ أنيميشن ناعم
    val animatedAzimuth by animateFloatAsState(
        targetValue = currentAzimuth,
        animationSpec = tween(durationMillis = 140),
        label = "CompassAnimation"
    )

    // ✅ اتجاه القبلة (0..360)
    var qiblaDirection by remember { mutableFloatStateOf(0f) }

    // ✅ Location + Magnetic declination
    var location by remember { mutableStateOf<Location?>(null) }
    var geomagneticField by remember { mutableStateOf<GeomagneticField?>(null) }

    val hasLocationPermission = remember {
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    // =========================
    // 1) LOCATION
    // =========================
    DisposableEffect(context, hasLocationPermission) {
        if (!hasLocationPermission) {
            // ✅ لازم نرجع onDispose دايمًا
            return@DisposableEffect onDispose { }
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                location = loc
                geomagneticField = GeomagneticField(
                    loc.latitude.toFloat(),
                    loc.longitude.toFloat(),
                    loc.altitude.toFloat(),
                    System.currentTimeMillis()
                )
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            val last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (last != null) {
                location = last
                geomagneticField = GeomagneticField(
                    last.latitude.toFloat(),
                    last.longitude.toFloat(),
                    last.altitude.toFloat(),
                    System.currentTimeMillis()
                )
            }

            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10_000L, 10f, listener)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10_000L, 10f, listener)
        } catch (_: Exception) {}

        onDispose {
            try { lm.removeUpdates(listener) } catch (_: Exception) {}
        }
    }

    // =========================
    // 2) QIBLA BEARING (stable)
    // =========================
    LaunchedEffect(location) {
        val loc = location ?: return@LaunchedEffect

        val kaabaLat = Math.toRadians(21.4225)
        val kaabaLon = Math.toRadians(39.8262)

        val userLat = Math.toRadians(loc.latitude)
        val userLon = Math.toRadians(loc.longitude)

        val dLon = kaabaLon - userLon

        val y = sin(dLon) * cos(kaabaLat)
        val x = cos(userLat) * sin(kaabaLat) - sin(userLat) * cos(kaabaLat) * cos(dLon)

        val bearing = Math.toDegrees(atan2(y, x))
        qiblaDirection = ((bearing + 360.0) % 360.0).toFloat()
    }

    // =========================
    // 3) SENSORS
    // =========================
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            // ✅ لازم نرجع onDispose دايمًا
            return@DisposableEffect onDispose { }
        }

        val rotationMatrix = FloatArray(9)
        val adjustedMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                try {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    // ✅ Remap for portrait stability
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        adjustedMatrix
                    )

                    SensorManager.getOrientation(adjustedMatrix, orientation)

                    var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (azimuth + 360f) % 360f

                    geomagneticField?.let {
                        azimuth = (azimuth + it.declination + 360f) % 360f
                    }

                    // ✅ Circular low-pass
                    val alpha = 0.12f
                    val diff = ((azimuth - currentAzimuth + 540f) % 360f) - 180f
                    currentAzimuth = (currentAzimuth + alpha * diff + 360f) % 360f
                } catch (_: Exception) {}
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            try { sensorManager.unregisterListener(listener) } catch (_: Exception) {}
        }
    }

    // =========================
    // 4) UI
    // =========================
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            "Qibla Direction",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.size(compassSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2.6f

                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = radius + 28f,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = radius + 8f,
                    center = center
                )

                // North reference
                rotate(-animatedAzimuth) {
                    drawLine(
                        color = Color.Red,
                        start = center,
                        end = Offset(center.x, center.y - radius),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }

                // Qibla arrow
                rotate(-animatedAzimuth + qiblaDirection) {
                    drawLine(
                        color = Color(0xFFFFD700),
                        start = center,
                        end = Offset(center.x, center.y - radius),
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )

                    val head = Offset(center.x, center.y - radius)
                    val left = Offset(head.x - 18f, head.y + 26f)
                    val right = Offset(head.x + 18f, head.y + 26f)
                    drawLine(Color(0xFFFFD700), head, left, strokeWidth = 10f, cap = StrokeCap.Round)
                    drawLine(Color(0xFFFFD700), head, right, strokeWidth = 10f, cap = StrokeCap.Round)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = if (location != null)
                "Heading: ${currentAzimuth.toInt()}° | Qibla: ${qiblaDirection.toInt()}°"
            else
                "Locating...",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
