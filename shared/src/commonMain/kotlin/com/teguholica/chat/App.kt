package com.teguholica.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teguholica.chat.ui.theme.ChatTheme

@Composable
fun App() {
    var darkTheme by remember { mutableStateOf(false) }

    ChatTheme(darkTheme = darkTheme) {
        Surface(Modifier.fillMaxSize()) {
            // Theme toggle — temporary, will be replaced by proper nav
            Column {
                Box(Modifier.fillMaxSize().weight(1f)) {
                    // Content placeholder — navigation root
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = { darkTheme = !darkTheme }) {
                        val label = if (darkTheme) "Mode Terang" else "Mode Gelap"
                        Text(label)
                    }
                }
            }
        }
    }
}
