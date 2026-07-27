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
    val showAddDialog by viewModel.showAddDialog.collectAsState()

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
                TextButton(onClick = { viewModel.showAddDialog() }) { Text("+", fontSize = 18.sp) }
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
                                onClick = { viewModel.createOrNavigate(contact.phone) },
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

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { name, phone -> viewModel.addContact(name, phone) },
        )
    }
}

@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kontak") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor Telepon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, phone) },
                enabled = name.isNotBlank() && phone.isNotBlank(),
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
    )
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

        Column(Modifier.weight(1f)) {
            Text(contact.displayName, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (contact.phone.isNotBlank()) {
                    Text(
                        contact.phone,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (contact.userId != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Terdaftar",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
