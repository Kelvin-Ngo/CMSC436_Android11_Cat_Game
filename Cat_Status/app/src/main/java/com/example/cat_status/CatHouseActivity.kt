package com.example.cat_status

import android.app.Activity
import android.app.AlertDialog
import android.app.ListActivity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import kotlin.math.max

// author: Kelvin Ngo
// main class of the catHouseActivity, it uses a listView to add cat statuses on the screen

// TODO: Need to add a storage feature so I can save the current cats that are in the list

// TODO: Figure out a way to pass the adapter class as an extra in an intent as to avoid creating
//       and loading a new adapter every time the listView is viewed

class CatHouseActivity : Activity() {
    private var cats = arrayListOf<Cat>()
    private lateinit var mImageView: ImageView
    private var favCat: Cat? = null
    private lateinit var mAdapter : catAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve the cats from the intent that was sent
        val extras = intent.extras
        mAdapter = catAdapter(applicationContext)
        mAdapter.setActivity(this)
        val pictureHolder = GridView(applicationContext)
        pictureHolder.adapter = mAdapter
        pictureHolder.numColumns = 2
        pictureHolder.verticalSpacing = 20
        pictureHolder.horizontalSpacing = 20
        setContentView(pictureHolder)


        // store the cats into a local variable. If the user has no cats, we instead inflate
        // a image that states user has no cats

        if (extras != null) {
            cats = extras.get(ICATKEY) as ArrayList<Cat>
            if(extras.get(IFAVKEY) != null) {
                favCat = extras.get(IFAVKEY) as Cat
                mAdapter.favId(favCat!!.getId())
            }
        }
        if(cats.isEmpty()) {
            setContentView(R.layout.no_cats)
        }
        // add each cat found in the arraylist into the adapter
        cats.forEach {
            mAdapter.add(it)
        }

    }

    // when you press back in this view, you should return to main activity so, we need to send
    // a result intent that should be handled by onActivityResult() in main activity. Once again,
    // this is to maintain consistency between main activity and CatHouseActivity
    override fun onBackPressed() {
        val intent = Intent(applicationContext, MainActivity :: class.java)
        val list = mAdapter.getList()
        intent.putExtra(ICATKEY, list)
        if(favCat != null && list.contains(favCat)) {
            intent.putExtra(IFAVKEY, favCat)
        }
        setResult(RESULT_OK, intent)
        super.onBackPressed()

    }

    // This method is to handle the shared button being pressed. We need permission to share
    // data so it first checks for permission and if it doesn't have it, it ask for permission
    // It will then call sendImage()
    fun shareButtonPressed(imageView: ImageView) {
        val permissions = ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissions == PackageManager.PERMISSION_GRANTED) {
            sendImage(imageView)
        } else {
            mImageView = imageView
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        }
    }

    // Grants or denies permission for request. Will activate sendImage is permission is granted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if(requestCode == 1) {
            if(!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                if(ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "GRANTED", Toast.LENGTH_LONG).show()
                    sendImage(mImageView)
                } else {
                    Toast.makeText(applicationContext, "DENIED", Toast.LENGTH_LONG).show()
                }
        }

    }

    //same thing as MainActivity's onPause method, look there
    override fun onPause() {
        val sharedPreferences = getSharedPreferences(SPTITLE, MODE_PRIVATE)
        val sharePEditor = sharedPreferences.edit()

        val gson = Gson()
        val jsonCatLists = gson.toJson(mAdapter.getList())
        val jsonFavCat = gson.toJson(favCat)
        sharePEditor.putString(SPFAVKEY, jsonFavCat).apply()
        sharePEditor.putString(SPCATKEY, jsonCatLists).apply()

        super.onPause()
    }

    // Handles sending the Image of the clicked cat. It first needs t retreive the image and the
    // user will choose how to send it and how to send it to.
    private fun sendImage(imageView: ImageView) {
        val imageView = findViewById<ImageView>(R.id.catIcon)
        val drawable = imageView.drawable
        val bitmap = (drawable as BitmapDrawable).bitmap

        try {
            val file =  File(applicationContext.externalCacheDir, File.separator + "cat_line.png")
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            file.setReadable(true, false)
            val intent = Intent(android.content.Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val photoURI = FileProvider.getUriForFile(applicationContext, BuildConfig.APPLICATION_ID + ".provider", file)

            intent.putExtra(Intent.EXTRA_STREAM, photoURI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "image/png"

            startActivity(Intent.createChooser(intent, "Share image via"))
        } catch (e : Exception) {
            e.printStackTrace()
        }

    }

    // function to change name of cats, takes a cat and the adapter the cat is stored in.
    // it first creates a dialog box asking for what the new name of the cat is. When user clicks
    // ok, the method first checks to see if favCat is the current cat getting its name changed.
    // If so it will change the name of favCat and current cat if not, it will just change
    // the current cat's name
    fun changeName(currCat: Cat, mAdapter: catAdapter) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Name:")

        val textInput = EditText(this)
        builder.setView(textInput)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            if(favCat != null && currCat.equals(favCat)) {
                val newName = textInput.text.toString()
                currCat.setName(" $newName")
                favCat?.setName(" $newName")
            } else {
                val newName = textInput.text.toString()
                currCat.setName(" $newName")
            }
        })

        builder.setNegativeButton("CANCEL", null)

        builder.show()
    }

    fun setFav(currCat : Cat) {

        if (currCat != favCat) {
            favCat = currCat
            mAdapter.favId(currCat.getId())
            mAdapter.notifyDataSetChanged()
        }
    }

    // opens up a dialog box confirming if user really wants to delete the cat. If so, then the
    // cat will be removed from the adapter and if favCat was the cat removed, favCat will be set
    // to null
    fun delete(currCat: Cat) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Do You Really Want to Delete this Cat?")

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            if(favCat != null && currCat.equals(favCat)) {
                favCat == null
            }
            mAdapter.remove(currCat)
            mAdapter.notifyDataSetChanged()
        })

        builder.setNegativeButton("CANCEL", null)

        builder.show()
    }

    companion object {
        const val SPTITLE = "catTitle"
        const val SPCATKEY = "catListsSP"
        const val SPFAVKEY = "favoriteSP"
        const val ICATKEY = "catListsIntent"
        const val IFAVKEY = "favoriteIntent"
    }
}