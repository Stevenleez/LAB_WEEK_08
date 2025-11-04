package com.example.lab_week_08

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.lab_week_08.NotificationService.Companion.NOTIFICATION_ID

class SecondNotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler
    private lateinit var handlerThread: HandlerThread

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // background thread for countdown work
        handlerThread = HandlerThread("SecondFGSThread").apply { start() }
        serviceHandler = Handler(handlerThread.looper)

        notificationBuilder = startForegroundService()
    }

    override fun onDestroy() {
        if (::handlerThread.isInitialized) handlerThread.quitSafely()
        super.onDestroy()
    }

    private fun startForegroundService(): NotificationCompat.Builder {

        val pendingIntent = getPendingIntent()

        val channelId = createNotificationChannel()


        val notificationBuilder = getNotificationBuilder(
            pendingIntent, channelId
        )

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        return notificationBuilder
    }

    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            FLAG_IMMUTABLE else 0

        return PendingIntent.getActivity(
            this, 0, Intent(
                this,
                MainActivity::class.java
            ), flag
        )
    }

    private fun createNotificationChannel(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "001"
            val channelName = "001 Channel"

            val channelPriority = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(
                channelId,
                channelName,
                channelPriority
            )

            val service = requireNotNull(
                ContextCompat.getSystemService(this,
                    NotificationManager::class.java)
            )

            service.createNotificationChannel(channel)

            channelId
        } else { "" }

    private fun getNotificationBuilder(pendingIntent: PendingIntent, channelId:
    String) =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second worker process is done")
            .setContentText("Check it out!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("Second worker process is done, check it out!")
            .setOngoing(true)


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // do short countdown to avoid toast/notification collisions
        serviceHandler.post {
            val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            for (i in 5 downTo 0) {      // shorter countdown than the first service
                Thread.sleep(500L)
                mgr.notify(
                    NOTIF_ID,
                    notificationBuilder.setContentText("$i seconds…").setSilent(true).build()
                )
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    companion object {
        const val NOTIF_ID = 0xCA8
        const val EXTRA_ID = "SecondId"
    }
}