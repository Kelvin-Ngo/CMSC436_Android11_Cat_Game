package com.example.cat_status

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// author: Ji Luo, Kelvin Ngo
// mainActivity view for our project should be handled by Ji Luo. In that activity field there
// should be a button that will activate the CatHouse activity. This mainActivity class will
// simulate that
class MainActivity : AppCompatActivity() {
    private lateinit var catLists : ArrayList<Cat>
    private var favCat : Cat? = null

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

        val gson = Gson()
        Toast.makeText(applicationContext, "$jsonFavCat", Toast.LENGTH_SHORT).show()
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

            catLists.add(Cat(0))
            catLists.add(Cat(1))
            catLists.add(Cat(2))
            catLists.add(Cat(3))
            catLists.add(Cat(4))
            catLists.add(Cat(5))
            catLists.add(Cat(6))
            catLists.add(Cat(7))
            catLists.add(Cat(8))

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
                    Toast.makeText(applicationContext, "${favCat!!.getName()}", Toast.LENGTH_SHORT).show()
                } else {
                    favCat = null
                    Toast.makeText(applicationContext, "No favorite cat", Toast.LENGTH_SHORT).show()
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
    }
}