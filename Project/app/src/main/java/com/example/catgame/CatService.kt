package com.example.catgame

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import java.util.*

class CatService(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val r = Random()
    private val currContext = context   //can't access context inside functions unless we do this
    private var notifID = 0

    //conditional on whether food and water > 0%
    override fun doWork(): Result {
        Log.i(TAG, "New task queued")
        try {
            // TODO - add conditional modifier for when toy is active
            val waitTime = (LOWERBOUND..UPPERBOUND).random()
            Log.i(TAG, "Wait time = $waitTime")
            Thread.sleep(waitTime)     //Should sleep for anywhere between 3 to 7 minutes
            rollCat()
        } catch (e : InterruptedException){
            Log.i(TAG, "Thread sleep failed")
        }
        return Result.success()
    }

    private fun rollCat(){
        val catTask = OneTimeWorkRequest.Builder(CatService::class.java).build()
        WorkManager.getInstance(currContext).enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, catTask)
        if(r.nextFloat() < CATCHANCE){
            //TODO - call cat appearance generator and save the new cat to our collection
            Log.i(TAG, "New cat generated!")
            //TODO - send out a notification if the app isn't in focus
            var builder = NotificationCompat.Builder(currContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.btn_star)
                .setAutoCancel(true)
                .setContentTitle(currContext.getString(R.string.app_name))
                .setContentText(currContext.getString(R.string.new_cat_message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(currContext)){
                notify(notifID, builder.build())
            }
            notifID += 1
            //TODO - open a window displaying the new cat when app is next open
            //I think this might need to go in the main activity

        }
        else {
            Log.i(TAG, "No new cat, requeuing task")
            //re-queue a new copy of this task
        }
    }

    companion object {
        private const val TAG = "CatService"

        private const val CATCHANCE = 0.5f    //50% chance of generating a cat every call

        private const val SECOND = 1000L
        private const val MINUTE = SECOND * 60L
        private const val LOWERBOUND = 3L * SECOND
        private const val UPPERBOUND = 7L * SECOND
        private const val TOYMOD = MINUTE
        private const val CHANNEL_ID = "New_Cat_Notif"
    }

}