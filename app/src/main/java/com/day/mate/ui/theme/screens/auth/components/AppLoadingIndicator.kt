package com.day.mate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.day.mate.R

@Composable
fun AppLoadingIndicator(sizeDp: Int = 64) {
    val transition = rememberInfiniteTransition(label = "logo_rotation")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Image(
        painter = painterResource(id = R.drawable.forgrnd),
        contentDescription = "Loading",
        modifier = Modifier
            .size(sizeDp.dp)
            .rotate(rotation.value)
    )
}
