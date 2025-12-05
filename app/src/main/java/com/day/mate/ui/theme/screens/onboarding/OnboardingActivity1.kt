package com.day.mate.ui.onboardingActivity1

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

// تم حذف OnboardingActivity1 بالكامل
// تم حذف PageDot بالكامل

/**
 * OnboardingScreen1
 *
 * Displays the first screen of the application's onboarding flow, featuring the core
 * Todo list/task management illustration and primary navigation actions.
 *
 * @param onContinue Callback to navigate to the next page (Page 2) in the Pager.
 * @param onSkip Callback to skip onboarding and navigate directly to the Auth screen.
 */
@Composable
fun OnboardingScreen1(
    onContinue: () -> Unit, // Navigate to page 2
    onSkip: () -> Unit       // Navigate to Auth screen
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
                .verticalScroll(rememberScrollState()) // FIX: Added vertical scroll for landscape support
        ) {

            // Illustration Card (Todo Mockup)
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

                    // Task 1 (Done)
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

                    // Task 2 (Active/Gold Star)
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

                    // Task 3 (Done)
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

            // Main Title
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

            // Description
            Text(
                text = stringResource(id = R.string.onboarding_description),
                color = Color(0xFFB0BEC5),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            // Add spacing here so the scroll works properly, pushing the content up
            Spacer(modifier = Modifier.height(180.dp))
        }

        // Action Buttons (Fixed at the bottom of the Box)
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

/**
 * TaskTextField (Placeholder component, assumed to be used by other onboarding screens)
 */
@Composable
private fun TaskTextField() {
    // This is a placeholder for external dependency
}

@Preview(showBackground = true)
@Composable
private fun PreviewOnboardingScreen1() {
    OnboardingScreen1(onContinue = {}, onSkip = {})
}