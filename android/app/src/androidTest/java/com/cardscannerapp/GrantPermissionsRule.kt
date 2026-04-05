package com.cardscannerapp
import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class GrantPermissionsRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            grantPermission(Manifest.permission.CAMERA)
            grantPermission(Manifest.permission.READ_CONTACTS)
            grantPermission(Manifest.permission.WRITE_CONTACTS)
            base.evaluate()
        }
    }
    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            ApplicationProvider.getApplicationContext<Context>().packageName, permission)
    }
}
