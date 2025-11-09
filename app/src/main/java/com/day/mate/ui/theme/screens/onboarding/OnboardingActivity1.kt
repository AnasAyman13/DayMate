package com.day.mate.ui.onboardingActivity1
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.day.mate.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.day.mate.AuthActivity
import com.day.mate.MainActivity
import com.day.mate.ui.onboardingActivity2.OnboardingActivity2
import com.day.mate.ui.theme.*


class OnboardingActivity1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("DayMatePrefs", MODE_PRIVATE)
        val onboardingShown = sharedPref.getBoolean("onboarding_shown", false)

        if (onboardingShown) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        setContent {
            DayMateDarkTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    OnboardingScreen1(
                        onContinue = {
                            val intent = Intent(this, OnboardingActivity2::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onSkip = {
                            val intent = Intent(this, AuthActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun DayMateDarkTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = Primary,
        background = BackgroundDark,
        surface = Color(0xFF0F1A1C),
        onBackground = CharcoalLight,
        onPrimary = Color.Black
    )

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(
            titleLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = colors.onBackground
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 16.sp,
                color = colors.onBackground.copy(alpha = 0.8f)
            )
        ),
        content = content
    )
}

@Composable
fun OnboardingScreen1(
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    val backgroundDark = Color(0xFF101F22)
    val accentTeal = Color(0xFF008080)
    val accentGold = Color(0xFFD4AF37)
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
                .offset(y = 80.dp)
        ) {

            // frosted glass card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // ✅ Task 1 done
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = null,
                                tint = backgroundDark,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.task_design_mockups),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    // ☐ Task 2 (active)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF4B5563))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.task_call_client),
                            color = Color(0xFFEEEEEE),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = null,
                            tint = accentGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // ✅ Task 3 done
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = null,
                                tint = backgroundDark,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.task_schedule_meeting),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(id = R.string.onboarding_title),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(id = R.string.onboarding_description),
                color = Color(0xFFB0BEC5),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        // ===== Footer =====
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
        ) {
            PageDot(active = true)
            Spacer(Modifier.width(8.dp))
            PageDot(active = false)
            Spacer(Modifier.width(8.dp))
            PageDot(active = false)
        }


        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text(
                    text = stringResource(id = R.string.button_continue),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.button_skip),
                color = Color(0xFFB0BEC5),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSkip() }
            )

        }
    }
}

@Composable
private fun PageDot(active: Boolean) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (active) Primary else Primary.copy(alpha = 0.3f))
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewOnboardingDark() {
    DayMateDarkTheme {
        OnboardingScreen1(onContinue = {}, onSkip = {})
    }
}
