package com.day.mate.ui.theme.screens.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlin.math.*

@SuppressLint("MissingPermission")
@Composable
fun QiblaCompass() {
    val context = LocalContext.current
    var azimuth by remember { mutableStateOf(0f) }
    var qiblaDirection by remember { mutableStateOf(0f) }
    var location by remember { mutableStateOf<Location?>(null) }

    // ✅ الحساسات
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // ✅ الموقع الحالي
    LaunchedEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
    }

    // ✅ حساب اتجاه القبلة
    location?.let {
        val kaabaLat = Math.toRadians(21.4225)
        val kaabaLon = Math.toRadians(39.8262)
        val userLat = Math.toRadians(it.latitude)
        val userLon = Math.toRadians(it.longitude)
        val dLon = kaabaLon - userLon
        val y = sin(dLon)
        val x = cos(userLat) * tan(kaabaLat) - sin(userLat) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))
        qiblaDirection = ((bearing + 360) % 360).toFloat()
    }

    // ✅ واجهة المستخدم
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Qibla Direction", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val arrowLength = size.minDimension / 2.5f

                rotate(-azimuth + qiblaDirection) {
                    drawLine(
                        color = Color.Yellow,
                        start = center,
                        end = Offset(center.x, center.y - arrowLength),
                        strokeWidth = 8f
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = location?.let { "Lat: %.4f, Lon: %.4f".format(it.latitude, it.longitude) } ?: "Getting location...",
            color = Color.White
        )
    }
}
