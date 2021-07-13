package com.example.firebaseabtest

import android.app.*
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.net.URISyntaxException
import java.util.*




class MyService : Service() {

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")

        currentState = EnumSet.noneOf(ServiceStateType::class.java)

        MainScope().launch {
            withContext(Dispatchers.IO) {
                repeat(1000) { i ->
                    Log.d(TAG, "$i")
                    delay(1000L)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.d(TAG, "onStartCommand intent: $intent")

        val wantedState: ServiceStateType? = intent?.extras?.getSerializable(WANTED_STATE_ARG) as? ServiceStateType?

        if (currentState?.contains(wantedState) != true) {
            if (wantedState == ServiceStateType.FOREGROUND) {
                startForeground()
            } else if (wantedState == ServiceStateType.BACKGROUND) {
                enterBackground()
            }
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        startForeground()

        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy")
    }


    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind")
        return null
    }


    private fun startForeground() {
        Log.d(TAG, "startForeground")

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            FLAG_MUTABLE or FLAG_UPDATE_CURRENT
        )

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(NOTIF_CHANNEL_ID,
            NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            .apply {
                lightColor = Color.RED
            }
//        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        service.createNotificationChannel(notificationChannel)

        startForeground(
            NOTIF_ID, NotificationCompat.Builder(this, NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setContentTitle("MyService Name")
                .setContentText("Service is running background")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        )

        currentState?.add(ServiceStateType.FOREGROUND)
        currentState?.remove(ServiceStateType.BACKGROUND)
    }

    private fun enterBackground() {
        Log.d(TAG, "enterBackground")

        stopForeground(STOP_FOREGROUND_REMOVE or STOP_FOREGROUND_DETACH)

        currentState?.add(ServiceStateType.BACKGROUND)
        currentState?.remove(ServiceStateType.FOREGROUND)
    }


    companion object {
        private const val TAG = "MyService"
        private const val NOTIF_ID = 54564
        private const val NOTIF_CHANNEL_ID = "MyServiceChannelID"
        private const val NOTIF_CHANNEL_NAME = "MyServiceChannelNAME"
        private const val WANTED_STATE_ARG = "wantedState"

        var currentState: EnumSet<ServiceStateType>? = null

        enum class ServiceStateType {
            BACKGROUND,
            FOREGROUND,
            BOUND
        }

        fun getIntent(wantedState: ServiceStateType): Intent =
            Intent(App.context, MyService::class.java).apply {
                putExtra(WANTED_STATE_ARG, wantedState)
            }

        fun startService(wantedState: ServiceStateType) {
            App.context.startService(getIntent(wantedState))
        }
    }
}