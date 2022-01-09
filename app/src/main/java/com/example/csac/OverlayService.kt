package com.example.csac

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import com.example.csac.databinding.OverlayMenuBinding

// To add another view, just add it with a new layoutparam and call windowManager.addView()
class OverlayService : Service(), View.OnClickListener {
    private lateinit var menu: OverlayMenuBinding
    private lateinit var autoClickIntent: Intent
    private lateinit var windowManager: WindowManager
    private var playing = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val layoutParams = createOverlayLayout(55, 165, Gravity.START)
        menu = OverlayMenuBinding.inflate(LayoutInflater.from(applicationContext))
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(menu.root, layoutParams)
        autoClickIntent = Intent(applicationContext, AutoClickService::class.java)

        // Add event listeners
        val draggable = Draggable(windowManager, layoutParams, menu.root)
        menu.root.setOnTouchListener(draggable)
        menu.playButton.setOnTouchListener(draggable)
        menu.plusButton.setOnTouchListener(draggable)
        menu.minusButton.setOnTouchListener(draggable)
        menu.playButton.setOnClickListener(this)
        menu.plusButton.setOnClickListener(this)
        menu.minusButton.setOnClickListener(this)

        makeNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    // Don't bind this service to anything
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Destroy created views when this service is stopped
    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(menu.root)
    }

    override fun onClick(p0: View?) {
        when(p0!!.id) {
            R.id.playButton -> {
                playing = !playing
                if(playing) {
                    menu.playButton.setImageResource(R.drawable.pause)
                    applicationContext.startService(autoClickIntent)
                } else {
                    menu.playButton.setImageResource(R.drawable.play)
                    applicationContext.stopService(autoClickIntent)
                }
            }
            R.id.plusButton -> {
                println("plus clicked")
            }
            R.id.minusButton -> {
                println("minus clicked")
            }
        }
    }

    private fun createOverlayLayout(width: Int, height: Int, gravity: Int): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams()
        // Convert width and height from pixels to dp
        layoutParams.width = (width * applicationContext.resources.displayMetrics.density).toInt()
        layoutParams.height = (height * applicationContext.resources.displayMetrics.density).toInt()
        // Display this on top of other applications
        layoutParams.type = if(Build.VERSION.SDK_INT >= 26) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("Deprecation")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        // Don't grab input focus
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        // Make the underlying application visible through any transparent sections
        layoutParams.format = PixelFormat.TRANSLUCENT
        // Position layout using gravity
        layoutParams.gravity = gravity
        return layoutParams
    }

    private fun makeNotification() {
        if(Build.VERSION.SDK_INT >= 26) {
            // Create notification channel for foreground service
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "csac_overlay",
                getString(R.string.overlay_notification),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)

            // Send notification
            val builder = Notification.Builder(this, "csac_overlay")
            startForeground(1, builder.build())
        }
    }
}