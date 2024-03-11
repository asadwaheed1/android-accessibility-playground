package com.cool.devskytask.windows

import android.content.Context
import android.gesture.GestureOverlayView
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.cool.devskytask.R


class Window(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val rootView = layoutInflater.inflate(R.layout.window, null)

    private val windowParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        0,
        0,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        },

                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL ,
        PixelFormat.TRANSLUCENT

    )


    private fun getCurrentDisplayMetrics(): DisplayMetrics {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm
    }


    private fun calculateSizeAndPosition(
         params: WindowManager.LayoutParams,
        widthInDp: Int,
        heightInDp: Int
    ) {

        val dm = getCurrentDisplayMetrics()
        // We have to set gravity for which the calculated position is relative.
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.width = dm.widthPixels
        // params.width = (widthInDp * dm.density).toInt()
        //params.height = (heightInDp * dm.density).toInt()
        params.height = dm.heightPixels
        params.x = 0
        // params.x = (dm.widthPixels - params.width) / 2
        //  params.y = (dm.heightPixels - params.height) / 2
        params.y = 0

    }


    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, 300, 80)
    }


    private fun initWindow() {
        // Using kotlin extension for views caused error, so good old findViewById is used
        /*rootView.findViewById<View>(R.id.window_close).setOnClickListener { close() }
        rootView.findViewById<View>(R.id.content_button).setOnClickListener {
            Toast.makeText(context, "Adding notes to be implemented.", Toast.LENGTH_SHORT).show()
        }*/
    }


    init {
        initWindowParams()
        initWindow()
    }


    fun open(): View? {
        try {
            val gest = GestureOverlayView(this.context,)
            windowManager.addView(rootView, windowParams)
            return rootView
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
        return null
    }


    fun close() {
        try {
            windowManager.removeView(rootView)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
    }

}