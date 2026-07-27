package com.teguholica.chat.ui.newchat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject

@Composable
fun NewChatScreen(
    onContactSelected: (chatId: String, chatName: String) -> Unit,
    onBack: () -> Unit,
    viewModel: NewChatViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadContacts() }

    LaunchedEffect(uiState) {
        if (uiState is NewChatUiState.Created) {
            val state = uiState as NewChatUiState.Created
            onContactSelected(state.chatId, state.chatName)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("←", fontSize = 18.sp) }
                Text("Kontak", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            placeholder = { Text("Cari kontak...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
        )

        when (val state = uiState) {
            is NewChatUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is NewChatUiState.Success -> {
                if (state.filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Tidak ada kontak",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn {
                        items(state.filtered, key = { it.id }) { contact ->
                            ContactRow(
                                contact = contact,
                                onClick = { viewModel.createOrNavigate(contact.id) },
                            )
                        }
                    }
                }
            }
            is NewChatUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadContacts() }) { Text("Coba lagi") }
                }
            }
            is NewChatUiState.Created -> { }
        }
    }
}

@Composable
private fun ContactRow(contact: ContactDisplay, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val initial = contact.displayName.firstOrNull()?.uppercase() ?: "?"
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .then(Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, fontSize = 16.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(contact.displayName, fontSize = 16.sp)
            if (contact.phone.isNotBlank()) {
                Text(
                    contact.phone,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
