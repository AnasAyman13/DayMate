package com.day.mate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // ✅ استدعاء مهم للأيقونة
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.day.mate.R
import com.day.mate.ui.theme.screens.prayer.PrayerViewModel
import com.day.mate.ui.theme.screens.prayer.cancelAdhanSchedule
import com.day.mate.ui.theme.screens.prayer.checkExactAlarmPermission
import com.day.mate.ui.theme.screens.prayer.getAdhanPref
import com.day.mate.ui.theme.screens.prayer.saveAdhanPref
import com.day.mate.ui.theme.screens.prayer.scheduleAdhan
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// 1. Helper Functions & Permissions
// ==========================================

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ActivityCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ActivityCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@SuppressLint("MissingPermission")
@Composable
fun PrayerScreen(viewModel: PrayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val timings by viewModel.timings.collectAsState()
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scroll = rememberScrollState()

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF042825), Color(0xFF073B3A)))

    var locationPermissionGranted by remember { mutableStateOf(hasLocationPermission(ctx)) }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationPermissionGranted = hasLocationPermission(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) viewModel.loadPrayerTimes(ctx = ctx)
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.loadPrayerTimes(ctx = ctx) }

    LaunchedEffect(locationPermissionGranted) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.loadPrayerTimes(ctx = ctx)
        }
    }

    // Time Ticker
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            nowMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    fun timeStrToNextMillis(time24: String): Long? {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val t = sdf.parse(time24) ?: return null
            val calNow = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val calT = Calendar.getInstance().apply {
                time = t
                set(Calendar.YEAR, calNow.get(Calendar.YEAR))
                set(Calendar.MONTH, calNow.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, calNow.get(Calendar.DAY_OF_MONTH))
            }
            var millis = calT.timeInMillis
            if (millis <= calNow.timeInMillis) millis += 24 * 3600 * 1000L
            millis
        } catch (_: Exception) { null }
    }

    val nextPrayerPair: Pair<String, Long>? = remember(timings, nowMillis) {
        timings?.let { t ->
            val list = listOfNotNull(
                "Fajr" to timeStrToNextMillis(t.Fajr),
                "Dhuhr" to timeStrToNextMillis(t.Dhuhr),
                "Asr" to timeStrToNextMillis(t.Asr),
                "Maghrib" to timeStrToNextMillis(t.Maghrib),
                "Isha" to timeStrToNextMillis(t.Isha)
            ).mapNotNull { if (it.second != null) it.first to it.second!! else null }
            list.minByOrNull { it.second }
        }
    }

    val remainingMillis = (nextPrayerPair?.second?.minus(nowMillis) ?: 0L).coerceAtLeast(0L)
    val remH = (remainingMillis / 3600000).toInt()
    val remM = ((remainingMillis % 3600000) / 60000).toInt()
    val remS = ((remainingMillis % 60000) / 1000).toInt()

    val adhanEnabled = remember {
        mutableStateMapOf(
            "Fajr" to getAdhanPref(ctx, "Fajr"),
            "Dhuhr" to getAdhanPref(ctx, "Dhuhr"),
            "Asr" to getAdhanPref(ctx, "Asr"),
            "Maghrib" to getAdhanPref(ctx, "Maghrib"),
            "Isha" to getAdhanPref(ctx, "Isha")
        )
    }

    LaunchedEffect(timings) {
        timings?.let {
            adhanEnabled.forEach { (prayerName, isEnabled) ->
                if (isEnabled) {
                    val timeStr = when (prayerName) {
                        "Fajr" -> it.Fajr
                        "Dhuhr" -> it.Dhuhr
                        "Asr" -> it.Asr
                        "Maghrib" -> it.Maghrib
                        "Isha" -> it.Isha
                        else -> null
                    }
                    val timeMillis = timeStr?.let { str -> timeStrToNextMillis(str) }
                    if (timeMillis != null) {
                        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
                        scheduleAdhan(ctx, prayerName, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                    }
                }
            }
        }
    }

    val hijriStr = remember { getHijriDateSafely(ctx) }

    // ==========================================
    // 2. Compass & Location Logic
    // ==========================================

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var deviceAzimuth by remember { mutableFloatStateOf(0f) }
    var qiblaBearing by remember { mutableFloatStateOf(0f) }

    DisposableEffect(locationPermissionGranted) {
        if (!locationPermissionGranted) {
            onDispose { }
            return@DisposableEffect onDispose { }
        }
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) { userLocation = loc }
            override fun onProviderDisabled(p: String) {}
            override fun onProviderEnabled(p: String) {}
            @Deprecated("Deprecated") override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
        }
        try {
            val lastGPS = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLastLocation = lastGPS ?: lastNet
            if (bestLastLocation != null) userLocation = bestLastLocation

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 10f, listener)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 10f, listener)
        } catch (e: Exception) { e.printStackTrace() }

        onDispose {
            try { lm.removeUpdates(listener) } catch (_: Exception) {}
        }
    }

    DisposableEffect(ctx) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val listener = object : SensorEventListener {
            val rMat = FloatArray(9)
            val orientation = FloatArray(3)
            var lastAccent = FloatArray(3)
            var lastMag = FloatArray(3)
            var isLastAccentSet = false
            var isLastMagSet = false

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rMat, event.values)
                    var azimuth = (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0].toDouble()) + 360) % 360
                    deviceAzimuth = azimuth.toFloat()
                } else {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, lastAccent, 0, event.values.size)
                        isLastAccentSet = true
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, lastMag, 0, event.values.size)
                        isLastMagSet = true
                    }
                    if (isLastAccentSet && isLastMagSet) {
                        SensorManager.getRotationMatrix(rMat, null, lastAccent, lastMag)
                        var azimuth = (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0].toDouble()) + 360) % 360
                        deviceAzimuth = azimuth.toFloat()
                    }
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }

        if (sensor != null) {
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            sm.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose { sm.unregisterListener(listener) }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { loc ->
            val lat1 = Math.toRadians(loc.latitude)
            val lon1 = Math.toRadians(loc.longitude)
            val lat2 = Math.toRadians(21.4225) // Kaaba
            val lon2 = Math.toRadians(39.8262)
            val dLon = lon2 - lon1
            val y = sin(dLon) * cos(lat2)
            val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
            val bearing = Math.toDegrees(atan2(y, x))
            qiblaBearing = ((bearing + 360.0) % 360.0).toFloat()
        }
    }

    // ==========================================
    // 3. UI Implementation
    // ==========================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hijriStr,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // ==========================================
            // ✅ قسم الصلاة القادمة
            // ==========================================

            // تعريف المتغير قبل الاستخدام
            val nextPrayerName = when (nextPrayerPair?.first) {
                "Fajr" -> stringResource(R.string.fajr)
                "Dhuhr" -> stringResource(R.string.dhuhr)
                "Asr" -> stringResource(R.string.asr)
                "Maghrib" -> stringResource(R.string.maghrib)
                "Isha" -> stringResource(R.string.isha)
                else -> stringResource(R.string.loading)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFC6A000))
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nextPrayerName,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF3E1F00)
                            )
                            Text(
                                String.format("%02d:%02d:%02d", remH, remM, remS),
                                color = Color(0xFF2C1A00)
                            )
                        }

                        // ✅ هنا استبدلنا الرسم بالـ Icon الجاهز
                        // تأكد إن اسم الصورة عندك في drawable هو ic_Mosque
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mosque),
                            contentDescription = null,
                            tint = Color(0xFF3E1F00), // نفس لون النصوص البني
                            modifier = Modifier
                                .size(50.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Prayer List
            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())

            timings?.let { t ->
                listOf(
                    "Fajr" to t.Fajr, "Dhuhr" to t.Dhuhr, "Asr" to t.Asr, "Maghrib" to t.Maghrib, "Isha" to t.Isha
                ).forEach { (name, timeStr) ->
                    val formatted = try { sdf12.format(sdf24.parse(timeStr)!!) } catch (_: Exception) { timeStr }
                    val timeMillis = timeStrToNextMillis(timeStr)
                    val translatedName = stringResource(when (name) {
                        "Fajr" -> R.string.fajr
                        "Dhuhr" -> R.string.dhuhr
                        "Asr" -> R.string.asr
                        "Maghrib" -> R.string.maghrib
                        "Isha" -> R.string.isha
                        else -> R.string.loading
                    })
                    PrayerRow(name = translatedName, time = formatted, enabled = adhanEnabled[name] == true) { checked ->
                        adhanEnabled[name] = checked
                        saveAdhanPref(ctx, name, checked)
                        if (timeMillis != null) {
                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkExactAlarmPermission(ctx)) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", ctx.packageName, null)
                                }
                                settingsLauncher.launch(intent)
                                Toast.makeText(ctx, ctx.getString(R.string.exact_alarm_permission_needed), Toast.LENGTH_LONG).show()
                            } else if (checked) {
                                val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
                                scheduleAdhan(ctx, name, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                            } else {
                                cancelAdhanSchedule(ctx, name)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ==========================================
            // ✅ قسم الأذكار (Carousel)
            // ==========================================
            val athkarList = remember {
                listOf(
                    "اللهم صلِّ على سيِّدنا محمدَ ﷺ",
                    "سبحان الله وبحمده، سبحان الله العظيم",
                    "أستغفر الله العظيم وأتوب إليه",
                    "لا إله إلا الله وحدَه لا شريك له",
                    "لا حول ولا قوة إلا بالله العلي العظيم",
                    "اللهم أعني على ذكرك وشكرك وحسن عبادتك",
                    "رضيت بالله رباً، وبالإسلام ديناً، وبمحمد ﷺ نبياً",
                    "سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر",
                    "يا حي يا قيوم برحمتك أستغيث",
                    "اللهم إنك عفو تحب العفو فاعفُ عني",
                    "حسبي الله لا إله إلا هو عليه توكلت"
                )
            }
            var athkarIndex by remember { mutableIntStateOf(0) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            athkarIndex = if (athkarIndex - 1 < 0) athkarList.lastIndex else athkarIndex - 1
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color(0xFF4B2E00)
                        )
                    }

                    Box(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                        AnimatedContent(
                            targetState = athkarIndex,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) + slideInHorizontally { width -> width / 4 } togetherWith
                                        fadeOut(animationSpec = tween(300)) + slideOutHorizontally { width -> -width / 4 }
                            },
                            label = "AthkarAnimation"
                        ) { targetIndex ->
                            Text(
                                text = athkarList[targetIndex],
                                color = Color(0xFF4B2E00),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            athkarIndex = (athkarIndex + 1) % athkarList.size
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = Color(0xFF4B2E00)
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // ==========================================
            // ✅ قسم البوصلة
            // ==========================================
            Text(
                stringResource(R.string.qibla_direction),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            val qiblaRotation = qiblaBearing - deviceAzimuth
            val normalizedRotation = (qiblaRotation + 540) % 360 - 180

            val animatedAngle by animateFloatAsState(
                targetValue = normalizedRotation,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
                label = "CompassNeedle"
            )

            val isAligned = kotlin.math.abs(normalizedRotation) < 5.0

            var hasVibrated by remember { mutableStateOf(false) }
            if (isAligned && !hasVibrated) {
                LaunchedEffect(Unit) {
                    val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        v.vibrate(100)
                    }
                    hasVibrated = true
                }
            } else if (!isAligned) {
                hasVibrated = false
            }

            val glowColor by animateColorAsState(
                targetValue = if (isAligned) Color(0xFF00FF9D) else Color(0xFFFFD700),
                animationSpec = tween(300),
                label = "GlowColor"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF072E2C), Color(0xFF021615)),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    drawCircle(
                        color = Color(0xFFC6A000),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 6f)
                    )

                    rotate(-deviceAzimuth) {
                        for (i in 0 until 360 step 30) {
                            val angleRad = Math.toRadians(i.toDouble() - 90)
                            val startR = radius - 20
                            val endR = radius - 10
                            val start = Offset(
                                center.x + startR * cos(angleRad).toFloat(),
                                center.y + startR * sin(angleRad).toFloat()
                            )
                            val end = Offset(
                                center.x + endR * cos(angleRad).toFloat(),
                                center.y + endR * sin(angleRad).toFloat()
                            )
                            val isCardinal = i % 90 == 0
                            drawLine(
                                color = if (isCardinal) Color(0xFFFFFFFF) else Color(0xFF808080),
                                start = start,
                                end = end,
                                strokeWidth = if (isCardinal) 4f else 2f
                            )
                        }

                        val textPaint = Paint().asFrameworkPaint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 40f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        drawIntoCanvas {
                            it.nativeCanvas.drawText("N", center.x, center.y - radius + 55, textPaint)
                        }
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    rotate(animatedAngle) {
                        if (isAligned) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(glowColor.copy(alpha = 0.6f), Color.Transparent),
                                    center = Offset(center.x, center.y - radius + 70),
                                    radius = 60f
                                ),
                                center = Offset(center.x, center.y - radius + 70),
                                radius = 60f
                            )
                        }

                        val arrowPath = Path().apply {
                            moveTo(center.x, center.y - radius + 30)
                            lineTo(center.x + 20, center.y)
                            lineTo(center.x, center.y - 20)
                            lineTo(center.x - 20, center.y)
                            close()
                        }

                        drawPath(path = arrowPath, color = glowColor)

                        drawLine(
                            color = glowColor.copy(alpha = 0.5f),
                            start = center,
                            end = Offset(center.x, center.y - radius + 30),
                            strokeWidth = 4f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                    drawCircle(color = glowColor, radius = 8f, center = center)
                }

                if (userLocation == null) {
                    Text("جاري تحديد الموقع...", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            val directionText = when {
                isAligned -> stringResource(R.string.facing_qibla)
                normalizedRotation > 0 -> stringResource(R.string.turn_right)
                else -> stringResource(R.string.turn_left)
            }

            Text(
                text = directionText,
                color = if (isAligned) Color(0xFF00FF9D) else Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

// ... PrayerRow and getHijriDateSafely ...
@Composable
fun PrayerRow(name: String, time: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(time, color = Color.Gray)
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

fun getHijriDateSafely(ctx: Context): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val islamic = android.icu.util.IslamicCalendar()
            val day = islamic.get(android.icu.util.Calendar.DAY_OF_MONTH)
            val month = ctx.getString(listOf(
                R.string.muharram, R.string.safar, R.string.rabi_al_awwal, R.string.rabi_al_thani,
                R.string.jumada_al_awwal, R.string.jumada_al_thani, R.string.rajab, R.string.shaaban,
                R.string.ramadan, R.string.shawwal, R.string.dhu_al_qidah, R.string.dhu_al_hijjah
            )[islamic.get(android.icu.util.Calendar.MONTH)])
            "$day $month ${islamic.get(android.icu.util.Calendar.YEAR)}"
        } else {
            SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(Date())
        }
    } catch (_: Exception) { "—" }
}