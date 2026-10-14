package com.jarvis.ai

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    companion object {
        private const val RECORD_AUDIO_REQUEST = 100
        private const val NOTIFICATION_REQUEST = 101
        private const val ASSISTANT_ROLE_REQUEST = 102
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 70, 40, 40)
        }

        val title = TextView(this).apply {
            text = "🤖 Jarvis AI"
            textSize = 30f
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            text =
                "Hands-Free Jarvis\n\n" +
                "Voice + screen control\n\n" +
                "1. Microphone allow karo.\n" +
                "2. Jarvis ko Assistant banao.\n" +
                "3. Accessibility ON karo.\n\n" +
                "Uske baad supported voice commands se screen actions test kar sakte ho."
            textSize = 18f
            setPadding(0, 40, 0, 40)
        }

        val permissionButton = Button(this).apply {
            text = "Allow Permissions"
            setOnClickListener {
                requestRequiredPermissions()
            }
        }

        val assistantButton = Button(this).apply {
            text = "Choose Jarvis as Assistant"
            setOnClickListener {
                requestAssistantRole()
            }
        }

        val accessibilityButton = Button(this).apply {
            text = "Enable Screen Control"
            setOnClickListener {
                openAccessibilitySettings()
            }
        }

        box.addView(title)
        box.addView(status)
        box.addView(permissionButton)
        box.addView(assistantButton)
        box.addView(accessibilityButton)

        setContentView(box)

        requestRequiredPermissions()
    }

    private fun requestRequiredPermissions() {
        if (
            Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                RECORD_AUDIO_REQUEST
            )
            return
        }

        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                NOTIFICATION_REQUEST
            )
        }
    }

    private fun requestAssistantRole() {
        if (Build.VERSION.SDK_INT >= 29) {

            val roleManager =
                getSystemService(RoleManager::class.java)

            if (
                roleManager != null &&
                roleManager.isRoleAvailable(
                    RoleManager.ROLE_ASSISTANT
                )
            ) {

                if (
                    roleManager.isRoleHeld(
                        RoleManager.ROLE_ASSISTANT
                    )
                ) {
                    return
                }

                startActivityForResult(
                    roleManager.createRequestRoleIntent(
                        RoleManager.ROLE_ASSISTANT
                    ),
                    ASSISTANT_ROLE_REQUEST
                )

                return
            }
        }

        try {
            startActivity(
                Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
            )
        } catch (e: Exception) {
            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )
        }
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(
                Intent(
                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                )
            )
        } catch (e: Exception) {
            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == RECORD_AUDIO_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermissionIfNeeded()
        }
    }
}
