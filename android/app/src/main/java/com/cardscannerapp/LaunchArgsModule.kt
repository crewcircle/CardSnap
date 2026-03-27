package com.cardscannerapp

import android.os.Bundle
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class LaunchArgsModule(
    reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "LaunchArgs"

    override fun getConstants(): MutableMap<String, Any> {
        val launchArgs = currentLaunchArgs()
        return mutableMapOf("launchArgs" to launchArgs)
    }

    @ReactMethod
    fun getLaunchArgs(promise: Promise) {
        promise.resolve(Arguments.makeNativeMap(currentLaunchArgs()))
    }

    private fun currentLaunchArgs(): Map<String, Any> {
        val args = mutableMapOf<String, Any>()

        // 1. Try to get extras from the current activity's intent
        val activity = currentActivity
        if (activity != null) {
            val intent = activity.intent
            val extras = intent?.extras
            if (extras != null) {
                extras.keySet().forEach { key ->
                    val value = extras.get(key)
                    if (value != null) {
                        args[key] = value
                    }
                }
            }
        }

        // 2. Also check the static MainActivity.launchArgs bundle for backward compatibility or if activity is not ready
        val staticBundle = MainActivity.launchArgs
        if (staticBundle != null) {
            staticBundle.keySet().forEach { key ->
                val value = staticBundle.get(key)
                if (value != null) {
                    args[key] = value
                }
            }
        }
        
        return args
    }
}
