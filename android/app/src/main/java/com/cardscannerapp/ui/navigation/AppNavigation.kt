package com.cardscannerapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.cardscannerapp.MainActivity
import com.cardscannerapp.ui.screens.contacts.ContactsScreen
import com.cardscannerapp.ui.screens.editcontact.EditContactScreen
import com.cardscannerapp.ui.screens.scan.ScanScreen
import com.cardscannerapp.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "scan") {
        composable(
            route = "scan",
            deepLinks = listOf(navDeepLink { uriPattern = "cardscanner://inject?imageUri={imageUri}" }),
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: MainActivity.pendingDeepLinkUri
            LaunchedEffect(imageUri) { if (imageUri != null) MainActivity.pendingDeepLinkUri = null }
            ScanScreen(imageUri = imageUri,
                onNavigateToContacts = { navController.navigate("contacts") },
                onNavigateToSettings = { navController.navigate("settings") })
        }
        composable("contacts") {
            ContactsScreen(
                onNavigateToEdit = { contactId -> navController.navigate("edit_contact/$contactId") },
                onNavigateBack = { navController.popBackStack() })
        }
        composable(route = "edit_contact/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            EditContactScreen(contactId = contactId, onNavigateBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
