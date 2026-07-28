package com.teguholica.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.teguholica.chat.data.local.DatabaseDriverFactory
import com.teguholica.chat.data.local.AndroidTokenStorage
import com.teguholica.chat.di.initKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initKoin(AndroidTokenStorage(this), MediaPicker(this), DatabaseDriverFactory(this))

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}