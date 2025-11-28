import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.location.LocationManager
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

    // الحالة الحالية للزاوية (Raw)
    var currentAzimuth by remember { mutableFloatStateOf(0f) }

    // أنيميشن لجعل حركة السهم ناعمة جداً بدلاً من القفزات
    val animatedAzimuth by animateFloatAsState(
        targetValue = currentAzimuth,
        animationSpec = tween(durationMillis = 100), // سرعة استجابة ناعمة
        label = "CompassAnimation"
    )

    var qiblaDirection by remember { mutableFloatStateOf(0f) }
    var location by remember { mutableStateOf<Location?>(null) }
    var geomagneticField by remember { mutableStateOf<GeomagneticField?>(null) }

    // ✅ الحساسات (تم تحسين السرعة والدقة)
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // مصفوفات لحساب التوجيه
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)

                // تحويل الراديان إلى درجات
                var azimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()

                // التأكد من أن الزاوية موجبة (0 - 360)
                azimuthDegrees = (azimuthDegrees + 360) % 360

                // ✅ تصحيح الشمال الجغرافي (True North) باستخدام الموقع
                geomagneticField?.let {
                    azimuthDegrees += it.declination
                }

                // ✅ فلتر بسيط لمنع اهتزاز السهم (Low Pass Filter logic via State)
                // إذا كان الفرق كبيراً (دوران كامل)، لا تقم بالتنعيم فوراً لتجنب التفاف السهم عكسياً
                if (abs(currentAzimuth - azimuthDegrees) > 180) {
                    // معالجة مشكلة الالتفاف عند نقطة الصفر (359 -> 1)
                    if (currentAzimuth > 180 && azimuthDegrees < 180) {
                        currentAzimuth -= 360
                    } else if (currentAzimuth < 180 && azimuthDegrees > 180) {
                        currentAzimuth += 360
                    }
                }

                currentAzimuth = azimuthDegrees
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // استخدام SENSOR_DELAY_GAME بدلاً من UI لسرعة استجابة أعلى
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // ✅ الموقع الحالي وحساب الانحراف المغناطيسي
    LaunchedEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            location = loc

            loc?.let {
                // حساب الانحراف المغناطيسي للدقة العالية
                geomagneticField = GeomagneticField(
                    it.latitude.toFloat(),
                    it.longitude.toFloat(),
                    it.altitude.toFloat(),
                    System.currentTimeMillis()
                )
            }
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

        Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2.5f

                // رسم دائرة البوصلة للخلفية
                drawCircle(color = Color.Gray.copy(alpha = 0.3f), radius = radius + 20f, center = center)

                // تدوير الـ Canvas بناءً على حركة الجهاز الناعمة
                // لاحظ: نقوم بتدوير الـ Canvas بعكس اتجاه الجهاز (-animatedAzimuth) ليبقى الشمال ثابتاً
                // ثم نضيف اتجاه القبلة لرسم السهم

                // رسم سهم الشمال (للمرجعية)
                rotate(-animatedAzimuth) {
                    drawLine(
                        color = Color.Red,
                        start = center,
                        end = Offset(center.x, center.y - radius),
                        strokeWidth = 5f
                    )
                    // رسم حرف N
                }

                // رسم سهم القبلة
                rotate(-animatedAzimuth + qiblaDirection) {
                    drawLine(
                        color = Color(0xFFFFD700), // لون ذهبي
                        start = center,
                        end = Offset(center.x, center.y - radius),
                        strokeWidth = 12f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = location?.let { "Heading: ${currentAzimuth.toInt()}° | Qibla: ${qiblaDirection.toInt()}°" } ?: "Locating...",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}