package com.cool.devskytask.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.cool.devskytask.br.NotificationReceiver
import com.cool.devskytask.windows.Window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class ScreenCaptureService : Service() {
    private var mResultCode: Int = 0
    private var mResultData: Intent? = null
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var job: Job = Job()
    private lateinit var imageReader: ImageReader
    private var touchSlop: Int = 0
    private var touchX = 0
    private var touchY = 0
    private lateinit var touchOverlay: View
    private lateinit var gestureDetector: GestureDetector
    private lateinit var myOverlayView: View
 //   private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        //addTouchOverlay()
    }

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Capture touch coordinates
            touchX = e.x.toInt()
            touchY = e.y.toInt()
            Log.e("MYCap", "onSingleTapUp: working")
            // Draw text overlay at touch coordinates
          //  drawTextOverlay(touchX, touchY)
            val image: Image = imageReader.acquireLatestImage()
            processCapturedImage(image)
            image.close()
            // Indicate that touch was detected
            return false
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return false
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return false
        }

        /* override fun onDown(e: MotionEvent): Boolean {
             Log.e("MyGEsture", "onDown: detected")
             return super.onDown(e)
         }*/
    }

    private fun addOverlayView() {
    //    windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        gestureDetector = GestureDetector(this, MyGestureListener())
        // Create your overlay view (e.g., a LinearLayout)
        /*  myOverlayView = LinearLayout(this)
          myOverlayView.setLayoutParams(
              WindowManager.LayoutParams(
                  WindowManager.LayoutParams.MATCH_PARENT,
                  WindowManager.LayoutParams.MATCH_PARENT
              )
          )
          // Add the overlay view to the window manager
          val params = WindowManager.LayoutParams(
              WindowManager.LayoutParams.WRAP_CONTENT,
              WindowManager.LayoutParams.WRAP_CONTENT,
              WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
              WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
              PixelFormat.TRANSLUCENT
          )*/
        try {
            val window = Window(this)
            window.open()?.let {
                myOverlayView = it
                // Attach the gesture detector to the overlay view
                myOverlayView.setOnTouchListener(touchListener)

            }
        } catch (e: Exception) {
            Log.e("TAG", "Error adding overlay view: ${e.message}")
        }

    }

    var touchListener = OnTouchListener { v, event ->
        Log.e("touchlistener", "event: ${event.action}")
        // Pass touch events to the gesture detector
       // v.performClick()

       // gestureDetector.onTouchEvent(event)
        false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //startForeground(FOREGROUND_SERVICE_ID, createNotification())
        mResultData = intent!!.getParcelableExtra<Intent>("data")
        mResultCode = intent!!.getIntExtra("resultCode", 0)
        intent?.let {
            /* Log.e("TAG", "onStartCommand: start called")
            startScreenCapture()*/
            //Notification
            //Set notification notification button text if developer did not
            var notificationTitle = "dev"
            var notificationDescription = "test"
            var notificationButtonText = "btn"
            //Set notification notification button text if developer did not
            if (notificationButtonText == null) {
                notificationButtonText = "STOP RECORDING"
            }


            //Set notification title if developer did not
            //Set notification title if developer did not
            if (notificationTitle == null || notificationTitle == "") {
                notificationTitle = "stop"
            }
            //Set notification description if developer did not
            //Set notification description if developer did not
            if (notificationDescription == null || notificationDescription == "") {
                notificationDescription = "stop message"
            }
            //Notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "001"
                val channelName = "RecordChannel"
                val channel =
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
                channel.lightColor = Color.BLUE
                channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                if (manager != null) {
                    manager.createNotificationChannel(channel)
                    val notification: Notification
                    val myIntent = Intent(this, NotificationReceiver::class.java)
                    val pendingIntent: PendingIntent
                    pendingIntent = if (Build.VERSION.SDK_INT >= 31) {
                        PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_IMMUTABLE)
                    } else {
                        PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_IMMUTABLE)
                    }
                    val action = Notification.Action.Builder(
                        Icon.createWithResource(this, R.drawable.presence_video_online),
                        notificationButtonText,
                        pendingIntent
                    ).build()


                    notification =
                        Notification.Builder(applicationContext, channelId).setOngoing(true)
                            .setSmallIcon(
                                Icon.createWithResource(this, R.drawable.ic_dialog_info)
                            ).setContentTitle(notificationTitle)
                            .setContentText(notificationDescription).addAction(action).build()
                    startFgs(101, notification)
                }

            } else {
                startFgs(101, Notification())
            }
            startScreenCapture()
            addOverlayView()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScreenCapture()
        job.cancel()
    }

    private fun startFgs(notificationId: Int, notificaton: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                notificationId,
                notificaton,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(notificationId, notificaton)
        }
    }


    private fun createNotification(): Notification {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("screen_capture", "Screen Capture Service")
            } else {
                ""
            }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Screen Capture Service")
            .setContentText("Capturing screen content")
            .setSmallIcon(androidx.core.R.drawable.ic_call_answer_low)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    private var mediaProjectionCallback: MediaProjection.Callback? = null
    private fun startScreenCapture() {
        mediaProjection = mediaProjectionManager.getMediaProjection(mResultCode, mResultData!!)
        /* mediaProjection = (Objects.requireNonNull<Any>(
             getSystemService(
                 MEDIA_PROJECTION_SERVICE
             )
         ) as MediaProjectionManager).getMediaProjection(
             mResultCode,
             mResultData!!
         )*/

        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val density = displayMetrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        mediaProjectionCallback = object : MediaProjection.Callback() {
            override fun onStop() {
                // Release resources when the capture session is stopped
                stopScreenCapture()
            }

            override fun onCapturedContentResize(width: Int, height: Int) {
                Log.e("TAG", "onCapturedContentResize: called widht = $width")
            }
        }
        mediaProjection?.registerCallback(mediaProjectionCallback!!, handler)
        mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            object : VirtualDisplay.Callback() {
                override fun onPaused() {
                    super.onPaused()
                    Log.e("TAG", "onPaused: VirtualDisplay.Callback")
                }

                override fun onResumed() {
                    super.onResumed()
                    Log.e("TAG", "onResumed: VirtualDisplay.Callback")
                }

                override fun onStopped() {
                    super.onStopped()
                    Log.e("TAG", "onStopped: VirtualDisplay.Callback")
                }
            },
            handler
        )
        imageReader.setOnImageAvailableListener({ reader ->
            //   val image: Image = reader.acquireLatestImage()
            Log.e("img", "startScreenCapture: ")
            // Process the captured image here
            //*//* processCapturedImage(image)
            // image.close()*//*
        }, handler)
    }
    /* private fun startScreenCapture(intent: Intent) {
         mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, intent)
         val displayMetrics = resources.displayMetrics
         val width = displayMetrics.widthPixels
         val height = displayMetrics.heightPixels
         val density = displayMetrics.densityDpi

         imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
         mediaProjectionCallback = object : MediaProjection.Callback() {
             override fun onStop() {
                 // Release resources when the capture session is stopped
                 stopScreenCapture()
             }

             override fun onCapturedContentResize(width: Int, height: Int) {
                 Log.e("TAG", "onCapturedContentResize: called widht = $width")
             }
         }
         mediaProjection?.registerCallback(mediaProjectionCallback!!, null)
         mediaProjection?.createVirtualDisplay(
             "ScreenCapture",
             width,
             height,
             density,
             DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
             imageReader.surface,
             object : VirtualDisplay.Callback() {

             },
             null
         )
         imageReader.setOnImageAvailableListener({ reader ->
             //   val image: Image = reader.acquireLatestImage()
             Log.e("img", "startScreenCapture: ")
             // Process the captured image here
             *//* processCapturedImage(image)
             image.close()*//*
        }, null)
    }*/

    private fun stopScreenCapture() {
        mediaProjectionCallback?.let {
            mediaProjection?.unregisterCallback(it)
        }
        mediaProjection?.stop()
        imageReader?.close()
        // Additional cleanup as needed...
    }

    private fun processCapturedImage(image: Image) {
        val planes = image.planes
        val buffer = planes[0].buffer
        val width = image.width
        val height = image.height
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)

        // Perform text detection and visual cues based on gesture detection
        // This is handled by the GestureDetector

        // Ensure that the gesture detector is properly initialized
        /* if (::gestureDetector.isInitialized) {
             // Simulate a MotionEvent based on the captured touch coordinates
             val motionEvent = MotionEvent.obtain(
                 SystemClock.uptimeMillis(),
                 SystemClock.uptimeMillis(),
                 MotionEvent.ACTION_DOWN,
                 touchX.toFloat(),
                 touchY.toFloat(),
                 0
             )
             // Handle the gesture detection on the captured bitmap
             gestureDetector.onTouchEvent(motionEvent)
         }*/
    }


    private fun drawTextOverlay(x: Int, y: Int) {
        val canvas = Canvas()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.BLACK
        paint.textSize = 50f // Adjust text size as needed
        val text = "Touch Detected"
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val width = bounds.width()
        val height = bounds.height()

        // Draw text at touch coordinates
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        canvas.drawText(text, 0f, height.toFloat(), paint)

        // Add the bitmap to the window manager to display the text overlay
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.x = x
        layoutParams.y = y
       /* windowManager.addView(
            ImageView(applicationContext).apply { setImageBitmap(bitmap) },
            layoutParams
        )*/
    }

    companion object {
        private const val FOREGROUND_SERVICE_ID = 1001
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.e("TAG", "onTaskRemoved: service removed")
    }
}
