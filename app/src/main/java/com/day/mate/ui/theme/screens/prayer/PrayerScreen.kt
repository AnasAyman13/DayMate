package com.day.mate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
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
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.day.mate.R
import com.day.mate.ui.theme.screens.prayer.PrayerViewModel
import com.day.mate.services.AdhanService
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/** Helper functions for saving/loading Adhan state */
fun saveAdhanPref(ctx: Context, prayer: String, enabled: Boolean) {
    val prefs = ctx.getSharedPreferences("adhan_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean(prayer, enabled).apply()
}

fun getAdhanPref(ctx: Context, prayer: String): Boolean {
    val prefs = ctx.getSharedPreferences("adhan_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean(prayer, false)
}

// ✅ دالة التحقق الجديدة (خارج Composable)
fun checkExactAlarmPermission(context: Context): Boolean {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return am.canScheduleExactAlarms()
    }
    return true
}


@SuppressLint("MissingPermission")
@Composable
fun PrayerScreen(viewModel: PrayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val timings by viewModel.timings.collectAsState()
    val ctx = LocalContext.current
    val scroll = rememberScrollState()
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF042825), Color(0xFF073B3A)))

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    // ✅ 1. تعريف Launcher لإطلاق Intent الإعدادات
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // بعد عودة المستخدم من الإعدادات، نقوم بإعادة جدولة الأذان (الـViewModel سيتولى ذلك)
        viewModel.loadPrayerTimes(ctx = ctx)
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

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
        } catch (e: Exception) { null }
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

    /** Adhan switches with persistence */
    val adhanEnabled = remember {
        mutableStateMapOf(
            "Fajr" to getAdhanPref(ctx, "Fajr"),
            "Dhuhr" to getAdhanPref(ctx, "Dhuhr"),
            "Asr" to getAdhanPref(ctx, "Asr"),
            "Maghrib" to getAdhanPref(ctx, "Maghrib"),
            "Isha" to getAdhanPref(ctx, "Isha")
        )
    }

    // 🚨 الخطوة 1: جدولة الأذانات المفعلة عند بدء التشغيل
    LaunchedEffect(timings) {
        timings?.let { t ->
            Log.d("PrayerScreen", "Attempting to schedule saved adhans...")
            viewModel.loadPrayerTimes(ctx = ctx)
        }
    }


    val hijriStr = remember { getHijriDateSafely(ctx) }

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var deviceAzimuth by remember { mutableStateOf(0f) }
    var qiblaBearing by remember { mutableStateOf<Float?>(null) }

    DisposableEffect(ctx) {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) { userLocation = loc }
            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5_000L, 5f, listener)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5_000L, 5f, listener)
                val last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (last != null) userLocation = last
            } catch (_: Exception) {}
        }
        onDispose { try { lm.removeUpdates(listener) } catch (_: Exception) {} }
    }

    DisposableEffect(ctx) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val adjustedMatrix = FloatArray(9)
            private val orientation = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent) {
                try {
                    // تحويل Rotation Vector إلى مصفوفة دوران
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    // إعادة ضبط الإحداثيات لوضع الجهاز الأفقي
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        adjustedMatrix
                    )

                    // استخراج الاتجاه بالدرجات
                    SensorManager.getOrientation(adjustedMatrix, orientation)
                    var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (azimuthDeg < 0) azimuthDeg += 360f

                    deviceAzimuth = azimuthDeg
                } catch (_: Exception) {}
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensor != null)
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            try { sm.unregisterListener(listener) } catch (_: Exception) {}
        }
    }


    LaunchedEffect(userLocation) {
        userLocation?.let { loc ->
            val lat1 = Math.toRadians(loc.latitude)
            val lon1 = Math.toRadians(loc.longitude)
            val lat2 = Math.toRadians(21.4225)
            val lon2 = Math.toRadians(39.8262)
            val dLon = lon2 - lon1
            val y = sin(dLon) * cos(lat2)
            val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
            val bearing = Math.toDegrees(atan2(y, x))
            qiblaBearing = ((bearing + 360.0) % 360.0).toFloat()
        }
    }

    val targetAngle = remember(deviceAzimuth, qiblaBearing) {
        qiblaBearing?.let { (((it - deviceAzimuth) + 540f) % 360f - 180f) } ?: 0f
    }
    val animatedAngle by animateFloatAsState(targetValue = targetAngle ?: 0f, animationSpec = TweenSpec(durationMillis = 400))
    val deltaToQibla = qiblaBearing?.let { ((it - deviceAzimuth + 360f) % 360f).let { if (it > 180f) 360f - it else it } } ?: 999f
    val isAligned = deltaToQibla <= 8f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .verticalScroll(scroll)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.location_hijri, hijriStr),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nextPrayerName,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF3E1F00)
                                )
                                Text(String.format("%02d:%02d:%02d", remH, remM, remS), color = Color(0xFF2C1A00))
                            }
                            Icon(Icons.Outlined.AccessTime, contentDescription = "time", tint = Color(0xFF3E1F00), modifier = Modifier.size(40.dp).padding(start = 8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timings?.let { t ->
                listOf(
                    "Fajr" to t.Fajr,
                    "Dhuhr" to t.Dhuhr,
                    "Asr" to t.Asr,
                    "Maghrib" to t.Maghrib,
                    "Isha" to t.Isha
                ).forEach { (name, timeStr) ->
                    val formatted = try { sdf12.format(sdf24.parse(timeStr)!!) } catch(_:Exception){ timeStr }

                    val timeMillis = timeStrToNextMillis(timeStr)

                    PrayerRow(
                        name = stringResource(
                            when(name){
                                "Fajr" -> R.string.fajr
                                "Dhuhr" -> R.string.dhuhr
                                "Asr" -> R.string.asr
                                "Maghrib" -> R.string.maghrib
                                "Isha" -> R.string.isha
                                else -> R.string.loading
                            }
                        ),
                        time = formatted,
                        enabled = adhanEnabled[name] == true
                    ) { checked ->
                        adhanEnabled[name] = checked
                        saveAdhanPref(ctx, name, checked)

                        // 🚨 2. منطق زر التبديل المهني
                        if (timeMillis != null) {

                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkExactAlarmPermission(ctx)) {

                                // الإذن مفقود: توجيه المستخدم لصفحة الإعدادات عبر Launcher
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", ctx.packageName, null)
                                }
                                settingsLauncher.launch(intent)

                                // رسالة توست مهنية لمرة واحدة فقط
                                Toast.makeText(ctx, "يرجى تفعيل إذن الجدولة الدقيقة في الشاشة المفتوحة ليعمل الأذان.", Toast.LENGTH_LONG).show()

                            } else if (checked) {
                                // الإذن ممنوح (أو الإصدار قديم): قم بالجدولة مباشرة
                                val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
                                scheduleAdhan(ctx, name,
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE)
                                )
                                // إظهار رسالة نجاح لمرة واحدة
                                Toast.makeText(ctx, "تم تفعيل أذان ${name}", Toast.LENGTH_SHORT).show()
                            } else {
                                // تعطيل: إلغاء الجدولة
                                cancelAdhanSchedule(ctx, name)
                                Toast.makeText(ctx, "تم إلغاء أذان ${name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } ?: Text(stringResource(R.string.loading_prayer_times), color = Color.White)

            Spacer(Modifier.height(26.dp))

            // Qibla Card
            Text(stringResource(R.string.qibla_direction), color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circle / compass
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2
                            val cy = size.height / 2
                            val center = Offset(cx, cy)
                            val radius = min(size.width, size.height) / 2 - 12f

                            drawCircle(color = Color(0xFF0B3A36), center = center, radius = radius + 12f)
                            drawCircle(color = Color(0xFF012A27), center = center, radius = radius)
                            drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFCCAC00))),
                                center = center, radius = radius, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f))

                            val cubeSize = 20f
                            drawRect(
                                brush = Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFC6A000))),
                                topLeft = Offset(center.x - cubeSize / 2, center.y - cubeSize / 2),
                                size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize)
                            )

                            rotate(degrees = animatedAngle) {
                                val end = Offset(cx, cy - radius + 20f)
                                drawLine(color = Color(0xFFFFC107), start = center, end = end, strokeWidth = 10f, cap = StrokeCap.Round)
                                val headSize = 18f
                                val left = Offset(end.x - headSize / 2, end.y + headSize)
                                val right = Offset(end.x + headSize / 2, end.y + headSize)
                                drawLine(Color(0xFFFFC107), start = end, end = left, strokeWidth = 8f, cap = StrokeCap.Round)
                                drawLine(Color(0xFFFFC107), start = end, end = right, strokeWidth = 8f, cap = StrokeCap.Round)
                            }
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Right column with fixed texts (no stringResource)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp) // مسافة جانبية متناسبة
                            .wrapContentHeight(), // يخلي ارتفاع العمود حسب المحتوى
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally // يظبط النص في الوسط
                    ) {
                        Text(
                            "اللهم صلِّ على سيِّدنا محمدَ ﷺ",
                            color = Color(0xFF4B2E00),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp // حجم متناسب مع كل الشاشات
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِين كِتَابًا مَوْقُوتًاَ",
                            color = Color(0xFF4B2E00),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(color = Color(0x33FFFFFF), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when {
                        qiblaBearing == null -> stringResource(R.string.getting_location)
                        isAligned -> stringResource(R.string.facing_qibla)
                        else -> if ((qiblaBearing!! - deviceAzimuth + 360f) % 360f in 0f..180f) stringResource(R.string.turn_right)
                        else stringResource(R.string.turn_left)
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

        }
    }
}

@Composable
fun PrayerRow(name: String, time: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(time, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                Text(stringResource(R.string.enable_adhan), color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
        }
    }
}

// Hijri date function
fun getHijriDateSafely(ctx: Context): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val islamic = android.icu.util.IslamicCalendar()
            val day = islamic.get(android.icu.util.Calendar.DAY_OF_MONTH)
            val monthIndex = islamic.get(android.icu.util.Calendar.MONTH)
            val months = listOf(
                R.string.muharram, R.string.safar, R.string.rabi_al_awwal, R.string.rabi_al_thani,
                R.string.jumada_al_awwal,R.string.jumada_al_thani, R.string.rajab, R.string.shaaban,
                R.string.ramadan, R.string.shawwal, R.string.dhu_al_qidah, R.string.dhu_al_hijjah
            ).map { ctx.getString(it) }
            val month = months.getOrElse(monthIndex) { "" }
            val year = islamic.get(android.icu.util.Calendar.YEAR)
            ctx.getString(R.string.location_hijri, "$day $month $year")
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val hijrah = java.time.chrono.HijrahDate.now()
                val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
                hijrah.format(formatter)
            } catch (_: Exception) {
                SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(Date())
            }
        } else {
            SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(Date())
        }
    } catch (_: Exception) { "—" }
}

// 🚨 دوال الجدولة والإلغاء (مع التعديل ليصبح صامتاً إذا كان الإذن مفقوداً)
fun scheduleAdhan(context: Context, prayer: String, hour: Int, minute: Int) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // 🚨 1. التحقق الصامت (منع أي Toast أو Intent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!am.canScheduleExactAlarms()) {
            // سجل المشكلة فقط وعد بصمت (هذا يحل مشكلة الـ5 رسائل)
            Log.w("AdhanScheduler", "Cannot schedule $prayer. Exact Alarm permission missing. User must enable it first.")
            return
        }
    }

    // 2. إعداد الوقت والنية (الـIntent)
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
    }

    val intent = Intent(context, AdhanService::class.java).apply {
        putExtra("PRAYER_NAME", prayer)
    }

    val pendingIntent = PendingIntent.getService(
        context,
        prayer.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 3. الجدولة الفعلية
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }
        Log.d("AdhanScheduler", "Scheduled $prayer successfully at ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(cal.timeInMillis))}")
    } catch (e: SecurityException) {
        // هذا الـcatch هو فقط للتأكد من عدم انهيار التطبيق
        Log.e("ALARM_ERROR", "Unexpected SecurityException caught during scheduling.", e)
    }
}

fun cancelAdhanSchedule(context: Context, prayer: String) {
    val intent = Intent(context, AdhanService::class.java)
    val pendingIntent = PendingIntent.getService(
        context,
        prayer.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.cancel(pendingIntent)
    Log.d("AdhanScheduler", "Canceled schedule for $prayer")
}