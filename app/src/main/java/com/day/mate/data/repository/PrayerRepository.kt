package com.day.mate.data.repository



import com.day.mate.data.local.PrayerApiService
import com.day.mate.data.local.Timings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// سنفترض أننا نستخدم الموقع الثابت (القاهرة، مصر) كما في PrayerViewModel
class PrayerRepository(private val apiService: PrayerApiService) {

    // تحويل استدعاء API إلى Flow
    fun getPrayerTimingsFlow(city: String = "Cairo", country: String = "Egypt"): Flow<Timings?> = flow {
        try {
            // ✅ لا حاجة لتمرير التاريخ هنا لأن API Aladhan يجلب اليوم الحالي افتراضياً
            val response = apiService.getPrayerTimes(city, country)
            emit(response.data.timings)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null) // إرسال Null في حالة الخطأ
        }
    }
}