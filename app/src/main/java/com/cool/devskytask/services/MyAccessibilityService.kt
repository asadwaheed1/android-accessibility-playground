package com.cool.devskytask.services

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.TextView
import com.cool.devskytask.R


/**
 * Highlights the view the user interacts with: text views get an enlarged copy of their text,
 * anything else gets a ripple. Only one overlay is shown at a time.
 */
class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"
    private val OVERLAY_DURATION_MS = 2000L
    private val TEXT_SCALE = 2f

    private val handler = Handler(Looper.getMainLooper())
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private var currentOverlay: View? = null
    private val removeOverlayRunnable = Runnable { removeCurrentOverlay() }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val source = event.source ?: return
        Log.d(TAG, "onAccessibilityEvent: type=${event.eventType} class=${source.className}")

        val textNode = findTextViewNode(source)
        val target = textNode ?: findDeepestLastNode(source)
        val rect = Rect()
        target.getBoundsInScreen(rect)
        val text = textNode?.text?.toString()

        if (target !== source) target.recycleCompat()
        source.recycleCompat()

        if (rect.isEmpty) return
        if (text != null) {
            showTextOverlay(rect, text)
        } else {
            showRippleOverlay(rect)
        }
    }

    private fun showTextOverlay(rect: Rect, text: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.animated_text_layout, null)
        val animatedTextView = view.findViewById<TextView>(R.id.animatedTextView)
        animatedTextView.layoutParams =
            FrameLayout.LayoutParams(rect.width(), rect.height(), Gravity.CENTER)
        animatedTextView.text = text

        // Make the window big enough to hold the scaled text so it isn't clipped.
        val width = (rect.width() * TEXT_SCALE).toInt()
        val height = (rect.height() * TEXT_SCALE).toInt()
        showOverlay(view, rect.centerX() - width / 2, rect.centerY() - height / 2, width, height)

        animatedTextView.animate()
            .scaleX(TEXT_SCALE)
            .scaleY(TEXT_SCALE)
            .setDuration(OVERLAY_DURATION_MS)
    }

    private fun showRippleOverlay(rect: Rect) {
        val size = resources.getDimensionPixelSize(R.dimen.ripple_overlay_size)
        val view = LayoutInflater.from(this).inflate(R.layout.float_view, null)
        showOverlay(view, rect.centerX() - size / 2, rect.centerY() - size / 2, size, size)
    }

    /** Replaces any overlay currently on screen with [view] and removes it after a delay. */
    private fun showOverlay(view: View, x: Int, y: Int, width: Int, height: Int) {
        removeCurrentOverlay()
        val layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            // Overlays are visual only: never take focus or touches from the app underneath,
            // and use raw screen coordinates so they line up with getBoundsInScreen().
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            this.width = width
            this.height = height
            this.x = x
            this.y = y
            gravity = Gravity.TOP or Gravity.START
            format = PixelFormat.TRANSLUCENT
        }
        try {
            windowManager.addView(view, layoutParams)
            currentOverlay = view
            handler.postDelayed(removeOverlayRunnable, OVERLAY_DURATION_MS)
        } catch (e: Exception) {
            Log.e(TAG, "Could not add overlay", e)
        }
    }

    private fun removeCurrentOverlay() {
        handler.removeCallbacks(removeOverlayRunnable)
        val view = currentOverlay ?: return
        currentOverlay = null
        try {
            windowManager.removeView(view)
        } catch (e: Exception) {
            Log.e(TAG, "Could not remove overlay", e)
        }
    }

    override fun onInterrupt() {
        removeCurrentOverlay()
    }

    override fun onDestroy() {
        removeCurrentOverlay()
        super.onDestroy()
    }

    /** Returns the first TextView in the tree rooted at [nodeInfo], or null if there is none. */
    private fun findTextViewNode(nodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (nodeInfo.className?.toString()?.contains(TextView::class.java.simpleName) == true) {
            return nodeInfo
        }
        for (i in 0 until nodeInfo.childCount) {
            val child = nodeInfo.getChild(i) ?: continue
            val result = findTextViewNode(child)
            if (result !== child) child.recycleCompat()
            if (result != null) return result
        }
        return null
    }

    /** Returns the deepest node reached by always following the last child. */
    private fun findDeepestLastNode(nodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo {
        for (i in nodeInfo.childCount - 1 downTo 0) {
            val child = nodeInfo.getChild(i) ?: continue
            val result = findDeepestLastNode(child)
            if (result !== child) child.recycleCompat()
            return result
        }
        return nodeInfo
    }

    /** Nodes must be recycled before Android 13; from 13 on recycle() is a deprecated no-op. */
    @Suppress("DEPRECATION")
    private fun AccessibilityNodeInfo.recycleCompat() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) recycle()
    }
}
