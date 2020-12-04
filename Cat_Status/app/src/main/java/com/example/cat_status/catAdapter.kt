package com.example.cat_status

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable

// author: Kelvin Ngo
// Adaptor class for listView. Code is based off of Lab4: UILabs
class catAdapter(private val mContext: Context) : BaseAdapter(), Serializable {
    private val mItems = ArrayList<Cat>()
    private lateinit var referenceActivity: CatHouseActivity
    private var favId = -1

    fun setActivity(activity1: CatHouseActivity) {
        referenceActivity = activity1
    }
    override fun getCount(): Int {
        return mItems.size
    }

    fun getList() :ArrayList<Cat>{
        return mItems
    }
    override fun getItem(position: Int): Any {
        return mItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun add(newCat: Cat) {
        mItems.add(newCat)
    }

    fun remove(newCat: Cat) {
        mItems.remove(newCat)
    }

    fun favId(id: Int) {
        favId = id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var currItem = mItems[position]
        val viewHolder: ViewHolder

        if(convertView == null) {
            viewHolder = ViewHolder()

            viewHolder.mItemLayout = LayoutInflater.from(mContext).inflate(
                R.layout.cat_status_bar,
                parent,
                false
            ) as RelativeLayout?
            viewHolder.mItemLayout?.tag = viewHolder
            viewHolder.position = position
            viewHolder.catName = viewHolder.mItemLayout?.findViewById(R.id.catName)
            viewHolder.button = viewHolder.mItemLayout?.findViewById(R.id.catIcon)

            viewHolder.button?.scaleX = 3F
            viewHolder.button?.scaleY = 3F
            viewHolder.button?.x = -55F
            viewHolder.button?.y = 10F

        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        if(currItem.getId() == favId) {
            viewHolder.mItemLayout?.findViewById<ImageView>(R.id.catStatusView)
                ?.setBackgroundResource(R.drawable.fav_background_cat_status)
            viewHolder.mItemLayout?.findViewById<TextView>(R.id.catName)
                ?.setBackgroundResource(R.drawable.fav_catname)
        } else {
            viewHolder.mItemLayout?.findViewById<ImageView>(R.id.catStatusView)
                ?.setBackgroundResource(R.drawable.background_cat_status)
            viewHolder.mItemLayout?.findViewById<TextView>(R.id.catName)
                ?.setBackgroundResource(R.drawable.catname)
        }

        var catName = currItem.getName()

        val fileName = "cat_${currItem.getId()}.png"
        val directory = mContext.getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(directory, "$fileName")

        val bmOptions = BitmapFactory.Options()

        var currBitmap = BitmapFactory.decodeFile(file.absolutePath, bmOptions)
        viewHolder.button?.setImageBitmap(currBitmap)

        if(catName != null && catName != "") {
            val newName = "  $catName"
            viewHolder.catName?.text = newName
            viewHolder.catName?.textSize = 20F
        }

        viewHolder.button?.setOnLongClickListener(
            View.OnLongClickListener {
                val popUpMenu = PopupMenu(mContext, it)
                popUpMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.share -> {
                            referenceActivity.shareButtonPressed(viewHolder.button!!)
                            true
                        }
                        R.id.delete -> {
                            referenceActivity.delete(currItem)
                            true
                        }
                        R.id.rename -> {
                            referenceActivity.changeName(currItem, this)
                            this.notifyDataSetChanged()
                            true
                        }
                        R.id.favorite -> {
                            referenceActivity.setFav(currItem)

                            true
                        }
                        else -> {
                            true
                        }
                    }

                }
                popUpMenu.inflate(R.menu.popup_menu)
                popUpMenu.show()
                true
            }
        )


        return viewHolder.mItemLayout
    }

    internal class ViewHolder: Serializable{
        var position: Int = 0
        var mItemLayout: RelativeLayout? = null
        var catName: TextView? = null
        var button: ImageView? = null

    }
}