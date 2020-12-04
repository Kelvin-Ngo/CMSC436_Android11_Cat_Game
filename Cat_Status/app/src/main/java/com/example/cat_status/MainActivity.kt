package com.example.cat_status

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

// author: Ji Luo, Kelvin Ngo, Anna Kraft
// mainActivity view for our project should be handled by Ji Luo. In that activity field there
// should be a button that will activate the CatHouse activity. This mainActivity class will
// simulate that
class MainActivity : AppCompatActivity() {
    private lateinit var catLists : ArrayList<Cat>
    private var favCat : Cat? = null
    private var uniqueCatID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // inflate catHouseView whenever the button is clicked
        val catHouseButton = findViewById<Button>(R.id.catHouseButton)

        // catLists stores the cats the current user owns. We'll be using this data later
        // to add cats to our catHouse view
        catLists = arrayListOf<Cat>()



        // this chunk of code handles retrieving saved data when the app is closed.
        // how to save data to be retrieved in the first place is coded further down
        val sharedPreferences = getSharedPreferences(SPTITLE, MODE_PRIVATE)
        val jsonCatLists = sharedPreferences.getString(SPCATKEY, "")
        val jsonFavCat = sharedPreferences.getString(SPFAVKEY, "")
        uniqueCatID = sharedPreferences.getInt(UNIQUEID, 0)

        val gson = Gson()
        if(jsonFavCat != "") {
            favCat = gson.fromJson(jsonFavCat, Cat ::class.java)
        }
        if(jsonCatLists != "") {
            val type = object : TypeToken<List<Cat>>() {}.type
            catLists = gson.fromJson(jsonCatLists, type)
        } else {

            // adding example cats into the catLists to show that it works

            // need to add the cats into a array of cats. The only parameter to create a cat is an
            // id. The id needs to be unique and cannot be -1
            val apperanceGenerator = CatAppearanceGenerator(resources, applicationContext)

            while(uniqueCatID < 10) {
                catLists.add(Cat(uniqueCatID, apperanceGenerator.generateCats(uniqueCatID)))
                uniqueCatID++
            }

        }

        val favCatImage = findViewById<ImageView>(R.id.favoriteCatImage)
        favCatImage.x = -60F

        // inflates a view in the main activity with the current favorite cat
        if (favCat != null) {
            val favCatNameView = findViewById<TextView>(R.id.favCatName)


            favCatNameView.text = favCat?.getName()
            favCatNameView.textSize = 20F

            val fileName = "cat_${favCat?.getId()}.png"
            val directory = applicationContext.getDir("imageDir", Context.MODE_PRIVATE)
            val file = File(directory, "$fileName")

            val bmOptions = BitmapFactory.Options()

            var currBitmap = BitmapFactory.decodeFile(file.absolutePath, bmOptions)
            favCatImage.setImageBitmap(currBitmap)
            favCatImage.scaleX = 3F
            favCatImage.scaleY = 3F
        }

        // when this button is pressed, the cat house activity is created. We need the
        // list of cats and the favorite cat if there's any in order to keep the two views
        // (the main activity view and the CatHouseActivity view) consistent
        catHouseButton.setOnClickListener(
            View.OnClickListener {
                val intent = Intent(applicationContext, CatHouseActivity::class.java)
                // we put the cats as an extra in the intent when we start up. We will handle
                // retrieving the data in CatHouseActivity
                intent.putExtra(ICATKEY, catLists)
                intent.putExtra(IFAVKEY, favCat)
                startActivityForResult(intent, 1)
            }
        )
    }

    // when the app is paused, we save the list of cats and the favorite cat inside sharedPreference
    // so the data won't be destroyed when the app is closed. as sharedPreferences only handles
    // a select few data types, we have to convert our cat list and favorite cat into a string using
    // Gson and Json
    override fun onPause() {
        val sharedPreferences = getSharedPreferences(SPTITLE, MODE_PRIVATE)
        val sharePEditor = sharedPreferences.edit()

        val gson = Gson()
        val jsonCatLists = gson.toJson(catLists)
        val jsonFavCat = gson.toJson(favCat)
        sharePEditor.putString(SPFAVKEY, jsonFavCat).apply()
        sharePEditor.putString(SPCATKEY, jsonCatLists).apply()
        sharePEditor.putInt(UNIQUEID, uniqueCatID).apply()
        super.onPause()
    }

    // the intent sent to CatHouseActivity was sent using startActivityForResult(). The results
    // we need are the cat lists and the favorite cat. This is so that any changes made in the
    // CatHouseAtivity view is reflected in the main activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val extras = data?.extras
                catLists = extras?.get(ICATKEY) as ArrayList<Cat>
                if(extras.get(IFAVKEY) != null) {

                    favCat = data?.extras?.get(IFAVKEY) as Cat
                    if (favCat != null) {
                        val favCatImage = findViewById<ImageView>(R.id.favoriteCatImage)
                        val favCatNameView = findViewById<TextView>(R.id.favCatName)

                        favCatNameView.text = favCat?.getName()
                        favCatNameView.textSize = 20F

                        val fileName = "cat_${favCat?.getId()}.png"
                        val directory = applicationContext.getDir("imageDir", Context.MODE_PRIVATE)
                        val file = File(directory, "$fileName")
                        val bmOptions = BitmapFactory.Options()

                        var currBitmap = BitmapFactory.decodeFile(file.absolutePath, bmOptions)
                        favCatImage.setImageBitmap(currBitmap)

                        favCatImage.scaleX = 3F
                        favCatImage.scaleY = 3F

                    }
                } else {
                    favCat = null

                    val favCatNameView = findViewById<TextView>(R.id.favCatName)
                    favCatNameView.text = ""
                    val favCatImage = findViewById<ImageView>(R.id.favoriteCatImage)
                    favCatImage.setImageDrawable(null)
                }

            }
        }
    }

    companion object {
        const val SPTITLE = "catTitle"
        const val SPCATKEY = "catListsSP"
        const val SPFAVKEY = "favoriteSP"
        const val ICATKEY = "catListsIntent"
        const val IFAVKEY = "favoriteIntent"
        const val UNIQUEID = "uniqueID"
    }
}