package com.cardscannerapp.ui.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cardscannerapp.data.db.ContactDatabase
import com.cardscannerapp.data.repository.ContactRepository
import com.cardscannerapp.domain.model.ContactCard
import com.cardscannerapp.ui.theme.BrandPrimary
import com.cardscannerapp.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onNavigateToEdit: (String) -> Unit, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { ContactsViewModel(ContactRepository(ContactDatabase.getInstance(context).contactDao())) }
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Contacts") },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
            actions = { IconButton(onClick = { if (uiState.contacts.isNotEmpty()) ShareHelper.shareCsv(context, uiState.contacts) }, modifier = Modifier.testTag("export-all-contacts-button")) { Icon(Icons.Default.Share, contentDescription = "Export All") } })
    }) { padding ->
        if (uiState.isLoading) { Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (uiState.contacts.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No contacts yet", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Text("Scan a business card to get started", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.contacts, key = { it.id }) { contact ->
                    ContactCardItem(contact = contact, onClick = { onNavigateToEdit(contact.id) }, onDelete = { viewModel.deleteContact(contact) })
                }
            }
        }
    }
}

@Composable
private fun ContactCardItem(contact: ContactCard, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(BrandPrimary, CircleShape), contentAlignment = Alignment.Center) {
                Text(text = (contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"), color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name.ifBlank { "Unknown" }, style = MaterialTheme.typography.titleMedium)
                if (contact.company.isNotBlank()) Text(contact.company, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (contact.email.isNotBlank()) Text(contact.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete-button-${contact.id}")) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray) }
        }
    }
}
