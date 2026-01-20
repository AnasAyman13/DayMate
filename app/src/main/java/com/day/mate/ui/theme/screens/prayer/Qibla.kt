package com.day.mate.ui.theme.screens.prayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import kotlin.math.*

@SuppressLint("MissingPermission")
@Composable
fun QiblaCompass(
    modifier: Modifier = Modifier,
    compassSize: Dp = 300.dp
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qibla_prefs", Context.MODE_PRIVATE) }

    // 🔥 1. التأكد من الصلاحية أولاً قبل أي حاجة
    val hasPermission = remember {
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // 🔥 2. تحميل البيانات المحفوظة "فقط" لو فيه صلاحية
    // لو مفيش صلاحية، بنصفر القيم عشان السهم يختفي
    val savedQibla = if (hasPermission) prefs.getFloat("last_qibla", 0f) else 0f
    val savedDist = if (hasPermission) prefs.getInt("last_dist", 0) else 0

    // State Variables
    var qiblaDirection by remember { mutableFloatStateOf(savedQibla) }
    var distanceToKaaba by remember { mutableIntStateOf(savedDist) }
    var currentAzimuth by remember { mutableFloatStateOf(0f) }
    var location by remember { mutableStateOf<Location?>(null) }
    var isSensorUnreliable by remember { mutableStateOf(false) }

    // 🔥 3. تحديد هل الموقع معروف بناءً على الصلاحية والبيانات
    var isLocationKnown by remember { mutableStateOf(hasPermission && savedQibla != 0f) }

    // Compass Animation
    val animatedAzimuth by animateFloatAsState(
        targetValue = currentAzimuth,
        animationSpec = tween(durationMillis = 200),
        label = "CompassSmoother"
    )

    // تشغيل الـ Location Updates
    DisposableEffect(Unit) {
        if (hasPermission) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locListener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    location = loc
                    val (bearing, dist) = calculateQiblaData(loc)
                    qiblaDirection = bearing
                    distanceToKaaba = dist
                    isLocationKnown = true // الموقع اتعرف خلاص

                    // حفظ البيانات الجديدة
                    prefs.edit()
                        .putFloat("last_qibla", bearing)
                        .putInt("last_dist", dist)
                        .apply()
                }
                @Deprecated("Deprecated") override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                override fun onProviderEnabled(p: String) {}
                override fun onProviderDisabled(p: String) {}
            }

            try {
                val lastGPS = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val bestLoc = lastGPS ?: lastNet

                if (bestLoc != null) {
                    location = bestLoc
                    val (bearing, dist) = calculateQiblaData(bestLoc)
                    qiblaDirection = bearing
                    distanceToKaaba = dist
                    isLocationKnown = true
                }

                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 10f, locListener)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 10f, locListener)
            } catch (e: Exception) { e.printStackTrace() }

            onDispose { lm.removeUpdates(locListener) }
        } else {
            // ⛔ إذا مفيش صلاحية، بنخلي الموقع غير معروف فوراً
            isLocationKnown = false
            qiblaDirection = 0f
            onDispose { }
        }
    }

    // Sensor Logic (زي ما هو)
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (azimuth + 360) % 360

                    // تصحيح الانحراف المغناطيسي لو الموقع معروف
                    if (isLocationKnown && location != null) {
                        val geoField = GeomagneticField(
                            location!!.latitude.toFloat(), location!!.longitude.toFloat(),
                            location!!.altitude.toFloat(), System.currentTimeMillis()
                        )
                        azimuth += geoField.declination
                    }

                    var delta = azimuth - currentAzimuth
                    while (delta < -180) delta += 360
                    while (delta > 180) delta -= 360
                    currentAzimuth += delta

                    isSensorUnreliable = (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || event.accuracy == 0)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                isSensorUnreliable = (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
            }
        }

        if (sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // UI Drawing
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF042825), Color(0xFF073B3A))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 التحكم في الرسائل بناءً على الصلاحية والموقع
        if (!hasPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB00020)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOff, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "تم رفض الصلاحية. لا يمكن تحديد القبلة.",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            InfoCapsule(
                text = if(isLocationKnown) formatCoordinates(location) else "جاري البحث عن الموقع..."
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLocationKnown && hasPermission) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoBox(
                    modifier = Modifier.weight(1f),
                    value = "$distanceToKaaba KM",
                    label = "البعد عن الكعبة",
                    icon = "🕋"
                )
                InfoBox(
                    modifier = Modifier.weight(1f),
                    value = String.format("%.1f°", qiblaDirection),
                    label = "زاوية القبلة",
                    icon = "📐"
                )
            }
        } else {
            // مكان فاضي أو رسالة انتظار
            Box(modifier = Modifier.height(90.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val msg = if(!hasPermission) "الخدمة متوقفة لعدم وجود صلاحية" else "في انتظار إشارة GPS..."
                Text(msg, color = Color.White.copy(alpha = 0.5f))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Compass Draw
        Box(
            modifier = Modifier.size(compassSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Background
                drawCircle(Color(0xFFF5F5F5), radius)
                drawCircle(Color.Black, radius, style = androidx.compose.ui.graphics.drawscope.Stroke(4f))

                // Compass Ticks & North (Always visible)
                rotate(-animatedAzimuth) {
                    for (i in 0 until 360 step 30) {
                        val angle = Math.toRadians(i.toDouble())
                        val isCardinal = i % 90 == 0
                        val startLen = if (isCardinal) 30f else 15f

                        val start = Offset(
                            (center.x + (radius - startLen) * sin(angle)).toFloat(),
                            (center.y - (radius - startLen) * cos(angle)).toFloat()
                        )
                        val end = Offset(
                            (center.x + radius * sin(angle)).toFloat(),
                            (center.y - radius * cos(angle)).toFloat()
                        )
                        drawLine(
                            color = if (isCardinal) Color.Black else Color.Gray,
                            start = start,
                            end = end,
                            strokeWidth = if (isCardinal) 5f else 2f,
                            cap = StrokeCap.Round
                        )
                    }

                    // North Indicator
                    drawLine(
                        color = Color.Red,
                        start = center,
                        end = Offset(center.x, center.y - radius + 40),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }

                // 🔥🔥 القبلة (ترسم فقط لو فيه بيرميشن + موقع معروف) 🔥🔥
                if (isLocationKnown && hasPermission) {
                    rotate(-animatedAzimuth + qiblaDirection) {
                        val arrowPath = Path().apply {
                            moveTo(center.x, center.y - radius + 60)
                            lineTo(center.x - 18, center.y)
                            lineTo(center.x + 18, center.y)
                            close()
                        }
                        drawPath(arrowPath, Color(0xFFFFD700))

                        drawLine(
                            color = Color(0xFFFFD700),
                            start = center,
                            end = Offset(center.x, center.y - radius + 60),
                            strokeWidth = 12f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Center Dot
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Red, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )

            // Warning Icons
            if (!hasPermission) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = "No Permission",
                    tint = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .background(Color.White.copy(alpha=0.8f), CircleShape)
                        .padding(12.dp)
                )
            } else if (isSensorUnreliable) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Calibrate",
                    tint = Color.Red,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .size(36.dp)
                        .background(Color.White, CircleShape)
                        .padding(6.dp)
                )
            }
        }
    }
}

// Helpers (زي ما هما)
@Composable
fun InfoCapsule(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.border(1.dp, Color.Black, RoundedCornerShape(50)),
        shape = RoundedCornerShape(50),
        color = Color.White
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
    }
}

@Composable
fun InfoBox(modifier: Modifier = Modifier, value: String, label: String, icon: String) {
    Surface(
        modifier = modifier
            .height(90.dp)
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

fun calculateQiblaData(loc: Location): Pair<Float, Int> {
    val kaabaLat = 21.422487
    val kaabaLon = 39.826206
    val userLatRad = Math.toRadians(loc.latitude)
    val kaabaLatRad = Math.toRadians(kaabaLat)
    val lonDiff = Math.toRadians(kaabaLon - loc.longitude)
    val y = sin(lonDiff) * cos(kaabaLatRad)
    val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(lonDiff)
    var bearing = Math.toDegrees(atan2(y, x)).toFloat()
    bearing = (bearing + 360) % 360

    val results = FloatArray(1)
    Location.distanceBetween(loc.latitude, loc.longitude, kaabaLat, kaabaLon, results)
    return Pair(bearing, (results[0] / 1000).toInt())
}

fun formatCoordinates(loc: Location?): String {
    if (loc == null) return "موقع محفوظ"
    fun toDMS(v: Double): String {
        val d = abs(v).toInt()
        val m = ((abs(v) - d) * 60).toInt()
        val s = ((abs(v) - d - m / 60.0) * 3600).toInt()
        return "$d°$m'$s\""
    }
    val lat = toDMS(loc.latitude) + if (loc.latitude >= 0) " N" else " S"
    val lon = toDMS(loc.longitude) + if (loc.longitude >= 0) " E" else " W"
    return "$lat  $lon"
}

@Preview
@Composable
fun QiblaPreview() {
    QiblaCompass()
}