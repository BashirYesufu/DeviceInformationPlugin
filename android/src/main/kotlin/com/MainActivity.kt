package com.jaiz.mobiletoken

import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.provider.Settings
import android.os.Build
import android.content.Context
import android.util.Log
import java.util.*

class MainActivity: FlutterFragmentActivity() {
    private val CHANNEL = "device_information"
    private val TAG = "DeviceInfo"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getIMEINumber" -> {
                    try {
                        val deviceId = getDeviceIdentifier()
                        result.success(deviceId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to get device ID", e)
                        result.error("DEVICE_ID_ERROR", "Failed to get device ID", e.localizedMessage)
                    }
                }
                "getModel" -> {
                    result.success(Build.MODEL)
                }
                "getManufacturer" -> {
                    result.success(Build.BRAND)
                }
                "getDevice" -> {
                    result.success(Build.DEVICE)
                }
                "getBrand" -> {
                    result.success(Build.BRAND)
                }
                "getManufacturer" -> {
                    result.success(Build.MANUFACTURER)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun getDeviceIdentifier(): String {
        return try {
            // Always use ANDROID_ID as it's the most reliable and privacy-compliant option
            val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            
            if (androidId.isNullOrEmpty() || androidId == "9774d56d682e549c") {
                // Fallback: Generate a unique identifier and store it persistently
                getOrCreateUniqueId()
            } else {
                androidId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Android ID", e)
            // Final fallback: Generate a unique identifier
            getOrCreateUniqueId()
        }
    }

    private fun getOrCreateUniqueId(): String {
        val prefs = getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        var uniqueId = prefs.getString("unique_device_id", null)
        
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString()
            prefs.edit().putString("unique_device_id", uniqueId).apply()
        }
        
        return uniqueId
    }
}