package com.example.cat_status

import android.app.Activity
import android.app.ListActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlin.math.max

// main class of the catHouseActivity, it uses a listView to add cat statuses on the screen

// TODO: Need to add a storage feature so I can save the current cats that are in the list
// TODO: Need to add notification feature when a cat is currently low in any status
// TODO  Need to add delete feature to get rid of cats
// TODO: Need to decrement each status as time goes on
// TODO: Need to customize each cat status because right now they're too samey
// TODO: Need to add fragment where clicking on a cat's icon will pop up a more detailed view of their stats
//       This fragment will display precise numbers for the hunger, thirst, and boredom bar and will
//       display a customize image of the cat (image will be created by Isaiah)
//       If I have extra time, I may add in characteristics for each cat that will affect their
//       status bars
// TODO: Search feature by cat name?
// TODO: Figure out a way to pass the adapter class as an extra in an intent as to avoid creating
//       and loading a new adapter every time the listView is viewed

class CatHouseActivity :ListActivity() {
    private var cats = arrayListOf<Cat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve the cats from the intent that was sent
        val extras = intent.extras
        val mAdapter = catAdapter(applicationContext)

        listView.adapter = mAdapter
        listView.dividerHeight = 30

        // store the cats into a local variable. If the user has no cats, we instead inflate
        // a image that states user has no cats
        if (extras != null) {
            cats = extras.get("argument") as ArrayList<Cat>
        }
        if(cats.isEmpty()) {
            listView.setBackgroundResource(R.drawable.ic_no_cats)
        }
        // add each cat found in the arraylist into the adapter
        cats.forEach {
            mAdapter.add(it)
        }

    }
}