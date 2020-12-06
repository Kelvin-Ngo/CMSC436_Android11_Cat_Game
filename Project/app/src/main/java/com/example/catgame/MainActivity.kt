package com.example.catgame

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Set the cat generator to start working
        val catTask = OneTimeWorkRequest.Builder(CatService::class.java).build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork("CatService", ExistingWorkPolicy.REPLACE, catTask)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val desc = getString(R.string.channel_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = desc
        }

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "CatService"
        private const val CHANNEL_ID = "New_Cat_Notif"
    }
}