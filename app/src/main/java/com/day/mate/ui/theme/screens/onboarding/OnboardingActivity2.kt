package com.day.mate.ui.onboardingActivity2
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.day.mate.AuthActivity
import com.day.mate.MainActivity
import com.day.mate.ui.theme.*
import com.day.mate.R
import com.day.mate.ui.onboardingActivity1.DayMateDarkTheme
import com.day.mate.ui.onboardingActivity3.OnboardingActivity3


class OnboardingActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DayMateDarkTheme {
                DayMateOnboardingScreen(
                    progress = 0.6f,
                    onContinue = {
                        // بعد ما المستخدم يضغط "Continue" يروح على MainActivity
                        val intent = Intent(this, OnboardingActivity3::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onSkip = {
                        // لو المستخدم ضغط "Skip"
                        val intent = Intent(this, AuthActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

/** ---------- Onboarding Screen ---------- */
@Composable
fun DayMateOnboardingScreen(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val centralSize = if (screenWidthDp >= 768) 320.dp else 256.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))

        // Main circle visual
        Box(
            modifier = Modifier
                .size(centralSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Teal.copy(alpha = 0.14f), SkyBlue.copy(alpha = 0.14f)),
                        center = Offset.Zero,
                        radius = centralSize.toPxSafe()
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Teal, SkyBlue),
                                center = Offset.Zero,
                                radius = centralSize.toPxSafe()
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = stringResource(id = R.string.icon_timer_description),
                        tint = SoftGold,
                        modifier = Modifier.size(72.dp)
                    )

                    Canvas(modifier = Modifier.matchParentSize()) {
                        val strokeWidth = 6.dp.toPx()
                        val inset = strokeWidth / 2f + 8.dp.toPx()
                        val arcRect = Rect(inset, inset, size.width - inset, size.height - inset)

                        drawArc(
                            color = SoftGold.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(arcRect.left, arcRect.top),
                            size = arcRect.size,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        val sweep = 360f * progress.coerceIn(0f, 1f)
                        val shader = Brush.linearGradient(
                            colors = listOf(SoftGold, SoftGold.copy(alpha = 0.5f)),
                            start = Offset(arcRect.left, arcRect.top),
                            end = Offset(arcRect.right, arcRect.bottom)
                        )
                        drawArc(
                            brush = shader,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(arcRect.left, arcRect.top),
                            size = arcRect.size,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = stringResource(id = R.string.onboarding2_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.onboarding2_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(0.9f)
        )

        Spacer(Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            PageDot(active = false)
            Spacer(Modifier.width(8.dp))
            PageDot(active = true)
            Spacer(Modifier.width(8.dp))
            PageDot(active = false)
        }

        Spacer(Modifier.height(18.dp))

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

        Spacer(Modifier.height(10.dp))

        TextButton(onClick = onSkip) {
            Text(
                text = stringResource(id = R.string.button_skip),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.height(12.dp))
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

@Composable
private fun Dp.toPxSafe(): Float = this.value * LocalDensity.current.density

@Preview(showBackground = true)
@Composable
fun PreviewOnboardingDark() {
    DayMateDarkTheme {
        DayMateOnboardingScreen(progress = 0.6f, onContinue = {}, onSkip = {})
    }
}
