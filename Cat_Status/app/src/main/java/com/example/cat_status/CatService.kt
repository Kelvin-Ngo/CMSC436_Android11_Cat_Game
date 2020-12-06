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
    private var catLists : ArrayList<Cat> = arrayListOf<Cat>()

    //conditional on whether food and water > 0%
    override fun doWork(): Result {
        if(mPrefs.getInt(MainActivity.UNIQUEID, -1) == -1) {
            val sharePEditor = mPrefs.edit()
            sharePEditor.putInt(MainActivity.UNIQUEID, catID).commit()
        }
        if(mPrefs.getLong(SPFOOD, 0) > 1000 && mPrefs.getLong(SPWATER, 0) > 1000) {
            Log.i(TAG, "New task queued")
            try {
                var waitTime = 0L
                if(mPrefs.getBoolean(SPPLAY, false)){
                    Log.i(TAG, "Toy modifier applied")
                    waitTime = (LOWERBOUND - TOYMOD..UPPERBOUND - TOYMOD).random()
                } else {
                    Log.i(TAG, "No toy modifier")
                    waitTime = (LOWERBOUND..UPPERBOUND).random()
                }

                Log.i(TAG, "Wait time = $waitTime")
                Thread.sleep(waitTime)     //Should sleep for anywhere between 3 to 7 minutes
                rollCat()


            } catch (e: InterruptedException) {
                Log.i(TAG, "Thread sleep failed")
            }
        } else {
            WorkManager.getInstance(currContext).cancelAllWorkByTag(TAG)
        }

        return Result.success(workDataOf(KEY_NEWCAT to newCat))
    }

    private fun rollCat(){
        if(mPrefs.getLong(SPFOOD, 0) > 1000 && mPrefs.getLong(SPWATER, 0) > 1000) {
            val catTask = OneTimeWorkRequest.Builder(CatService::class.java).build()
            WorkManager.getInstance(currContext)
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, catTask)
            if (r.nextFloat() < CATCHANCE) {
                //TODO - call cat appearance generator and save the new cat to our collection
                Log.i(TAG, "New cat generated!")
                catID = mPrefs.getInt(MainActivity.UNIQUEID, 0)
                newCat = Cat(catID, catGenerator.generateCats(catID))
                var jsonCatLists = mPrefs.getString(MainActivity.SPCATKEY, "")
                catID += 1
                val gson = Gson()
                if (jsonCatLists != "") {
                    val type = object : TypeToken<List<Cat>>() {}.type
                    catLists = gson.fromJson(jsonCatLists, type)
                }

                catLists.add(newCat!!)

                val sharePEditor = mPrefs.edit()
                jsonCatLists = gson.toJson(catLists)
                sharePEditor.putString(MainActivity.SPCATKEY, jsonCatLists).commit()
                sharePEditor.putInt(MainActivity.UNIQUEID, catID).commit()
                Log.i(TAG, "New cat added to Shared Preferences")

                //TODO - send out a notification if the app isn't in focus
                var builder = NotificationCompat.Builder(currContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.btn_star)
                    .setAutoCancel(true)
                    .setContentTitle(currContext.getString(R.string.app_name))
                    .setContentText(currContext.getString(R.string.new_cat_message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                with(NotificationManagerCompat.from(currContext)) {
                    notify(notifID, builder.build())
                }
                notifID += 1
                //TODO - open a window displaying the new cat when app is next open
                //I think this might need to go in the main activity
            } else {
                Log.i(TAG, "No new cat, requeuing task")
                //re-queue a new copy of this task
//            newCat = null
            }
//        return newCat
        } else {
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

        private const val KEY_NEWCAT = "newCat"

        const val SPCount = "countPrefs"
        const val SPFOOD = "foodSP"
        const val SPWATER = "waterSP"
        const val SPTOY = "toySP"
        const val SPPLAY = "play"

        const val SPTITLE = "catTitle"
    }

}