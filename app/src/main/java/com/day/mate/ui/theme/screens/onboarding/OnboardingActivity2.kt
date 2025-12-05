package com.day.mate.ui.onboardingActivity2

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
import com.day.mate.R
import com.day.mate.ui.theme.SoftGold
import com.day.mate.ui.theme.SkyBlue
import com.day.mate.ui.theme.Teal

// NOTE: Placeholder comment for removed component documentation

/**
 * OnboardingScreen2
 *
 * Displays the second screen of the onboarding flow, illustrating the Pomodoro/Focus feature
 * using a graphical timer representation.
 *
 * @param progress The current progress value (0.0 to 1.0) of the timer illustration.
 * @param onContinue Callback to navigate to the next page (Page 3) in the Pager.
 * @param onSkip Callback to skip onboarding and navigate directly to the Auth screen.
 */
@Composable
fun OnboardingScreen2(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    onContinue: () -> Unit, // Navigate to page 3
    onSkip: () -> Unit       // Navigate to Auth screen
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    // Adjust central visual size based on screen width
    val centralSize = if (screenWidthDp >= 768) 320.dp else 256.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))

        // Main circle visual (Illustration)
        Box(
            modifier = Modifier
                .size(centralSize)
                .clip(CircleShape)
                .background(
                    // Outer radial gradient for background glow
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
                // Inner circle with gradient fill
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

                    // Timer Progress Arc drawing
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val strokeWidth = 6.dp.toPx()
                        val inset = strokeWidth / 2f + 8.dp.toPx()
                        val arcRect = Rect(inset, inset, size.width - inset, size.height - inset)

                        // Draw background track (full circle)
                        drawArc(
                            color = SoftGold.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(arcRect.left, arcRect.top),
                            size = arcRect.size,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Draw progress arc
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

        // Title
        Text(
            text = stringResource(id = R.string.onboarding2_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        // Description
        Text(
            text = stringResource(id = R.string.onboarding2_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(0.9f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(Modifier.weight(1f))

        // Action Buttons
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

/**
 * toPxSafe
 *
 * Extension function to safely convert Dp dimension to pixel float value
 * within a Composable context using LocalDensity.
 */
@Composable
private fun Dp.toPxSafe(): Float = this.value * LocalDensity.current.density

@Preview(showBackground = true)
@Composable
private fun PreviewOnboardingScreen2() {
    OnboardingScreen2(progress = 0.75f, onContinue = {}, onSkip = {})
}