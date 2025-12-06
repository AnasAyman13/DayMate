package com.day.mate.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.day.mate.data.local.media.VaultType
import com.day.mate.data.local.media.VaultTypeConverter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple instrumentation test for VaultTypeConverter.
 * Uses only Android-compatible APIs.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class VaultTypeConverterTest {

    private val converter = VaultTypeConverter()

    @Test
    fun fromType_toType_roundtrip() {
        val original = VaultType.PHOTO
        val asString = converter.fromType(original)
        val back = converter.toType(asString)
        assertEquals(original, back)
    }
}