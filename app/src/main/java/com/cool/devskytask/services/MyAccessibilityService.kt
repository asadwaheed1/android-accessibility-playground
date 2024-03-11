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
            y = rect.bottom
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


        // Remove the view after animation completes
        /*scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                windowManager.removeView(view)
            }
        })*/
        /* Handler(Looper.getMainLooper()).postDelayed({
             windowManager.removeView(view)
         }, 2000)*/
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
            x = (rect.left + rect.right) / 2
            y = (rect.top + rect.bottom) / 2
            gravity = Gravity.TOP or Gravity.START
            format = PixelFormat.TRANSPARENT
        }
        val view = LayoutInflater.from(context).inflate(R.layout.float_view, null)
        //val textParams = LinearLayout.LayoutParams(rect.width(), rect.height())
        //  view.layoutParams = textParams
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

    /*override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes =
            AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_TOUCH_INTERACTION_END or AccessibilityEvent.TYPE_TOUCH_INTERACTION_START
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        info.notificationTimeout = 100
        this.serviceInfo = info
        *//*  val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
          mLayout = FrameLayout(this)
          val layoutParams = WindowManager.LayoutParams()
          layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
          layoutParams.format = PixelFormat.TRANSLUCENT
          layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
          layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
          layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
          layoutParams.gravity = Gravity.LEFT
          val inflater = LayoutInflater.from(this)
          inflater.inflate(R.layout.floating_bar, mLayout)
          windowManager.addView(mLayout, layoutParams)
          configurePowerButton()
          configureSimulateTouch()
          configureVolumeButton()
          configureScrollButton()
          configureSwipeButton()*//*
    }
*/
    fun findTextViewNode(nodeInfo: AccessibilityNodeInfo?): AccessibilityNodeInfo? {

        //I highly recommend leaving this line in! You never know when the screen content will
        //invalidate a node you're about to work on, or when a parents child will suddenly be gone!
        //Not doing this safety check is very dangerous!
        if (nodeInfo == null) return null
        Log.v(TAG, nodeInfo.toString())

        //Notice that we're searching for the TextView's simple name!
        //This allows us to find AppCompat versions of TextView as well
        //as 3rd party devs well names subclasses... though with perhaps
        //a few poorly named unintended stragglers!
        if (nodeInfo.className.toString().contains(TextView::class.java.simpleName)) {
            return nodeInfo
        }

        //Do other work!
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
            childNode.recycle()
        }

        // Return the last non-null child node found
        return lastNonNullChild ?: nodeInfo
    }


    private fun configurePowerButton() {
        val power = mLayout?.findViewById(R.id.power) as Button
        power.setOnClickListener { performGlobalAction(GLOBAL_ACTION_POWER_DIALOG) }
    }

    private fun configureVolumeButton() {
        val volumeUpButton = mLayout?.findViewById(R.id.volume_up) as Button
        volumeUpButton.setOnClickListener {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
            )
        }
    }

    private fun configureSimulateTouch() {
        val btnSimulateTouch = mLayout?.findViewById(R.id.simulateTouch) as Button
        btnSimulateTouch.setOnClickListener {
            Log.e(TAG, "onClick: Simulate Touch")
            val tap = Path()
            tap.moveTo(110F, 50F)
            val tapBuilder = GestureDescription.Builder()
            tapBuilder.addStroke(StrokeDescription(tap, 0, 500))
            dispatchGesture(tapBuilder.build(), null, null)
        }
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque: ArrayDeque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node
            }
            for (i in 0 until node.childCount) {
                deque.addLast(node.getChild(i))
            }
        }
        return null
    }

    private fun configureScrollButton() {
        val scrollButton = mLayout?.findViewById(R.id.scroll) as Button
        scrollButton.setOnClickListener {
            val scrollable = findScrollableNode(rootInActiveWindow)
            scrollable?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
        }
    }

    private fun configureSwipeButton() {
        val swipeButton = mLayout?.findViewById(R.id.swipe) as Button
        swipeButton.setOnClickListener {
            val swipePath = Path()
            swipePath.moveTo(1000F, 1000F)
            swipePath.lineTo(100F, 1000F)
            val gestureBuilder = GestureDescription.Builder()
            gestureBuilder.addStroke(StrokeDescription(swipePath, 0, 500))
            dispatchGesture(gestureBuilder.build(), null, null)
        }
    }

}

