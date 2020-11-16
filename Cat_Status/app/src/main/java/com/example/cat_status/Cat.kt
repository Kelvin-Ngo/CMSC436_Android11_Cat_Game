package com.example.cat_status

import java.io.Serializable

// Temporary Cat class used to test my listView. I need the hunger, thirst, and
// boredom parameters so I could set each cat's status appropriately

// Notice that this class implements Serializable in order to be able to pass as an extra
// in an intent. Note that any classes that Cat uses in its code will also need to implement
// Serializable in order for this to work
class Cat(name: String, maxHunger: Int, maxThirst: Int, maxBoredom: Int): Serializable {
    private var maxHunger = maxHunger
    private var maxThirst = maxThirst
    private var maxBoredom = maxBoredom
    private var name = name

    private var currHunger = maxHunger
    private var currThirst = maxThirst
    private var currBoredom = maxBoredom

    fun getHungerPercent(): Int {
        return currHunger / maxHunger
    }

    fun getThirstPercent(): Int {
        return currThirst / maxThirst
    }

    fun getBoredomPercent(): Int {
        return currBoredom / maxBoredom
    }

    fun getHungerMax(): Int {
        return maxHunger
    }

    fun getThirstMax(): Int {
        return maxThirst
    }

    fun getBoredomMax(): Int {
        return maxBoredom
    }
    fun getName(): String {
        return name
    }

}