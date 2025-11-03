package com.day.mate.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSave: (focusMinutes: Int, shortBreakMinutes: Int, longBreakMinutes: Int) -> Unit
) {
    var focusTime by remember { mutableStateOf("25") }
    var shortBreak by remember { mutableStateOf("5") }
    var longBreak by remember { mutableStateOf("15") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Settings", fontSize = 28.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

        OutlinedTextField(
            value = focusTime,
            onValueChange = { focusTime = it },
            label = { Text("Focus Time (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = shortBreak,
            onValueChange = { shortBreak = it },
            label = { Text("Short Break (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = longBreak,
            onValueChange = { longBreak = it },
            label = { Text("Long Break (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Button(onClick = {
                onSave(focusTime.toIntOrNull() ?: 25,
                    shortBreak.toIntOrNull() ?: 5,
                    longBreak.toIntOrNull() ?: 15)
            }) { Text("Save") }
        }
    }
}