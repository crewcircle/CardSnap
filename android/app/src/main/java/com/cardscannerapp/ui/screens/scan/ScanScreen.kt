package com.cardscannerapp.ui.screens.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.cardscannerapp.data.db.ContactDatabase
import com.cardscannerapp.data.repository.ContactRepository
import com.cardscannerapp.data.repository.SettingsRepository
import com.cardscannerapp.ui.theme.*
import com.cardscannerapp.util.HapticFeedback
import com.cardscannerapp.util.NetworkMonitor
import com.cardscannerapp.util.ShareHelper
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(imageUri: String? = null, onNavigateToContacts: () -> Unit = {}, onNavigateToSettings: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = remember { ScanViewModel(ContactRepository(ContactDatabase.getInstance(context).contactDao()), SettingsRepository(context)) }
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { NetworkMonitor.observeNetwork(context).collect { isConnected -> viewModel.setOffline(!isConnected) } }
    LaunchedEffect(imageUri) { if (!imageUri.isNullOrBlank()) viewModel.processImage(imageUri, context) }

    var hasCameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted -> hasCameraPermission = granted }
    LaunchedEffect(Unit) { if (!hasCameraPermission) cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { val imagePath = copyUriToCache(context, it); viewModel.processImage("file://$imagePath", context); HapticFeedback.medium(context) }
    }

    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp)); Text("Camera permission required", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Grant Permission") }
            }
        }; return
    }

    if (uiState.showResults) ScanResultsView(uiState = uiState, viewModel = viewModel, context = context, onReset = { viewModel.resetState() }, onNavigateToContacts = onNavigateToContacts)
    else CameraView(uiState = uiState, viewModel = viewModel, context = context, lifecycleOwner = lifecycleOwner, onNavigateToContacts = onNavigateToContacts, onNavigateToSettings = onNavigateToSettings, onPickImage = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })

    if (uiState.errorMessage != null) {
        AlertDialog(onDismissRequest = { viewModel.clearError() }, title = { Text("Error") }, text = { Text(uiState.errorMessage!!) }, confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } })
    }
}

@Composable
private fun CameraView(uiState: ScanUiState, viewModel: ScanViewModel, context: Context, lifecycleOwner: androidx.lifecycle.LifecycleOwner, onNavigateToContacts: () -> Unit, onNavigateToSettings: () -> Unit, onPickImage: () -> Unit) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black).testTag("scan-screen")) {
        AndroidView(factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            try { cameraProvider.unbindAll(); cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture) } catch (e: Exception) { e.printStackTrace() }
            previewView
        }, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { viewModel.toggleTorch() }, modifier = Modifier.testTag("torch-button")) { Icon(imageVector = if (uiState.torchOn) Icons.Default.FlashOn else Icons.Default.FlashOff, contentDescription = "Torch", tint = if (uiState.torchOn) Warning else Color.White) }
                IconButton(onClick = onNavigateToContacts, modifier = Modifier.testTag("contacts-button")) { Icon(Icons.Default.People, contentDescription = "Contacts", tint = Color.White) }
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("settings-button")) { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White) }
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.width(280.dp).aspectRatio(1.75f).padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Fit the card inside the frame", color = Color.White.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
            }
            if (uiState.isOffline) { Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).background(Color.Black.copy(alpha = 0.75f), MaterialTheme.shapes.small).padding(10.dp), contentAlignment = Alignment.Center) { Text("No internet — scanning still works", color = Color.White) }; Spacer(modifier = Modifier.height(8.dp)) }
            Text("Point camera at business card and tap to capture", color = Color.White, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                HapticFeedback.medium(context)
                val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
                imageCapture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), Executors.newSingleThreadExecutor(), object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) { viewModel.processImage("file://${file.absolutePath}", context) }
                    override fun onError(exception: ImageCaptureException) { exception.printStackTrace() }
                })
            }, enabled = !uiState.isProcessing, modifier = Modifier.size(60.dp).testTag("capture-button"), colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)) {
                if (uiState.isProcessing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                else Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onPickImage, modifier = Modifier.testTag("gallery-button")) { Icon(Icons.Default.Image, contentDescription = null, tint = Color.White); Spacer(modifier = Modifier.width(4.dp)); Text("or upload a photo", color = Color.White) }
            Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
        }
        AnimatedVisibility(visible = uiState.isProcessing) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(color = BrandPrimary); Spacer(modifier = Modifier.height(16.dp)); Text("Reading card...", color = Color.White) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanResultsView(uiState: ScanUiState, viewModel: ScanViewModel, context: Context, onReset: () -> Unit, onNavigateToContacts: () -> Unit) {
    var name by remember(uiState.contact.name) { mutableStateOf(uiState.contact.name) }
    var email by remember(uiState.contact.email) { mutableStateOf(uiState.contact.email) }
    var phone by remember(uiState.contact.phone) { mutableStateOf(uiState.contact.phone) }
    var company by remember(uiState.contact.company) { mutableStateOf(uiState.contact.company) }
    var title by remember(uiState.contact.title) { mutableStateOf(uiState.contact.title) }
    var website by remember(uiState.contact.website) { mutableStateOf(uiState.contact.website) }
    val updatedContact = uiState.contact.copy(name = name, email = email, phone = phone, company = company, title = title, website = website)
    Scaffold(topBar = { TopAppBar(title = { Text("Review Contact") }, navigationIcon = { IconButton(onClick = onReset) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            uiState.capturedImage?.let { uri -> AsyncImage(model = uri, contentDescription = "Captured card", modifier = Modifier.fillMaxWidth().height(200.dp)); Spacer(modifier = Modifier.height(16.dp)) }
            listOf("field-name" to name to { name = it }, "field-email" to email to { email = it }, "field-phone" to phone to { phone = it }, "field-company" to company to { company = it }, "field-title" to title to { title = it }, "field-website" to website to { website = it }).forEach { (tag, value, setter) ->
                OutlinedTextField(value = value, onValueChange = setter, label = { Text(tag.removePrefix("field-").replaceFirstChar { it.uppercase() }) }, modifier = Modifier.fillMaxWidth().testTag(tag), keyboardOptions = KeyboardOptions(keyboardType = when(tag) { "field-email" -> KeyboardType.Email; "field-phone" -> KeyboardType.Phone; "field-website" -> KeyboardType.Uri; else -> KeyboardType.Text }))
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (uiState.extractedText.isNotBlank()) { Spacer(modifier = Modifier.height(16.dp)); Text("Raw OCR Text", style = MaterialTheme.typography.labelMedium, color = TextSecondary); Text(uiState.extractedText, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.background(SurfacePrimary).padding(8.dp)) }
            Spacer(modifier = Modifier.height(24.dp))
            if (uiState.isContactSaved) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp)); Text("Contact Saved!", style = MaterialTheme.typography.headlineSmall, color = Success)
                    Text(updatedContact.name, style = MaterialTheme.typography.titleLarge); Text(updatedContact.email, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { HapticFeedback.success(context); onReset() }, modifier = Modifier.testTag("scan-another-button")) { Icon(Icons.Default.CameraAlt, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Scan Another") }
                        OutlinedButton(onClick = { ShareHelper.shareVCard(context, updatedContact) }, modifier = Modifier.testTag("export-after-save-button")) { Icon(Icons.Default.Share, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Share") }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f).testTag("retake-button")) { Icon(Icons.Default.Refresh, contentDescription = null); Spacer(modifier = Modifier.width(4.dp)); Text("Retake") }
                    Button(onClick = { if (updatedContact.hasDetails()) { viewModel.saveContact(updatedContact, context); HapticFeedback.success(context); Toast.makeText(context, "Contact saved!", Toast.LENGTH_SHORT).show() } }, modifier = Modifier.weight(1f).testTag("save-contact-button")) { Icon(Icons.Default.Save, contentDescription = null); Spacer(modifier = Modifier.width(4.dp)); Text("Save") }
                    OutlinedButton(onClick = { if (updatedContact.hasDetails()) ShareHelper.shareVCard(context, updatedContact) }, modifier = Modifier.weight(1f).testTag("export-contact-button")) { Icon(Icons.Default.Share, contentDescription = null); Spacer(modifier = Modifier.width(4.dp)); Text("Export") }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    AnimatedVisibility(visible = uiState.showSuccess) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(72.dp)); Spacer(modifier = Modifier.height(16.dp)); Text("Saved!", color = Color.White, style = MaterialTheme.typography.headlineMedium) }
        }
        LaunchedEffect(Unit) { kotlinx.coroutines.delay(1500); viewModel.dismissSuccess() }
    }
}

private fun copyUriToCache(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "picked_${System.currentTimeMillis()}.jpg")
    inputStream?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
    return file.absolutePath
}
