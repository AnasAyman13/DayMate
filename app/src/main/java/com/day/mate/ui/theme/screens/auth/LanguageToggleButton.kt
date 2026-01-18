package com.day.mate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LanguageToggleButton(
    isArabic: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f)),
        tonalElevation = 0.dp
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(
                    imageVector = Icons.Outlined.Translate,
                    contentDescription = "Toggle language",
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isArabic) "EN" else "AR",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    lineHeight = 10.sp
                )
            }
        }
    }
}
