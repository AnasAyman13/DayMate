@file:OptIn(ExperimentalMaterial3Api::class)

package com.day.mate.ui.theme.screens.prayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.day.mate.R
import kotlin.math.*

@SuppressLint("MissingPermission")
@Composable
fun QiblaCompass(
    modifier: Modifier = Modifier,
    compassSize: Dp = 280.dp
) {
    val context = LocalContext.current
    val hasPermission = remember {
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    var qiblaDirection by remember { mutableFloatStateOf(0f) }
    var currentAzimuth by remember { mutableFloatStateOf(0f) }
    var location by remember { mutableStateOf<Location?>(null) }
    var isSensorUnreliable by remember { mutableStateOf(false) }
    var isLocationKnown by remember { mutableStateOf(false) }

    // 1. منطق الموقع (توفير البطارية)
    DisposableEffect(Unit) {
        if (hasPermission) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locListener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    if (loc.accuracy < 50f) {
                        location = loc
                        qiblaDirection = calculateQiblaBearing(loc)
                        isLocationKnown = true
                        lm.removeUpdates(this) // إيقاف لتوفير البطارية
                    }
                }
                override fun onProviderDisabled(p: String) {}
                override fun onProviderEnabled(p: String) {}
            }
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 5f, locListener)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 5f, locListener)
            } catch (e: Exception) { e.printStackTrace() }
            onDispose { lm.removeUpdates(locListener) }
        } else onDispose {}
    }

    // 2. منطق الحساسات (تنعيم الحركة)
    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            val alpha = 0.15f
            var smoothedAzimuth = 0f
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rMat = FloatArray(9)
                    val orient = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(rMat, event.values)
                    SensorManager.getOrientation(rMat, orient)
                    var azimuth = Math.toDegrees(orient[0].toDouble()).toFloat()
                    azimuth = (azimuth + 360) % 360
                    location?.let {
                        val geo = GeomagneticField(it.latitude.toFloat(), it.longitude.toFloat(), it.altitude.toFloat(), System.currentTimeMillis())
                        azimuth += geo.declination
                    }
                    smoothedAzimuth = smoothedAzimuth + alpha * (azimuth - smoothedAzimuth)
                    currentAzimuth = smoothedAzimuth
                    isSensorUnreliable = event.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {
                isSensorUnreliable = a < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
            }
        }
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }

    val animatedAzimuth by animateFloatAsState(targetValue = currentAzimuth, animationSpec = tween(200), label = "")

    // UI
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!hasPermission) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFB00020))) {
                Text("برجاء تفعيل الموقع بدقة عالية لتحديد القبلة", modifier = Modifier.padding(16.dp), color = Color.White)
            }
        } else {
            InfoCapsule(text = if(isLocationKnown) "تم تحديد الموقع بدقة" else "جاري تحسين دقة الموقع...")
        }

        Spacer(Modifier.height(30.dp))

        Box(modifier = Modifier.size(compassSize), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2
                rotate(-animatedAzimuth) {
                    drawCircle(Color.White.copy(0.1f), radius, style = Stroke(2f))
                    for (i in 0 until 360 step 30) {
                        val angleRad = Math.toRadians(i.toDouble() - 90)
                        val lineLen = if (i % 90 == 0) 30f else 15f
                        val start = Offset(center.x + (radius - lineLen) * cos(angleRad).toFloat(), center.y + (radius - lineLen) * sin(angleRad).toFloat())
                        val end = Offset(center.x + radius * cos(angleRad).toFloat(), center.y + radius * sin(angleRad).toFloat())
                        drawLine(Color.White, start, end, strokeWidth = if (i % 90 == 0) 4f else 2f)
                    }
                }
            }

            if (isLocationKnown) {
                val qiblaRotation = (qiblaDirection - animatedAzimuth + 540) % 360 - 180
                val animatedQibla by animateFloatAsState(targetValue = qiblaRotation, label = "")
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    rotate(animatedQibla) {
                        val path = Path().apply {
                            moveTo(center.x, center.y - radius + 20)
                            lineTo(center.x - 20, center.y - radius + 70)
                            lineTo(center.x + 20, center.y - radius + 70)
                            close()
                        }
                        drawPath(path, Color(0xFFFFD700))
                    }
                }
            }

            if (isSensorUnreliable) {
                Icon(Icons.Default.Warning, "Calibrate", tint = Color.Yellow, modifier = Modifier.align(Alignment.BottomEnd).size(32.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        if (isLocationKnown) {
            Text(text = "القبلة على زاوية: ${qiblaDirection.toInt()}°", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// 🔥 الدالة التي كانت ناقصة وتسببت في الخطأ
@Composable
fun InfoCapsule(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.border(1.dp, Color.White.copy(0.5f), RoundedCornerShape(50)),
        shape = RoundedCornerShape(50),
        color = Color.White.copy(0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

fun calculateQiblaBearing(loc: Location): Float {
    val kaabaLat = Math.toRadians(21.422487)
    val kaabaLon = Math.toRadians(39.826206)
    val userLat = Math.toRadians(loc.latitude)
    val userLon = Math.toRadians(loc.longitude)
    val y = sin(kaabaLon - userLon)
    val x = cos(userLat) * tan(kaabaLat) - sin(userLat) * cos(kaabaLon - userLon)
    return (Math.toDegrees(atan2(y, x)).toFloat() + 360) % 360
}