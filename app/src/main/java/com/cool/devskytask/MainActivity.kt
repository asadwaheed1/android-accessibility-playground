package com.cool.devskytask

import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cool.devskytask.databinding.ActivityMainBinding
import com.cool.devskytask.services.MyAccessibilityService
import com.cool.devskytask.services.ScreenCaptureService
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {
    private var mediaProjection: MediaProjection? = null
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private val OVERLAY_PERMISSION_REQUEST_CODE: Int = 88
    private val SCREEN_RECORD_REQUEST_CODE: Int = 90
    private val TAG: String = "MainActivity"
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*if ( !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${packageName}")
            )
        //    startActivityForResult(intent)
            overDrawLauncher.launch(intent)
        }
        // Inside your activity or service
        // Inside your activity or service
        val REQUEST_CODE_SCREEN_CAPTURE = 123 // Choose any request code*/


// Request screen capture permission

// Request screen capture permission
        // Create a screen capture intent

        binding.button.setOnClickListener {
            // requestNotif()
            openAccessibilitySettings()
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    val pushNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            sendNotification()
        }
    var overDrawLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            // Handle the result here
            Log.e(TAG, "permission granted: overrid")
            // ...
        } else {
            // Handle other result codes (e.g., canceled)
        }
    }

    var screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            // Handle the result here
            /*    val intent = Intent(this, ScreenCaptureService::class.java)
                intent.putExtra("resultCode",result.resultCode)
                intent.putExtra("data",result.data)
                startForegroundService(intent)*/
            val intentAcc = Intent(this, MyAccessibilityService::class.java)
            //   intent.putExtra("resultCode",result.resultCode)
            //  intent.putExtra("data",result.data)
            startService(intentAcc)
            //  mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
            //  this.finish();
            // ...
        } else {
            // Handle other result codes (e.g., canceled)
        }
    }

    fun requestNotif() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) ==
                    PackageManager.PERMISSION_GRANTED -> {
                Log.e(TAG, "User accepted the notifications!")
                sendNotification()
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                Snackbar.make(
                    binding.root,
                    "The user denied the notifications ):",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Settings") {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val uri: Uri =
                            Uri.fromParts("com.onesilisondiode.geeksforgeeks", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .show()
            }

            else -> {
                pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        /*private fun startRecordingScreen() {
            val mediaProjectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val permissionIntent = mediaProjectionManager?.createScreenCaptureIntent()
            startActivityForResult(permissionIntent!!, SCREEN_RECORD_REQUEST_CODE)
        }*/

        /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    //Start screen recording
                    val intent = Intent(this,ScreenCaptureService::class.java)
                    startForegroundService(intent)
                }
            }
        }*/
    }

    lateinit var screenCaptureIntent: Intent
    fun sendNotification() {
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()

        screenCaptureLauncher.launch(screenCaptureIntent)

    }

}

