package com.example.cat_status

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothClass
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import com.google.gson.Gson
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve the cats from the intent that was sent
        val extras = intent.extras
        mAdapter = catAdapter(applicationContext)
        setContentView(R.layout.cathouse_view)

        //customizing layout
        var pictureHolder = findViewById<GridView>(R.id.gridView)
        pictureHolder.adapter = mAdapter
        pictureHolder.numColumns = 2
        pictureHolder.verticalSpacing = 20
        pictureHolder.horizontalSpacing = 20


        if (extras != null) {
            cats = extras.get(ICATKEY) as ArrayList<Cat>
            if(extras.get(IFAVKEY) != null) {
                favCat = extras.get(IFAVKEY) as Cat
                mAdapter.setFav(favCat!!)
            }
        }

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


    // when you press back in this view, you should return to main activity so, we need to send
    // a result intent that should be handled by onActivityResult() in main activity. Once again,
    // this is to maintain consistency between main activity and CatHouseActivity
    override fun onBackPressed() {
        val intent = Intent(applicationContext, MainActivity :: class.java)
        val list = mAdapter.getList()
        intent.putExtra(ICATKEY, list)
        if(favCat != null && list.contains(favCat!!)) {
            intent.putExtra(IFAVKEY, favCat)
        }
        setResult(RESULT_OK, intent)
        super.onBackPressed()

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
            } else {
                isMute = false
                mAdapter.setIsMute(false)
                muteButton.setBackgroundResource(R.drawable.speaker)

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
        val sharedPreferences = getSharedPreferences(SPTITLE, MODE_PRIVATE)
        val sharePEditor = sharedPreferences.edit()

        val gson = Gson()
        val catList = mAdapter.getList()
        for(cat in catList) {
            cat.markedForDelete(false)
        }
        val jsonCatLists = gson.toJson(catList)
        favCat = mAdapter.getFav()
        val jsonFavCat = gson.toJson(favCat)
        sharePEditor.putString(SPFAVKEY, jsonFavCat).apply()
        sharePEditor.putString(SPCATKEY, jsonCatLists).apply()

        val intent = Intent(applicationContext, MainActivity::class.java)
        val list = mAdapter.getList()
        intent.putExtra(ICATKEY, list)
        if(favCat != null && list.contains(favCat)) {
            intent.putExtra(IFAVKEY, favCat)
        }
        setResult(RESULT_OK, intent)

        super.onPause()
    }

    // when you press back in this view, you should return to main activity so, we need to send
    // a result intent that should be handled by onActivityResult() in main activity. Once again,
    // this is to maintain consistency between main activity and CatHouseActivity
    override fun onBackPressed() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val catList = mAdapter.getList()
        for(cat in catList) {
            cat.markedForDelete(false)
        }
        mAdapter.clearDeleteList()
        intent.putExtra(ICATKEY, catList)
        favCat = mAdapter.getFav()

        if(favCat != null && catList.contains(favCat)) {
            intent.putExtra(IFAVKEY, favCat)
        }
        setResult(RESULT_OK, intent)
        super.onBackPressed()

    }



    companion object {
        const val SPTITLE = "catTitle"
        const val SPCATKEY = "catListsSP"
        const val SPFAVKEY = "favoriteSP"
        const val ICATKEY = "catListsIntent"
        const val IFAVKEY = "favoriteIntent"
        const val MAXCHARNAME = 13
    }
}