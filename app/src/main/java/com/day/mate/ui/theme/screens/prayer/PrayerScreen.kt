package com.day.mate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import kotlin.math.abs
import kotlin.math.tan

// ==========================================
// 1. Helper Functions
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

// ✅ جديد: هل الـ GPS / Location service شغال؟
private fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        lm.isLocationEnabled
    } else {
        try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {
            false
        }
    }
}

// ✅ جديد: يفتح صفحة إعدادات التطبيق (ومنها Permissions)
private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

// دالة حساب القبلة الدقيقة
private fun calculateQiblaBearingInternal(loc: Location): Float {
    val kaabaLat = Math.toRadians(21.422487)
    val kaabaLon = Math.toRadians(39.826206)
    val userLat = Math.toRadians(loc.latitude)
    val userLon = Math.toRadians(loc.longitude)
    val y = sin(kaabaLon - userLon)
    val x = cos(userLat) * tan(kaabaLat) - sin(userLat) * cos(kaabaLon - userLon)
    return (Math.toDegrees(atan2(y, x)).toFloat() + 360) % 360
}

@SuppressLint("MissingPermission")
@Composable
fun PrayerScreen(viewModel: PrayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val timings by viewModel.timings.collectAsState()
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isArabic = LocalConfiguration.current.locales[0].language == "ar"

    // ✅ 1. تعريف حالة السكرول
    val scroll = rememberScrollState()

    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF042825), Color(0xFF073B3A)))

    var locationPermissionGranted by remember { mutableStateOf(hasLocationPermission(ctx)) }

    // ✅ جديد
    var locationEnabled by remember { mutableStateOf(isLocationEnabled(ctx)) }
    var showPermissionHelpDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationPermissionGranted = hasLocationPermission(ctx)
                locationEnabled = isLocationEnabled(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        locationEnabled = isLocationEnabled(ctx)

        // ✅ لو رفض: افتح Dialog يرشدّه للأذونات
        if (!isGranted) {
            showPermissionHelpDialog = true
        } else {
            // لو اتوافق والـGPS شغال
            if (locationEnabled) viewModel.loadPrayerTimes(ctx = ctx)
        }
    }

    // ✅ ده كان عندك — سيبناه للـ exact alarm + إعادة التحميل
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.loadPrayerTimes(ctx = ctx) }

    // ✅ جديد: Launcher لإعدادات الـ Location
    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        locationEnabled = isLocationEnabled(ctx)
        locationPermissionGranted = hasLocationPermission(ctx)
        if (locationPermissionGranted && locationEnabled) {
            viewModel.loadPrayerTimes(ctx = ctx)
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionGranted = hasLocationPermission(ctx)
        locationEnabled = isLocationEnabled(ctx)

        if (!locationPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.loadPrayerTimes(ctx = ctx)
        }
    }

    // ✅ Dialog يرشد المستخدم للأذونات (مش بيكسر حاجة)
    if (showPermissionHelpDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionHelpDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionHelpDialog = false
                    openAppSettings(ctx)
                }) {
                    Text(if (isArabic) "افتح الأذونات" else "Open permissions")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionHelpDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            },
            title = {
                Text(if (isArabic) "تفعيل إذن الموقع" else "Enable location permission")
            },
            text = {
                Text(
                    if (isArabic)
                        "فعّل إذن الموقع من: Permissions > Location\nعلشان نقدر نعرض اتجاه القبلة."
                    else
                        "Enable location permission from: Permissions > Location\nso we can show Qibla direction."
                )
            }
        )
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
                timeStrToNextMillis(t.Fajr)?.let { "Fajr" to it },
                timeStrToNextMillis(t.Dhuhr)?.let { "Dhuhr" to it },
                timeStrToNextMillis(t.Asr)?.let { "Asr" to it },
                timeStrToNextMillis(t.Maghrib)?.let { "Maghrib" to it },
                timeStrToNextMillis(t.Isha)?.let { "Isha" to it }
            )
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

    val hijriStr = remember { getHijriDateSafely(ctx) }

    // ==========================================
    // 2. Compass & Location Logic
    // ==========================================

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var deviceAzimuth by remember { mutableFloatStateOf(0f) }
    var qiblaBearing by remember { mutableFloatStateOf(0f) }
    var isGPSPrecise by remember { mutableStateOf(false) }

    DisposableEffect(locationPermissionGranted, locationEnabled) {
        if (!locationPermissionGranted || !locationEnabled) {
            onDispose { }
            return@DisposableEffect onDispose { }
        }
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                if (loc.accuracy < 100f) {
                    userLocation = loc
                    isGPSPrecise = true
                    qiblaBearing = calculateQiblaBearingInternal(loc)
                    if(loc.accuracy < 30f) lm.removeUpdates(this)
                }
            }
            override fun onProviderDisabled(p: String) {}
            override fun onProviderEnabled(p: String) {}
            @Deprecated("Deprecated") override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
        }
        try {
            val lastGPS = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastGPS != null) {
                userLocation = lastGPS
                isGPSPrecise = true
                qiblaBearing = calculateQiblaBearingInternal(lastGPS)
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 5f, listener)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 5f, listener)
        } catch (e: Exception) { e.printStackTrace() }

        onDispose {
            try { lm.removeUpdates(listener) } catch (_: Exception) {}
        }
    }

    DisposableEffect(ctx) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)

        var smoothedAzimuth = 0f
        val alpha = 0.35f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, gravity, 0, 3)
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                }

                val r = FloatArray(9)
                val i = FloatArray(9)

                if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (azimuth + 360) % 360

                    userLocation?.let { loc ->
                        val geoField = GeomagneticField(
                            loc.latitude.toFloat(),
                            loc.longitude.toFloat(),
                            loc.altitude.toFloat(),
                            System.currentTimeMillis()
                        )
                        azimuth += geoField.declination
                    }

                    var diff = azimuth - smoothedAzimuth
                    if (diff > 180) diff -= 360
                    if (diff < -180) diff += 360

                    smoothedAzimuth += diff * alpha
                    smoothedAzimuth = (smoothedAzimuth + 360) % 360

                    deviceAzimuth = smoothedAzimuth
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }

        sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
        sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME)

        onDispose { sm.unregisterListener(listener) }
    }

    // ==========================================
    // 3. UI Implementation
    // ==========================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            // ✅ حماية الحواف في اللاندسكيب (عشان النوتش والبارات)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll) // ✅ السكرول الأساسي
                .padding(horizontal = 16.dp, vertical = 16.dp), // ✅ بادينج موحد
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hijriStr,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // ✅ الكارت الأحمر: يظهر لو permission ناقص أو GPS مقفول
            if (!locationPermissionGranted || !locationEnabled) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB00020)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    onClick = {
                        // تحديث سريع
                        locationPermissionGranted = hasLocationPermission(ctx)
                        locationEnabled = isLocationEnabled(ctx)

                        when {
                            // 1) Permission ناقص
                            !locationPermissionGranted -> {
                                val activity = ctx as? Activity
                                if (activity == null) {
                                    showPermissionHelpDialog = true
                                    return@Card
                                }

                                val canAskAgain = shouldShowRequestPermissionRationale(
                                    activity,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )

                                if (canAskAgain) {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                } else {
                                    // Don’t ask again / رفض كتير → Settings + إرشاد
                                    showPermissionHelpDialog = true
                                }
                            }

                            // 2) Permission موجود بس GPS مقفول
                            !locationEnabled -> {
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                locationSettingsLauncher.launch(intent)
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.TouchApp, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                !locationPermissionGranted && isArabic ->
                                    "فعّل إذن الموقع علشان القبلة — اضغط هنا"
                                !locationPermissionGranted && !isArabic ->
                                    "Enable location permission for Qibla — tap here"
                                locationPermissionGranted && !locationEnabled && isArabic ->
                                    "فعّل GPS علشان القبلة — اضغط هنا"
                                else ->
                                    "Enable GPS for Qibla — tap here"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // كارت الصلاة القادمة (زي ما هو)
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
                            brush = Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFC6A000))),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nextPrayerName, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF3E1F00))
                            Text(String.format("%02d:%02d:%02d", remH, remM, remS), color = Color(0xFF2C1A00))
                        }
                        Icon(painterResource(id = R.drawable.ic_mosque), null, tint = Color(0xFF3E1F00), modifier = Modifier.size(50.dp))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Prayer List
            timings?.let { t ->
                listOf(
                    "Fajr" to t.Fajr, "Dhuhr" to t.Dhuhr, "Asr" to t.Asr, "Maghrib" to t.Maghrib, "Isha" to t.Isha
                ).forEach { (name, timeStr) ->
                    val formatted = try {
                        val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        sdf12.format(sdf24.parse(timeStr)!!)
                    } catch (_: Exception) { timeStr }

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
                        val timeMillis = timeStrToNextMillis(timeStr)
                        if (timeMillis != null) {
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkExactAlarmPermission(ctx)) {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.fromParts("package", ctx.packageName, null)
                                    }
                                    settingsLauncher.launch(intent)
                                } else {
                                    val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
                                    scheduleAdhan(ctx, name, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                                }
                            } else {
                                cancelAdhanSchedule(ctx, name)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Athkar Carousel
            val athkarList = remember {
                listOf("سبحان الله وبحمده", "أستغفر الله العظيم", "لا إله إلا الله", "اللهم صل على محمد", "لا حول ولا قوة إلا بالله")
            }
            var athkarIndex by remember { mutableIntStateOf(0) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { athkarIndex = if (athkarIndex - 1 < 0) athkarList.lastIndex else athkarIndex - 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF4B2E00))
                    }
                    Box(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                        AnimatedContent(targetState = athkarIndex, label = "") { target ->
                            Text(athkarList[target], color = Color(0xFF4B2E00), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    IconButton(onClick = { athkarIndex = (athkarIndex + 1) % athkarList.size }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF4B2E00))
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Compass Section
            Text(
                stringResource(R.string.qibla_direction),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            val qiblaRotation = qiblaBearing - deviceAzimuth
            val normalizedRotation = (qiblaRotation + 540) % 360 - 180

            val targetRotation = normalizedRotation
            val animatedAngle by animateFloatAsState(
                targetValue = targetRotation,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "CompassNeedle"
            )

            val isAligned = kotlin.math.abs(normalizedRotation) < 5.0

            var hasVibrated by remember { mutableStateOf(false) }
            if (isAligned && !hasVibrated) {
                LaunchedEffect(Unit) {
                    val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else { v.vibrate(50) }
                    hasVibrated = true
                }
            } else if (!isAligned) { hasVibrated = false }

            val glowColor by animateColorAsState(
                targetValue = if (isAligned) Color(0xFF00FF9D) else Color(0xFFFFD700),
                animationSpec = tween(300), label = "GlowColor"
            )

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    drawCircle(Brush.radialGradient(listOf(Color(0xFF072E2C), Color(0xFF021615)), center, radius), radius, center)
                    drawCircle(Color(0xFFC6A000), radius, center, style = Stroke(6f))

                    rotate(-deviceAzimuth) {
                        for (i in 0 until 360 step 30) {
                            val angleRad = Math.toRadians(i.toDouble() - 90)
                            val isCardinal = i % 90 == 0
                            val lineLen = if (isCardinal) 25f else 15f
                            val start = Offset(center.x + (radius - lineLen) * cos(angleRad).toFloat(), center.y + (radius - lineLen) * sin(angleRad).toFloat())
                            val end = Offset(center.x + radius * cos(angleRad).toFloat(), center.y + radius * sin(angleRad).toFloat())
                            drawLine(if (isCardinal) Color(0xFFFFFFFF) else Color(0xFF808080), start, end, strokeWidth = if (isCardinal) 4f else 2f)
                        }
                        val paint = Paint().asFrameworkPaint().apply {
                            color = android.graphics.Color.WHITE; textSize = 40f; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true
                        }
                        drawIntoCanvas { it.nativeCanvas.drawText("N", center.x, center.y - radius + 55, paint) }
                    }
                }

                if (locationPermissionGranted && locationEnabled && isGPSPrecise) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        rotate(animatedAngle) {
                            if (isAligned) {
                                drawCircle(
                                    brush = Brush.radialGradient(listOf(glowColor.copy(alpha = 0.6f), Color.Transparent), center = Offset(center.x, center.y - radius + 70), radius = 60f),
                                    center = Offset(center.x, center.y - radius + 70), radius = 60f
                                )
                            }
                            val path = Path().apply {
                                moveTo(center.x, center.y - radius + 30)
                                lineTo(center.x + 20, center.y); lineTo(center.x, center.y - 20); lineTo(center.x - 20, center.y); close()
                            }
                            drawPath(path, glowColor)
                            drawLine(glowColor.copy(alpha = 0.5f), center, Offset(center.x, center.y - radius + 30), strokeWidth = 4f, cap = StrokeCap.Round)
                        }
                        drawCircle(glowColor, 8f, center)
                    }
                } else {
                    // ✅ تعديل النص فقط (ترجمة + GPS)
                    Text(
                        text = when {
                            !locationPermissionGranted && isArabic -> "فعّل إذن الموقع من الأعلى"
                            !locationPermissionGranted && !isArabic -> "Enable location permission from above"
                            locationPermissionGranted && !locationEnabled && isArabic -> "فعّل GPS من الأعلى"
                            locationPermissionGranted && !locationEnabled && !isArabic -> "Enable GPS from above"
                            isArabic -> "جاري تحديد الموقع..."
                            else -> "Getting location..."
                        },
                        color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val directionText = when {
                isAligned -> stringResource(R.string.facing_qibla)
                normalizedRotation > 0 -> stringResource(R.string.turn_right)
                else -> stringResource(R.string.turn_left)
            }
            Text(
                text = if(locationPermissionGranted && locationEnabled && isGPSPrecise) directionText else "",
                color = if (isAligned) Color(0xFF00FF9D) else Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            // ✅ سكرول إضافي في النهاية (عشان اللاندسكيب)
            Spacer(Modifier.height(120.dp))
        }
    }
}

// ... Helper Functions remain the same ...
@Composable
fun PrayerRow(name: String, time: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
