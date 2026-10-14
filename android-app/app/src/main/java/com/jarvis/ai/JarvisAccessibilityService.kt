package com.jarvis.ai

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "JarvisAccessibility"

        @Volatile
        var instance: JarvisAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        serviceInfo = serviceInfo.apply {
            eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_CLICKED or
                AccessibilityEvent.TYPE_VIEW_SCROLLED

            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC

            flags =
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        Log.d(TAG, "Jarvis Accessibility READY")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Screen/UI events are available to Jarvis.
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility interrupted")
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    fun getScreenText(): String {
        val root = rootInActiveWindow ?: return ""

        val out = StringBuilder()
        collectText(root, out)

        return out.toString().trim()
    }

    private fun collectText(
        node: AccessibilityNodeInfo?,
        out: StringBuilder
    ) {
        if (node == null) return

        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrEmpty()) {
            out.append(text).append('\n')
        } else if (!desc.isNullOrEmpty()) {
            out.append(desc).append('\n')
        }

        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), out)
        }
    }

    fun clickText(target: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val wanted = target.trim().lowercase()

        val nodes = root.findAccessibilityNodeInfosByText(target)

        for (node in nodes) {
            if (clickNode(node)) return true
        }

        return clickMatchingNode(root, wanted)
    }

    private fun clickMatchingNode(
        node: AccessibilityNodeInfo?,
        wanted: String
    ): Boolean {
        if (node == null) return false

        val text =
            node.text?.toString()?.lowercase()?.trim() ?: ""

        val desc =
            node.contentDescription?.toString()?.lowercase()?.trim() ?: ""

        if (
            text == wanted ||
            desc == wanted ||
            text.contains(wanted) ||
            desc.contains(wanted)
        ) {
            if (clickNode(node)) return true
        }

        for (i in 0 until node.childCount) {
            if (clickMatchingNode(node.getChild(i), wanted)) {
                return true
            }
        }

        return false
    }

    private fun clickNode(
        node: AccessibilityNodeInfo
    ): Boolean {
        var current: AccessibilityNodeInfo? = node

        repeat(8) {
            if (current == null) return false

            if (
                current!!.isClickable &&
                current!!.isEnabled
            ) {
                return current!!.performAction(
                    AccessibilityNodeInfo.ACTION_CLICK
                )
            }

            current = current!!.parent
        }

        return false
    }

    fun scrollForward(): Boolean {
        val root = rootInActiveWindow ?: return false
        return root.performAction(
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        )
    }

    fun scrollBackward(): Boolean {
        val root = rootInActiveWindow ?: return false
        return root.performAction(
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        )
    }

    fun pressBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun pressHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun openRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    fun screenBounds(): Rect? {
        val root = rootInActiveWindow ?: return null
        val rect = Rect()
        root.getBoundsInScreen(rect)
        return rect
    }
}
