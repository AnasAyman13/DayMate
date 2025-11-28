package com.day.mate.ui.onboardingActivity3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.R
import com.day.mate.ui.theme.Teal

// تم حذف OnboardingActivity3 بالكامل
// تم حذف PageDot بالكامل

@Composable
fun OnboardingScreen3(
    onStart: () -> Unit // لإنهاء الـ Onboarding والانتقال إلى Auth
) {
    val backgroundDark = Color(0xFF101F22)
    val accentTeal = Color(0xFF13C8EC)
    val cardBorder = Color.White.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (30).dp)
        ) {

            // Vault Icon Section (Illustration)
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(200.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, cardBorder, RoundedCornerShape(100.dp))
                    .shadow(8.dp, RoundedCornerShape(100.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    tint = accentTeal,
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(id = R.string.onboarding3_title),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.onboarding3_description),
                color = Color(0xFFB0BEC5),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        // ===== Footer =====
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // تم حذف Row الخاص بالنقاط هنا

            Spacer(modifier = Modifier.height(24.dp))

            // Let’s Start Button
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text(
                    text = stringResource(id = R.string.lets_start),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}