package com.teguholica.chat.ui.creategroup

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
import com.teguholica.chat.domain.model.User
import org.koin.compose.koinInject

@Composable
fun CreateGroupScreen(
    onCreated: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateGroupViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val groupName by viewModel.groupName.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val selectedPhones by viewModel.selectedPhones.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is CreateGroupUiState.Success) {
            val id = (uiState as CreateGroupUiState.Success).conversationId
            onCreated(id)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("←", fontSize = 18.sp) }
                Text("Grup Baru", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.createGroup() }, enabled = groupName.isNotBlank() && selectedPhones.isNotEmpty()) {
                    Text("Buat")
                }
            }
        }

        when (val state = uiState) {
            is CreateGroupUiState.Creating -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            is CreateGroupUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = viewModel::updateGroupName,
                    label = { Text("Nama grup") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )

                Text(
                    "Peserta (${selectedPhones.size} dipilih)",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                LazyColumn {
                    items(contacts, key = { it.id }) { contact ->
                        ContactItem(
                            contact = contact,
                            isSelected = selectedPhones.contains(contact.phone ?: ""),
                            onClick = { viewModel.toggleContact(contact.phone ?: "") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactItem(contact: User, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val initial = (contact.displayName ?: contact.phone ?: "").firstOrNull()?.uppercase() ?: "?"
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .then(if (isSelected) Modifier else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, fontSize = 16.sp)
        }

        Spacer(Modifier.width(12.dp))
        Text(
            text = contact.displayName ?: contact.phone ?: "",
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
        )

        Checkbox(checked = isSelected, onCheckedChange = { onClick() })
    }
}
