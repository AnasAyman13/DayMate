package com.day.mate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.day.mate.ui.theme.navigation.MainNavGraph
import com.day.mate.ui.theme.DayMateTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DayMateTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainNavGraph()
                }
            }
        }
    }
}
