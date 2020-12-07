package com.example.cat_status

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.min

// author: Ji Luo, Kelvin Ngo, Anna Kraft
// mainActivity view for our project should be handled by Ji Luo. In that activity field there
// should be a button that will activate the CatHouse activity. This mainActivity class will
// simulate that
class MainActivity : AppCompatActivity() {
    private var defaultProgressTime:Long = 60000
    private var defaultRate:Long = 1000
    private var catLists : ArrayList<Cat> = arrayListOf()
    private var favCat : Cat? = null
    private var uniqueCatID = 0
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var foodbar: ProgressBar
    private lateinit var waterbar: ProgressBar
    private lateinit var toyStatus: TextView
    private lateinit var mCountDownTimerFood: CountDownTimer
    private lateinit var mCountDownTimerWater: CountDownTimer
    private lateinit var mCountDownTimerToy: CountDownTimer
    private var mTimeLeftInMillisFood:Long = defaultProgressTime
    private var mTimeLeftInMillisWater:Long = defaultProgressTime
    private var mTimeLeftInMillisToy:Long = 0
    private var isPlaying = false

    private val listener = SharedPreferencesListener()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannels()


        //get information from the last update and restore them
        val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
        val current = System.currentTimeMillis()
        val leaving = countPrefs.getLong("LeavingTime", current)

        mTimeLeftInMillisFood = countPrefs.getLong(SPFOOD, defaultProgressTime) - (current - leaving)
        mTimeLeftInMillisWater = countPrefs.getLong(SPWATER, defaultProgressTime) - (current - leaving)
        mTimeLeftInMillisToy = countPrefs.getLong(SPTOY, 0) - (current - leaving)
        isPlaying = countPrefs.getBoolean(SPPLAY, false)
        if(mTimeLeftInMillisFood < 0){
            mTimeLeftInMillisFood = 0
        }
        if(mTimeLeftInMillisWater < 0){
            mTimeLeftInMillisWater = 0
        }
        if(mTimeLeftInMillisToy < 0){
            mTimeLeftInMillisToy = 0
            isPlaying = false
        }
        if(isPlaying){
            playing()
        }


        // inflate catHouseView whenever the button is clicked
        val catHouseButton = findViewById<ImageButton>(R.id.catHouseButton)
        //Set up foodbar and food image
        foodbar = findViewById(R.id.food)
        foodbar.setOnClickListener{
            Log.i(TAG, "Refilled food")
            fillFood()

            if(hasFoodAndWater())  {
                val catTask = OneTimeWorkRequest.Builder(CatService::class.java).build()
                val workManager = WorkManager.getInstance(applicationContext)
                workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, catTask)
            }
        }

        val foodImage = findViewById<ImageView>(R.id.foodPNG)
        foodImage.setImageResource(R.drawable.food)
        //set up water bar and image
        waterbar = findViewById(R.id.water)
        waterbar.setOnClickListener{
            Log.i(TAG, "Refilled water")
            fillWater()
            if(hasFoodAndWater()) {
                val catTask = OneTimeWorkRequest.Builder(CatService::class.java).build()
                val workManager = WorkManager.getInstance(applicationContext)
                workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, catTask)
            }
        }
        val waterImage = findViewById<ImageView>(R.id.waterPNG)
        waterImage.setImageResource(R.drawable.water)

        toyStatus = findViewById(R.id.toyStatus)
        toyStatus.text = getString(R.string.toyStatusEmpty)

        eating()
        drinking()
        playing()

        //set up toy and image
        val toyButton = findViewById<ImageView>(R.id.toy)
        toyButton.setImageResource(R.drawable.toy)
        toyButton.setOnClickListener {
            isPlaying = true
            play()
        }

        //start the cat generation service
        if(hasFoodAndWater()) {
            val catTask = OneTimeWorkRequest.Builder(CatService::class.java).build()
            val workManager = WorkManager.getInstance(applicationContext)
            workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, catTask)
        }

        createNotificationChannel()

        // this chunk of code handles retrieving saved data when the app is closed.
        // how to save data to be retrieved in the first place is coded further down
        countPrefs.registerOnSharedPreferenceChangeListener(listener)

        Log.i(TAG, "Found new cat in Shared Preferences")
        val jsonCatLists = countPrefs!!.getString(SPCATKEY, "")
        val jsonFavCat = countPrefs.getString(SPFAVKEY, "")
        uniqueCatID = countPrefs.getInt(UNIQUEID, 0)

        val gson = Gson()
        if (jsonFavCat != "") {
            favCat = gson.fromJson(jsonFavCat, Cat::class.java)
        }

        if (jsonCatLists != "") {
            Log.i(TAG, "Cat List is not empty - loading cats")
            val type = object : TypeToken<List<Cat>>() {}.type
            catLists = gson.fromJson(jsonCatLists, type)

        } else {
            Log.i(TAG, "Cat List is empty :(")
        }

        createFavCat()

        // when this button is pressed, the cat house activity is created. We need the
        // list of cats and the favorite cat if there's any in order to keep the two views
        // (the main activity view and the CatHouseActivity view) consistent
        catHouseButton.setOnClickListener {
            val intent = Intent(applicationContext, CatHouseActivity::class.java)
            // we put the cats as an extra in the intent when we start up. We will handle
            // retrieving the data in CatHouseActivity
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 1 && resultCode == RESULT_OK) {
            val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
            val jsonFavCat = countPrefs.getString(SPFAVKEY, "")

            val gson = Gson()

            val favCatImage = findViewById<ImageView>(R.id.favoriteCatImage)
            favCat = gson.fromJson(jsonFavCat, Cat::class.java)
            if (favCat != null) {
                val favCatNameView = findViewById<TextView>(R.id.favCatName)

                favCatNameView.text = favCat?.getName()
                favCatNameView.textSize = 17F

                val fileName = "cat_${favCat?.getId()}.png"
                val file = File(applicationContext.filesDir, fileName)
                val bmOptions = BitmapFactory.Options()

                val currBitmap = BitmapFactory.decodeFile(file.absolutePath, bmOptions)

                favCatImage.setImageBitmap(currBitmap)

                favCatImage.scaleX = 3F
                favCatImage.scaleY = 3F

                if(mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(applicationContext, R.raw.cat_meow)
                }

                favCatImage.setOnClickListener {
                    mediaPlayer?.start()
                }

            } else {
                val favCatNameView = findViewById<TextView>(R.id.favCatName)
                favCatNameView.text = ""
                val favCatImage = findViewById<ImageView>(R.id.favoriteCatImage)
                favCatImage.setImageDrawable(null)
                favCatImage.setOnClickListener(null)
            }
        }
    }


    // inflates a view in the main activity with the current favorite cat
    private fun createFavCat(){
        val favCatImage = findViewById<ImageView>(R.id.favoriteCatImage)
        favCatImage.x = -60F

        if (favCat != null) {
            val favCatNameView = findViewById<TextView>(R.id.favCatName)


            favCatNameView.text = favCat?.getName()
            favCatNameView.textSize = 17F

            val fileName = "cat_${favCat?.getId()}.png"
            val file = File(applicationContext.filesDir, fileName)

            val bmOptions = BitmapFactory.Options()

            val currBitmap = BitmapFactory.decodeFile(file.absolutePath, bmOptions)
            Log.i("HELLO", "HELLO")
            favCatImage.setImageBitmap(currBitmap)
            favCatImage.scaleX = 3F
            favCatImage.scaleY = 3F

            if(mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(applicationContext, R.raw.cat_meow)
            }

            favCatImage.setOnClickListener {
                mediaPlayer?.start()
            }
        }

    }

    override fun onStop() {
        //When the app is stopping, save all the current data to Shared Preference
        val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
        val editor = countPrefs.edit()
        editor.putLong("LeavingTime", System.currentTimeMillis())
        editor.putLong(SPFOOD, mTimeLeftInMillisFood)
        editor.putLong(SPWATER, mTimeLeftInMillisWater)
        editor.putLong(SPTOY, mTimeLeftInMillisToy)
        editor.putBoolean(SPPLAY, isPlaying)
        editor.apply()


        //create notification and alarm
        val intent = Intent(this, NotificationBroad::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        val mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeCurrent = System.currentTimeMillis()
        val timeToNotify = min(mTimeLeftInMillisFood,mTimeLeftInMillisWater)

        mAlarmManager.set(AlarmManager.RTC_WAKEUP, timeCurrent + timeToNotify, pendingIntent)
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_LONG).show()
        super.onStop()
    }

    // when the app is paused, we save the list of cats and the favorite cat inside sharedPreference
    // so the data won't be destroyed when the app is closed. as sharedPreferences only handles
    // a select few data types, we have to convert our cat list and favorite cat into a string using
    // Gson and Json
    override fun onPause() {
        //Same as above, save data to shared preference

        val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
        val editor = countPrefs.edit()
        editor.putLong("LeavingTime", System.currentTimeMillis())
        editor.putLong(SPFOOD, mTimeLeftInMillisFood)
        editor.putLong(SPWATER, mTimeLeftInMillisWater)
        editor.putLong(SPTOY, mTimeLeftInMillisToy)
        editor.putBoolean(SPPLAY, isPlaying)
        editor.apply()


        val sharedPreferences = getSharedPreferences(SPCount, MODE_PRIVATE)
        val sharePEditor = sharedPreferences.edit()

        val gson = Gson()
        val jsonCatLists = gson.toJson(catLists)
        val jsonFavCat = gson.toJson(favCat)
        sharePEditor.putString(SPFAVKEY, jsonFavCat).apply()
        sharePEditor.putString(SPCATKEY, jsonCatLists).apply()
        sharePEditor.putInt(UNIQUEID, uniqueCatID).apply()


        super.onPause()
    }

    override fun onResume() {
        // unpack data from shared Preferences
        val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
        val current = System.currentTimeMillis()
        val leaving = countPrefs.getLong("LeavingTime", current)
        mTimeLeftInMillisFood = countPrefs.getLong(SPFOOD, defaultProgressTime) - (current - leaving)
        if (mTimeLeftInMillisFood < 0){
            mTimeLeftInMillisFood = 0
        }
        mTimeLeftInMillisWater = countPrefs.getLong(SPWATER, defaultProgressTime) - (current - leaving)
        if(mTimeLeftInMillisWater < 0){
            mTimeLeftInMillisFood = 0
        }
        mTimeLeftInMillisToy = countPrefs.getLong(SPTOY, 0) - (current - leaving)
        isPlaying = countPrefs.getBoolean(SPPLAY, false)

        if(mTimeLeftInMillisToy < 0){
            mTimeLeftInMillisToy = 0
            isPlaying = false
        }

        super.onResume()
    }

    override fun onStart() {
        //Unpack data here to
        val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
        val current = System.currentTimeMillis()
        val leaving = countPrefs.getLong("LeavingTime", current)
        mTimeLeftInMillisFood = countPrefs.getLong(SPFOOD, defaultProgressTime) - (current - leaving)
        if (mTimeLeftInMillisFood < 0){
            mTimeLeftInMillisFood = 0
        }
        mTimeLeftInMillisWater = countPrefs.getLong(SPWATER, defaultProgressTime) - (current - leaving)
        if(mTimeLeftInMillisWater < 0){
            mTimeLeftInMillisFood = 0
        }
        mTimeLeftInMillisToy = countPrefs.getLong(SPTOY, 0) - (current - leaving)
        isPlaying = countPrefs.getBoolean(SPPLAY, false)
        if(mTimeLeftInMillisToy < 0){
            mTimeLeftInMillisToy = 0
            isPlaying = false
        }
        super.onStart()
    }

    inner class SharedPreferencesListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            if(key == SPCATKEY){
                Log.i(TAG, "Found new cat in Shared Preferences")
                val jsonCatLists = sharedPreferences!!.getString(SPCATKEY, "")
                // val jsonFavCat = sharedPreferences.getString(SPFAVKEY, "")
                uniqueCatID = sharedPreferences.getInt(UNIQUEID, 0)

                val gson = Gson()
//                if (jsonFavCat != "") {
//
//                    favCat = gson.fromJson(jsonFavCat, Cat::class.java)
//                }

                if (jsonCatLists != "") {
                    Log.i(TAG, "Cat List is not empty - loading cats")
                    val type = object : TypeToken<List<Cat>>() {}.type
                    catLists = gson.fromJson(jsonCatLists, type)

                } else {
                    Log.i(TAG, "Cat List is empty :(")
                }
            }
        }

    }

    //cats start eating the food
    //create a count down timer for food bar
    private fun eating(){
        mCountDownTimerFood = object : CountDownTimer(mTimeLeftInMillisFood, defaultRate) {
            override fun onTick(millisUntilFinished: Long) {
                foodbar.progress = 100*millisUntilFinished.toInt()/defaultProgressTime.toInt()
                mTimeLeftInMillisFood = millisUntilFinished

                val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
                val editor = countPrefs.edit()
                editor.putLong(SPFOOD, mTimeLeftInMillisFood)
                editor.commit()
            }

            override fun onFinish() {
                foodbar.progress = 0
                if(!hasFoodAndWater()){
                    Log.i(TAG, "Out of food - stop cat generation")
                    WorkManager.getInstance(applicationContext).cancelAllWorkByTag(WORK_TAG)
                }
            }

        }
        mCountDownTimerFood.start()
    }

    //cats start drinking the water
    //create a count down timer for water bar
    private fun drinking(){
        mCountDownTimerWater = object : CountDownTimer(mTimeLeftInMillisWater, defaultRate) {
            override fun onTick(millisUntilFinished: Long) {

                waterbar.progress = 100*millisUntilFinished.toInt()/defaultProgressTime.toInt()
                mTimeLeftInMillisWater = millisUntilFinished

                val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
                val editor = countPrefs.edit()
                editor.putLong(SPWATER, mTimeLeftInMillisWater)
                editor.commit()
            }

            override fun onFinish() {
                waterbar.progress = 0
                if(!hasFoodAndWater()){
                    Log.i(TAG, "Out of water - stop cat generation")
                    WorkManager.getInstance(applicationContext).cancelAllWorkByTag(WORK_TAG)
                }
            }
        }
        mCountDownTimerWater.start()
    }

    //cats will play with toy
    //each click will increment playing time for 1 minute
    private fun playing(){
        mCountDownTimerToy = object : CountDownTimer(mTimeLeftInMillisToy, defaultRate) {
            override fun onTick(millisUntilFinished: Long) {

                mTimeLeftInMillisToy = millisUntilFinished

                var hour = "" + mTimeLeftInMillisToy.toInt()/1000/60/60
                var mins = "" + mTimeLeftInMillisToy.toInt()/1000/60
                var second = "" + mTimeLeftInMillisToy.toInt()/1000 % 60
                if(mTimeLeftInMillisToy.toInt()/1000/60/60 < 10){
                    hour = "0$hour"
                }
                if(mTimeLeftInMillisToy.toInt()/1000/60 < 10){
                    mins = "0$mins"
                }
                if(mTimeLeftInMillisToy.toInt()/1000 % 60 < 10){
                    second = "0$second"
                }
                val time = "$hour:$mins:$second"

                toyStatus.text = getString(R.string.toyStatusTime, time)

                val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
                val editor = countPrefs.edit()
                editor.putLong(SPTOY, mTimeLeftInMillisToy)
                editor.putBoolean(SPPLAY, isPlaying)
                editor.apply()
            }

            override fun onFinish(){
                isPlaying = false

                toyStatus.text = getString(R.string.toyStatusEmpty)

                val countPrefs = getSharedPreferences(SPCount, Context.MODE_PRIVATE)
                val editor = countPrefs.edit()
                editor.putBoolean(SPPLAY, isPlaying)
                editor.apply()
            }
        }.start()
    }

    //refill food for cats
    private fun fillFood(){
        mCountDownTimerFood.cancel()
        mTimeLeftInMillisFood = defaultProgressTime
        eating()
    }

    //refill water for cats
    private fun fillWater(){
        mCountDownTimerWater.cancel()
        mTimeLeftInMillisWater = defaultProgressTime
        drinking()
    }

    private fun play(){
        mCountDownTimerToy.cancel()
        mTimeLeftInMillisToy += 60000
        playing()
    }

    fun hasFoodAndWater(): Boolean {
        return mTimeLeftInMillisFood > 1000 && mTimeLeftInMillisWater > 1000
    }

    //create notification channel
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



    private fun createNotificationChannels(){
        val mChannel = NotificationChannel(channelID1, "CatChannel", NotificationManager.IMPORTANCE_DEFAULT)
        mChannel.description = "Water and Food situation"
        val mNotificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
        mNotificationManager.createNotificationChannel(mChannel)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val WORK_TAG = "CatService"

        const val SPCount = "countPrefs"
        const val SPCATKEY = "catListsSP"
        const val SPFAVKEY = "favoriteSP"
        const val SPFOOD = "foodSP"
        const val SPWATER = "waterSP"
        const val SPTOY = "toySP"
        const val SPPLAY = "play"
        const val UNIQUEID = "uniqueID"
        const val channelID1 = "cat_channel"
        private const val CHANNEL_ID = "New_Cat_Notif"
    }
}
