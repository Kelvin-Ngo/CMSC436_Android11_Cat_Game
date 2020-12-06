package com.example.cat_status

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationBroad: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val builder = NotificationCompat.Builder(context!!, "catNotification")
            .setSmallIcon(R.drawable.ic_cat)
            .setContentTitle("Cat Notification")
            .setContentText("Please come back! Your cats need your care!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.notify(2020, builder.build())
    }
}