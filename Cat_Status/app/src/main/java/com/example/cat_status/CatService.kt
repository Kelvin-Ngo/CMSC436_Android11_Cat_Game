package com.example.cat_status

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class CatService(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val r = Random()
    private val currContext = context   //can't access context inside functions unless we do this
    private var notifID = 0
    private var mPrefs = currContext.getSharedPreferences(SPCount, Context.MODE_PRIVATE)
    private var catGenerator = CatAppearanceGenerator(currContext.resources, currContext)
    private var catID = 1
    private var newCat : Cat? = null
    private var catLists : ArrayList<Cat> = arrayListOf()

    //conditional on whether food and water > 0%
    override fun doWork(): Result {
        //if we don't have a catID in shared preferences put the starting value there
        if(mPrefs.getInt(MainActivity.UNIQUEID, -1) == -1) {
            val sharePEditor = mPrefs.edit()
            sharePEditor.putInt(MainActivity.UNIQUEID, catID).commit()
        }
        //check that we have both food and water
        if(mPrefs.getLong(SPFOOD, 0) > 1000 && mPrefs.getLong(SPWATER, 0) > 1000) {
            Log.i(TAG, "New task queued")
            try {
                val waitTime = if(mPrefs.getBoolean(SPPLAY, false)){
                    Log.i(TAG, "Toy modifier applied")
                    (LOWERBOUND - TOYMOD..UPPERBOUND - TOYMOD).random()
                } else {
                    Log.i(TAG, "No toy modifier")
                    (LOWERBOUND..UPPERBOUND).random()
                }

                Log.i(TAG, "Wait time = $waitTime")
                Thread.sleep(waitTime)     //Should sleep for anywhere between 3 to 7 minutes (2 - 6 if toy is active)
                rollCat()


            } catch (e: InterruptedException) {
                Log.i(TAG, "Thread sleep failed")
            }
        } else {
            WorkManager.getInstance(currContext).cancelAllWorkByTag(TAG)
        }

        return Result.success()
    }

    private fun rollCat(){
        if(mPrefs.getLong(SPFOOD, 0) > 1000 && mPrefs.getLong(SPWATER, 0) > 1000) {
            //re-queue a new copy of this task
            val catTask = OneTimeWorkRequest.Builder(CatService::class.java).build()
            WorkManager.getInstance(currContext)
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, catTask)
            //check if we generate a cat
            if (r.nextFloat() < CATCHANCE) {
                //call cat appearance generator and save the new cat to our collection
                Log.i(TAG, "New cat generated!")
                catID = mPrefs.getInt(MainActivity.UNIQUEID, 0)     //get last-used cat ID
                newCat = Cat(catID, catGenerator.generateCats(catID))       // generate the new cat's appearance
                var jsonCatLists = mPrefs.getString(MainActivity.SPCATKEY, "")
                catID += 1
                val gson = Gson()
                if (jsonCatLists != "") {
                    //turn our stored list of cats into an array
                    val type = object : TypeToken<List<Cat>>() {}.type
                    catLists = gson.fromJson(jsonCatLists, type)
                }

                //add the new cat to the array
                catLists.add(newCat!!)

                val sharePEditor = mPrefs.edit()
                //turn the list of cats back into a json and save it back to shared preferences
                jsonCatLists = gson.toJson(catLists)
                sharePEditor.putString(MainActivity.SPCATKEY, jsonCatLists).commit()
                sharePEditor.putInt(MainActivity.UNIQUEID, catID).commit()
                Log.i(TAG, "New cat added to Shared Preferences")

                //send out a notification that we got a new cat
                val builder = NotificationCompat.Builder(currContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_pets_24)
                    .setAutoCancel(true)
                    .setContentTitle(currContext.getString(R.string.app_name))
                    .setContentText(currContext.getString(R.string.new_cat_message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                with(NotificationManagerCompat.from(currContext)) {
                    notify(notifID, builder.build())
                }
                notifID += 1
            } else {
                Log.i(TAG, "No new cat, requeuing task")
            }
        } else {
            //Send notification that we're out of food/water
            val builder = NotificationCompat.Builder(currContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_local_dining_24)
                .setAutoCancel(true)
                .setContentTitle(currContext.getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            when {
                mPrefs.getLong(SPFOOD, 0) < 1000 -> {
                    builder.setContentText("Your cats are out of food!")
                }
                mPrefs.getLong(SPWATER, 0) < 1000 -> {
                    builder.setContentText("Your cats are out of water!")
                }
                else -> {
                    builder.setContentText("Your cats are out of food and water!")
                }

            }
            with(NotificationManagerCompat.from(currContext)) {
                notify(notifID, builder.build())
            }
            notifID += 1
            Log.i(TAG, "Out of food and water, can't generate cats")
        }
    }

    companion object {
        private const val TAG = "CatService"

        private const val CATCHANCE = 1f    //50% chance of generating a cat every call

        private const val SECOND = 1000L
        private const val MINUTE = SECOND * 60L
        private const val LOWERBOUND = 3L * SECOND
        private const val UPPERBOUND = 7L * SECOND
        private const val TOYMOD = SECOND
        private const val CHANNEL_ID = "New_Cat_Notif"

        const val SPCount = "countPrefs"
        const val SPFOOD = "foodSP"
        const val SPWATER = "waterSP"
        const val SPPLAY = "play"
    }

}