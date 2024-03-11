package com.cool.devskytask.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.cool.devskytask.R


class MyAccessibilityService : AccessibilityService() {
    val TAG = "RecorderService"
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val source = event!!.source ?: return
        Log.e(TAG, "onAccessibilityEvent: ${event.source?.className}")
        Log.e(TAG, "onAccessibilityEventType: ${event.eventType}")
        /*  when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {*/
        val textNodeInfo = findTextViewNode(event.source)
        if (textNodeInfo != null) {
            val rect = Rect()
            textNodeInfo.getBoundsInScreen(rect)
            addAnimatedTextViewToWindow(this, rect, textNodeInfo.text.toString())
            Log.i(TAG, "The TextView Node: ${textNodeInfo.text}")
        } else {
            val rect = Rect()
            val item = findLastNonNullChildNode(source)
            item?.let {
                item.getBoundsInScreen(rect)
                addRippleLayoutToWindow(this, rect)
            }

        }

        /*   }

           AccessibilityEvent.TYPE_TOUCH_INTERACTION_END -> {
               val rect = Rect()
               val item = findLastNonNullChildNode(source)
               item?.let {
                   item.getBoundsInScreen(rect)
                   addRippleLayoutToWindow(this, rect)
               }
           }

           AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START -> {
               val rect = Rect()
               val item = findLastNonNullChildNode(source)
               item?.let {
                   item.getBoundsInScreen(rect)
                   addRippleLayoutToWindow(this, rect)
               }
           }

           AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED -> {
               val rect = Rect()
               val item = findLastNonNullChildNode(source)
               item?.let {
                   item.getBoundsInScreen(rect)
                   addRippleLayoutToWindow(this, rect)
               }
           }
*/
        /*else -> {
            val rect = Rect()
            source.getBoundsInScreen(rect)
            addRippleLayoutToWindow(this, rect)
        }*/

        // }


        /* val findAccessibilityNodeInfosByViewId =
             source.findAccessibilityNodeInfosByViewId("YOUR PACKAGE NAME:id/RESOURCE ID FROM WHERE YOU WANT DATA")*/
        //     Log.e(TAG, "onAccessibilityEvent: ${event.source.toString()}")
        /*if (findAccessibilityNodeInfosByViewId.size > 0) {
            // You can also traverse the list if required data is deep in view hierarchy. 
            val requiredText = findAccessibilityNodeInfosByViewId[0].text.toString()
            Log.i("Required Text", requiredText)
        }*/
        /* when (event?.eventType) {
             AccessibilityEvent.TYPE_TOUCH_INTERACTION_START -> {
                 Log.e("TAG", "onAccessibilityEvent: TYPE_TOUCH_INTERACTION_START")
                 val touchedView = event.source
                 if (isTextView(touchedView)) {
                     Log.e(TAG, "onAccessibilityEvent: textViewDetected")
                     val view = touchedView as TextView
                     Log.e(TAG, "onAccessibilityEvent: text is ${view.text}")
                     // The user is interacting with a TextView
                     // Handle the interaction as needed
                 }
             }

             AccessibilityEvent.TYPE_TOUCH_INTERACTION_END -> {
                 Toast.makeText(this,"touch",Toast.LENGTH_SHORT).show()
                 // Handle the end of touch interaction
                 Log.e(TAG, "onAccessibilityEvent: TYPE_TOUCH_INTERACTION_END")
             }
         }*/
    }

    // Function to add a ripple effect to a given rect
    fun addRippleEffectOld(view: View, rect: Rect) {
        // Create a ripple drawable with a transparent background
        val rippleDrawable = RippleDrawable(
            ColorStateList.valueOf(Color.RED),
            ColorDrawable(0xFFFFFFFF.toInt()),
            null
        )

        // Set the bounds of the ripple drawable to match the provided rect
        rippleDrawable.setBounds(rect)

        // Set the ripple drawable as the background of the view
        view.background = rippleDrawable
    }


    fun addAnimatedTextViewToWindow(context: Context, rect: Rect, text: String) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams().apply {
            // Set window type and flags for accessibility overlay
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            // Set layout parameters
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            // Set position based on the provided rect
            x = rect.left
            y = rect.top
            gravity = Gravity.TOP or Gravity.START
            format = PixelFormat.TRANSPARENT
        }
        val view = LayoutInflater.from(context).inflate(R.layout.animated_text_layout, null)
        val textParams = LinearLayout.LayoutParams(rect.width(), rect.height())
        // Find the TextView in the layout
        val animatedTextView = view.findViewById<TextView>(R.id.animatedTextView)
        animatedTextView.layoutParams = textParams

        animatedTextView.text = text

        // Load the custom animation
        //    val scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.text_scale_animation)
        // Apply the animation to the TextView
        windowManager.addView(view, layoutParams)
        //  animatedTextView.startAnimation(scaleAnimation)
        animatedTextView.animate().scaleX(2f).scaleY(2f).setDuration(2000).withEndAction(Runnable {
            windowManager.removeView(view)
        })

    }


    fun addRippleLayoutToWindow(context: Context, rect: Rect) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams().apply {
            // Set window type and flags for accessibility overlay
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            // Set layout parameters
            width = 200
            height = 200
            // Set position based on the provided rect
            x = rect.left
            y = rect.top
            gravity = Gravity.TOP or Gravity.START
            format = PixelFormat.TRANSPARENT
        }
        val view = LayoutInflater.from(context).inflate(R.layout.float_view, null)
        windowManager.addView(view, layoutParams)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 2000)
    }

    override fun onInterrupt() {
        Log.e(TAG, "Something went wrong");
        // Handle interruption
    }


    private fun isTextView(view: AccessibilityNodeInfo?): Boolean {
        if (view == null) return false
        // Check if the view is a TextView
        val className = view.className?.toString()
        return className == "android.widget.TextView"
    }

    var mLayout: FrameLayout? = null

    fun findTextViewNode(nodeInfo: AccessibilityNodeInfo?): AccessibilityNodeInfo? {


        if (nodeInfo == null) return null
        Log.v(TAG, nodeInfo.toString())


        if (nodeInfo.className.toString().contains(TextView::class.java.simpleName)) {
            return nodeInfo
        }

        for (i in 0 until nodeInfo.childCount) {
            val result = findTextViewNode(nodeInfo.getChild(i))
            if (result != null) return result
        }
        return null
    }

    fun findLastNonNullChildNode(nodeInfo: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        // Safety check to handle null nodeInfo
        if (nodeInfo == null) return null

        var lastNonNullChild: AccessibilityNodeInfo? = null

        // Iterate through each child node
        for (i in 0 until nodeInfo.childCount) {
            val childNode = nodeInfo.getChild(i)

            // Recursively find the last non-null child node
            val lastChild = findLastNonNullChildNode(childNode)

            // Update lastNonNullChild if the current child is not null
            if (lastChild != null) {
                lastNonNullChild = lastChild
            }

            // Recycle the childNode to avoid memory leaks
            // childNode.recycle()
        }

        // Return the last non-null child node found
        return lastNonNullChild ?: nodeInfo
    }


}

