package com.example.cat_status

import android.graphics.Bitmap
import android.view.View
import android.widget.FrameLayout
import java.io.Serializable
// author: Kelvin Ngo, Isaiah Bentz

// Temporary Cat class used to test my listView. I need the hunger, thirst, and
// boredom parameters so I could set each cat's status appropriately

// Notice that this class implements Serializable in order to be able to pass as an extra
// in an intent. Note that any classes that Cat uses in its code will also need to implement
// Serializable in order for this to work

class Cat(id: Int, imageLoc : String?): Serializable {
    private var id = id
    private var name = "Cat#$id"
    private var imageLoc = imageLoc
    private var markedDelete = false

    fun getName(): String {
        return name
    }

    fun setName(newName: String) {
        name = newName
    }

    fun getId() : Int {
        return id
    }

    fun getImageLoc() : String?{
        return imageLoc
    }

    fun markedForDelete(bol : Boolean) {
        markedDelete = bol
    }

    fun isMarkedDelete() : Boolean {
        return markedDelete
    }

    override fun equals(other: Any?): Boolean {
        if (other is Cat) {
            return name == other.getName() &&  id == other.id
        }
        return false
    }

}