package com.cardscannerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cardscannerapp.ui.theme.CardSnapTheme

class MainActivity : ComponentActivity() {
    companion object { var pendingDeepLinkUri: String? = null }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)
        setContent { CardSnapTheme { AppNavigation() } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null && data.scheme == "cardscanner" && data.host == "inject") {
            pendingDeepLinkUri = data.getQueryParameter("imageUri")
        }
    }
}
