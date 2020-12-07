package com.example.cat_status

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothClass
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.work.Operation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


// referenced Lab 4:UI Lab for implementation

// author: Kelvin Ngo
// CatHouseActivity handles displaying cats on a gridView. It utilizes a custom catAdapter to handle
// displaying cats

// Labs used: Lab4: UI Lab
class CatHouseActivity : Activity() {
    private var cats = arrayListOf<Cat>()
    private var favCat: Cat? = null
    private lateinit var mAdapter : catAdapter
    private var isMute = false
    private val listener = SharedPreferencesListener()
    private var catListSize = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences(SPCount, MODE_PRIVATE)

        // retrieve the cats from the intent that was sent
        mAdapter = catAdapter(applicationContext)
        setContentView(R.layout.cathouse_view)

        //customizing layout
        var pictureHolder = findViewById<GridView>(R.id.gridView)
        pictureHolder.adapter = mAdapter
        pictureHolder.numColumns = 2
        pictureHolder.verticalSpacing = 20
        pictureHolder.horizontalSpacing = 20

        //get list as it is when this activity is opened
        val jsonCatLists = sharedPreferences.getString(MainActivity.SPCATKEY, "")
        val jsonFavCat = sharedPreferences.getString(MainActivity.SPFAVKEY, "")
        isMute = sharedPreferences.getBoolean(SPMUTEKEY, false)

        if(isMute) {
            mAdapter.setIsMute(isMute)
            mAdapter.notifyDataSetChanged()
        }

        val gson = Gson()
        if (jsonFavCat != "") {
            favCat = gson.fromJson(jsonFavCat, Cat::class.java)
            if (favCat != null) {
                Log.i("HELLO", "HELLO2")
            }
        }

        if (jsonCatLists != "") {
            Log.i(TAG, "Successfully found shared cats")
            val type = object : TypeToken<List<Cat>>() {}.type
            cats = gson.fromJson(jsonCatLists, type)

        } else {
            Log.i(TAG, "Cat house did not receive any cats from shared prefs")
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        var noCatsTitleView = findViewById<TextView>(R.id.noCatsTitle)
        var noCatsImage = findViewById<ImageView>(R.id.noCatsImage)
        // store the cats into a local variable. If the user has no cats, we instead inflate
        // an icon and text no cats
        if(cats.isEmpty()) {
            noCatsTitleView.text = "No Cats"

            noCatsTitleView.textSize = 40F
            noCatsImage.setBackgroundResource(R.drawable.ic_cat)
        } else {
            noCatsTitleView.text = null
            noCatsImage.background = null
        }
        // add each cat found in the arraylist into the adapter
        cats.forEach {
            mAdapter.add(it)
        }

        setUpMuteButton()
        setUpTrashButton()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
        )

    }

    // Grants or denies permission to allow share functionality. If request is denied then
    // share won't work
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if(requestCode == 1) {
            if(!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                if(ActivityCompat.checkSelfPermission(
                        applicationContext,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(applicationContext, "READ PERMISSION DENIED", Toast.LENGTH_LONG).show()
                }

        }
    }

    //handles creating the icon and creating the functionality of muting
    private fun setUpMuteButton() {
        val sharedPreferences = getSharedPreferences(SPCount, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val muteButton = findViewById<Button>(R.id.muteButton)
        if(!isMute) {
            muteButton.setBackgroundResource(R.drawable.speaker)
        } else {
            muteButton.setBackgroundResource(R.drawable.ic_mute)
        }

        muteButton.setOnClickListener {
            if(!isMute) {
                isMute = true
                mAdapter.setIsMute(true)
                muteButton.setBackgroundResource(R.drawable.ic_mute)
                editor.putBoolean(SPMUTEKEY, isMute).apply()
            } else {
                isMute = false
                mAdapter.setIsMute(false)
                muteButton.setBackgroundResource(R.drawable.speaker)
                editor.putBoolean(SPMUTEKEY, isMute).apply()
            }
            mAdapter.notifyDataSetChanged()
        }
    }

    //handles setting up the trash button image and functionality
    private fun setUpTrashButton() {
        val trashButton = findViewById<Button>(R.id.trashButton)
        trashButton.setOnClickListener {
            mAdapter.setMultiDelete(true)

            val confirmButton = findViewById<Button>(R.id.confirmButton)
            val cancelButton = findViewById<Button>(R.id.cancelButton)

            confirmButton.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            mAdapter.notifyDataSetChanged()

            confirmButton.setOnClickListener {
                mAdapter.multiDelete(mAdapter.getCatsToBeDeleted(), confirmButton.rootView.context)
                mAdapter.notifyDataSetChanged()
                confirmButton.visibility = View.GONE
                cancelButton.visibility = View.GONE
                mAdapter.setMultiDelete(false)
                mAdapter.notifyDataSetChanged()
            }

            cancelButton.setOnClickListener {
                mAdapter.clearDeleteList()
                confirmButton.visibility = View.GONE
                cancelButton.visibility = View.GONE
                mAdapter.setMultiDelete(false)
                val catList = mAdapter.getList()
                for(cat in catList) {
                    cat.markedForDelete(false)
                }
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    //same thing as MainActivity's onPause method, look there
    override fun onPause() {
        val catList = mAdapter.getList()
        for(cat in catList) {
            cat.markedForDelete(false)
        }
        favCat = mAdapter.getFav()
        setResult(RESULT_OK)
        super.onPause()
    }

    override fun onStop() {
        setResult(RESULT_OK)
        super.onStop()
    }

    // when you press back in this view, you should return to main activity so, we need to send
    // a result intent that should be handled by onActivityResult() in main activity. Once again,
    // this is to maintain consistency between main activity and CatHouseActivity
    override fun onBackPressed() {
        val catList = mAdapter.getList()
        for(cat in catList) {
            cat.markedForDelete(false)
        }
        mAdapter.clearDeleteList()
        setResult(RESULT_OK)
        super.onBackPressed()

    }

    inner class SharedPreferencesListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            if(key == MainActivity.SPCATKEY){
                //Get updated cat list
                val jsonCatLists = sharedPreferences!!.getString(MainActivity.SPCATKEY, "")
                val jsonFavCat = sharedPreferences.getString(MainActivity.SPFAVKEY, "")

                val gson = Gson()
                if (jsonFavCat != "") {
                    favCat = gson.fromJson(jsonFavCat, Cat::class.java)
                }

                var noCatsTitleView = findViewById<TextView>(R.id.noCatsTitle)
                var noCatsImage = findViewById<ImageView>(R.id.noCatsImage)

                if (jsonCatLists != "") {
                    Log.i(TAG, "Cat List is not empty - loading cats")
                    val type = object : TypeToken<List<Cat>>() {}.type
                    cats = gson.fromJson(jsonCatLists, type)
                    if(cats.size > catListSize && cats.size > 0)
                        mAdapter.add(cats[cats.size-1])
                    catListSize = cats.size
                } else {
                    Log.i(TAG, "Cat List is empty :(")
                }

                if(cats.isEmpty()) {
                    noCatsTitleView.text = "No Cats"

                    noCatsTitleView.textSize = 40F
                    noCatsImage.setBackgroundResource(R.drawable.ic_cat)
                } else {
                    noCatsTitleView.text = null
                    noCatsImage.background = null
                }
                mAdapter.notifyDataSetChanged()
            }
        }

    }



    companion object {
        const val TAG = "CatHouseActivity"
        const val SPCount = "countPrefs"
        const val SPMUTEKEY = "isMute"
        const val MAXCHARNAME = 13
    }
}