package com.example.catgame

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.*

class CatService(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val r = Random()
    private val currContext = context   //can't access context inside functions unless we do this

    override fun doWork(): Result {
        try {
            // TODO - add conditional modifier for when toy is active
            Thread.sleep(LOWERBOUND + r.nextLong() * (UPPERBOUND - LOWERBOUND + MINUTE))     //Should sleep for anywhere between 3 to 5 minutes
            rollCat()
        } catch (e : InterruptedException){
            Log.i(TAG, "Thread sleep failed")
        }
        return Result.success()
    }

    private fun rollCat(){
        if(r.nextFloat() < CATCHANCE){
            //call cat appearance generator and save the new cat to our collection
        }
        else {
            //re-queue a new copy of this task
            val catTask = OneTimeWorkRequest.Builder(CatService::class.java).build()
            WorkManager.getInstance(currContext).enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, catTask)
        }
    }

    companion object {
        private const val TAG = "CatService"

        private const val CATCHANCE = 0.5f    //50% chance of generating a cat every call

        private const val SECOND = 1000
        private const val MINUTE = SECOND * 60
        private const val LOWERBOUND = 3 * MINUTE
        private const val UPPERBOUND = 7 * MINUTE
        private const val TOYMOD = MINUTE
    }

}