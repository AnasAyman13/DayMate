package com.day.mate.utils

fun getLocalizedErrorMessage(originalMessage: String?, isArabic: Boolean): String {
    val msg = originalMessage?.lowercase() ?: return if (isArabic) "حدث خطأ غير معروف" else "Unknown error occurred"

    if (!isArabic) return originalMessage

    return when {
        msg.contains("email address is already in use") || msg.contains("email already exists") -> "هذا البريد الإلكتروني مسجل مسبقاً."
        msg.contains("badly formatted") || msg.contains("invalid email") -> "تنسيق البريد الإلكتروني غير صحيح."
        msg.contains("password") && (msg.contains("weak") || msg.contains("6 characters")) -> "كلمة المرور ضعيفة (يجب أن تكون 6 أحرف على الأقل)."
        msg.contains("user not found") || msg.contains("no user record") -> "لا يوجد حساب بهذا البريد، يرجى التسجيل."
        msg.contains("wrong password") || msg.contains("invalid password") -> "كلمة المرور غير صحيحة."
        msg.contains("network error") || msg.contains("network request failed") -> "فشل الاتصال بالإنترنت، تحقق من الشبكة."
        msg.contains("blocked") || msg.contains("disabled") -> "تم تعطيل هذا الحساب لكثرة المحاولات الخاطئة."
        msg.contains("too many requests") -> "محاولات كثيرة جداً، الرجاء المحاولة لاحقاً."
        msg.contains("verified") -> "يرجى تفعيل الحساب من الرابط المرسل للإيميل."
        else -> "حدث خطأ: $originalMessage"
    }
}