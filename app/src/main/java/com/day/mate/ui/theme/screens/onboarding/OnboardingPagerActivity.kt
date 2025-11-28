package com.day.mate.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.day.mate.AuthActivity
import com.day.mate.ui.onboardingActivity1.OnboardingScreen1
import com.day.mate.ui.onboardingActivity2.DayMateOnboardingScreen
import com.day.mate.ui.onboardingActivity3.OnboardingScreen3
import com.day.mate.ui.theme.Primary
import com.day.mate.ui.theme.DayMateTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class OnboardingPagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("DayMatePrefs", MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)

        if (!isFirstTime) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContent {
            DayMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OnboardingPager(
                        onFinishOnboarding = {
                            sharedPref.edit().putBoolean("isFirstTime", false).apply()
                            startActivity(Intent(this, AuthActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPager(onFinishOnboarding: () -> Unit) {
    val pageCount = 3
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pageCount }
    )
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // HorizontalPager مع أنيميشن سلسة
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp
        ) { page ->
            // حساب offset للأنيميشن
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            val scale = 0.85f + (0.15f * alpha)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = alpha
                        this.scaleX = scale
                        this.scaleY = scale
                        translationX = pageOffset * size.width * 0.3f
                    }
            ) {
                val goToNextPage: () -> Unit = {
                    coroutineScope.launch {
                        if (page < pageCount - 1) {
                            pagerState.animateScrollToPage(
                                page + 1,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }
                }

                when (page) {
                    0 -> OnboardingScreen1(
                        onContinue = goToNextPage,
                        onSkip = onFinishOnboarding
                    )
                    1 -> DayMateOnboardingScreen(
                        progress = 0.6f,
                        onContinue = goToNextPage,
                        onSkip = onFinishOnboarding
                    )
                    2 -> OnboardingScreen3(
                        onStart = onFinishOnboarding
                    )
                }
            }
        }

        // مؤشرات الصفحات مع أنيميشن
        AnimatedPageIndicator(
            pagerState = pagerState,
            pageCount = pageCount,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 180.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnimatedPageIndicator(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        repeat(pageCount) { index ->
            val isSelected = pagerState.currentPage == index

            // أنيميشن للحجم
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "scale"
            )

            // أنيميشن للون
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.3f,
                animationSpec = tween(durationMillis = 300),
                label = "alpha"
            )

            // أنيميشن للعرض (النقطة النشطة تكون أطول)
            val width by animateDpAsState(
                targetValue = if (isSelected) 20.dp else 6.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "width"
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(6.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = alpha))
            )

            if (index < pageCount - 1) {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}