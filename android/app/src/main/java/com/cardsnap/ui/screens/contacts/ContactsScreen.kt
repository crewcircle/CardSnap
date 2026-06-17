package com.cardsnap.ui.screens.contacts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAdd
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
import com.cardsnap.data.db.ContactDatabase
import com.cardsnap.data.repository.ContactRepository
import com.cardsnap.domain.model.ContactCard
import com.cardsnap.domain.model.ContactsState
import com.cardsnap.ui.theme.BrandPrimary
import com.cardsnap.ui.screens.contacts.ContactsViewModel.ImportResult
import com.cardsnap.util.ShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onNavigateToEdit: (String) -> Unit, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { ContactsViewModel(ContactRepository(ContactDatabase.getInstance(context).contactDao())) }
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val vcfImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val content = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() } ?: ""
                    if (content.isNotBlank()) {
                        viewModel.importVCard(content)
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Import failed: ${e.message}")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.importEvent.collect { result ->
            when (result) {
                is ImportResult.Success -> {
                    snackbarHostState.showSnackbar("Imported ${result.count} contact(s)")
                }
                is ImportResult.Error -> {
                    snackbarHostState.showSnackbar("Import failed: ${result.message}")
                }
            }
        }
    }

    val duplicatePairs by viewModel.duplicatePairs.collectAsState()
    var showMergeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.mergeEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Contacts") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { vcfImportLauncher.launch(arrayOf("text/vcard", "text/x-vcard", "text/directory")) },
                        modifier = Modifier.testTag("import-vcard-button")) {
                        Icon(Icons.Default.NoteAdd, contentDescription = "Import VCard")
                    }
                    IconButton(onClick = { val contacts = (state as? ContactsState.Success)?.contacts.orEmpty(); if (contacts.isNotEmpty()) ShareHelper.shareCsv(context, contacts) },
                        modifier = Modifier.testTag("export-all-contacts-button")) {
                        Icon(Icons.Default.Share, contentDescription = "Export All")
                    }
                })
        }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (duplicatePairs.isNotEmpty()) {
                DuplicateBanner(
                    count = duplicatePairs.size,
                    onReview = { showMergeDialog = true },
                    modifier = Modifier.testTag("duplicate-banner")
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state) {
                    is ContactsState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                    is ContactsState.Empty -> {
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No contacts yet", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Text("Scan a business card to get started", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                    is ContactsState.Success -> {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.contacts, key = { it.id }) { contact ->
                                ContactCardItem(contact = contact, onClick = { onNavigateToEdit(contact.id) }, onDelete = { viewModel.deleteContact(contact) })
                            }
                        }
                    }
                    is ContactsState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Failed to load contacts", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(state.message, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMergeDialog && duplicatePairs.isNotEmpty()) {
        val pair = duplicatePairs.first()
        MergeDialog(
            pair = pair,
            onMerge = {
                viewModel.mergeContacts(pair.first, pair.second)
                showMergeDialog = false
            },
            onSkip = {
                viewModel.dismissDuplicatePair(pair.first, pair.second)
                showMergeDialog = false
            },
            onDismiss = { showMergeDialog = false }
        )
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

@Composable
private fun DuplicateBanner(count: Int, onReview: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (count == 1) "1 duplicate contact found" else "$count duplicate contacts found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            FilledTonalButton(
                onClick = onReview,
                modifier = Modifier.testTag("duplicate-review-button")
            ) { Text("Review") }
        }
    }
}

@Composable
private fun MergeDialog(
    pair: Pair<ContactCard, ContactCard>,
    onMerge: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Merge Contacts") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ContactSummary(pair.first)
                HorizontalDivider()
                ContactSummary(pair.second)
            }
        },
        confirmButton = {
            Button(
                onClick = onMerge,
                modifier = Modifier.testTag("merge-button")
            ) { Text("Merge") }
        },
        dismissButton = {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.testTag("skip-button")
            ) { Text("Skip") }
        }
    )
}

@Composable
private fun ContactSummary(contact: ContactCard) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (contact.name.isNotBlank()) {
            Text(contact.name, style = MaterialTheme.typography.titleSmall)
        }
        if (contact.email.isNotBlank()) {
            Text(contact.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (contact.phone.isNotBlank()) {
            Text(contact.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (contact.company.isNotBlank()) {
            Text(contact.company, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
