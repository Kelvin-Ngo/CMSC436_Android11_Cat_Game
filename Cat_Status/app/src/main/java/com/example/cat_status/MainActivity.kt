package com.example.cat_status

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ListView

// mainActivity view for our project should be handled by Ji Luo. In that activity field there
// should be a button that will activate the CatHouse activity. This mainActivity class will
// simulate that
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // inflate catHouseView whenever the button is clicked
        val catHouseButton = findViewById<Button>(R.id.catHouseButton)

        // catLists stores the cats the current user owns. We'll be using this data later
        // to add cats to our catHouse view
        val catLists = arrayListOf<Cat>()

        // adding example cats into the catLists to show that it works, the parameters for
        // a cat may change in the future, so please let me know so I can adjust my code accordingly
        catLists.add(Cat("Tommy", 200, 200, 200))
        catLists.add(Cat("John", 200, 200, 200))
        catLists.add(Cat("Dick", 200, 200, 200))
        catLists.add(Cat("Richard", 200, 200, 200))
        catLists.add(Cat("Bernard", 200, 200, 200))
        catLists.add(Cat("Alfonso", 200, 200, 200))
        catLists.add(Cat("Monsieur Chat", 200, 200, 200))
        catLists.add(Cat("Timmy", 200, 200, 200))
        catLists.add(Cat("Becky", 200, 200, 200))
        catLists.add(Cat("Denice", 200, 200, 200))
        catLists.add(Cat("Tommy", 200, 200, 200))
        catLists.add(Cat("John", 200, 200, 200))
        catLists.add(Cat("Dick", 200, 200, 200))
        catLists.add(Cat("Richard", 200, 200, 200))
        catLists.add(Cat("Bernard", 200, 200, 200))
        catLists.add(Cat("Alfonso", 200, 200, 200))
        catLists.add(Cat("Monsieur Chat", 200, 200, 200))
        catLists.add(Cat("Timmy", 200, 200, 200))
        catLists.add(Cat("Becky", 200, 200, 200))
        catLists.add(Cat("Denice", 200, 200, 200))

        catHouseButton.setOnClickListener(
            View.OnClickListener {
                val intent = Intent(applicationContext, CatHouseActivity :: class.java)
                // we put the cats as an extra in the intent when we start up. We will handle
                // retrieving the data in CatHouseActivity
                intent.putExtra("argument", catLists)

                startActivity(intent)
            }
        )
    }
}