package com.day.mate.ui.onboardingActivity3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

// NOTE: Placeholder comment for removed component documentation

/**
 * OnboardingScreen3
 *
 * Displays the third and final screen of the onboarding flow, illustrating the Media Vault/Security
 * feature using a lock icon and biometric security message.
 *
 * @param onStart Callback to finalize onboarding and navigate to the main Authentication screen.
 */
@Composable
fun OnboardingScreen3(
    onStart: () -> Unit // Navigate to Auth screen
) {
    val backgroundDark = Color(0xFF101F22)
    val accentTeal = Color(0xFF13C8EC) // Used for the lock icon tint
    val cardBorder = Color.White.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // Apply vertical scroll only to the content column to keep buttons fixed
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (30).dp)
                .verticalScroll(rememberScrollState())
                // FIX: تقليل الـ padding السفلي لترك مساحة كافية للـ Footer
                .padding(bottom = 180.dp)
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
                    contentDescription = stringResource(R.string.desc_lock_icon),
                    tint = accentTeal,
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title
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

            // Description
            Text(
                text = stringResource(id = R.string.onboarding3_description),
                color = Color(0xFFB0BEC5),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            // Add extra space to ensure content is scrollable in landscape
            Spacer(modifier = Modifier.height(30.dp))
        }

        // ===== Footer (Action Buttons) =====
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(backgroundDark) // Ensure footer overlaps content during scroll
                .padding(horizontal = 24.dp)
                // FIX: إعادة الزر للأسفل مع ترك padding مناسب (48dp هو القيمة القياسية للفوتر)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Let’s Start Button
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
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

@Preview(showBackground = true)
@Composable
private fun PreviewOnboardingScreen3() {
    OnboardingScreen3(onStart = {})
}