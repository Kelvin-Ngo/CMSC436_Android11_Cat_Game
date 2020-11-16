package com.example.cat_status

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import java.io.Serializable
import kotlin.collections.ArrayList

// Adaptor class for listView. Code is based off of Lab4: UILabs
class catAdapter(private val mContext: Context) : BaseAdapter(), Serializable{
    private val mItems = ArrayList<Cat>()

    override fun getCount(): Int {
        return mItems.size
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
            viewHolder.hungerBar = viewHolder.mItemLayout?.findViewById(R.id.hungerBar)
            viewHolder.thirstBar = viewHolder.mItemLayout?.findViewById(R.id.thirstBar)
            viewHolder.boredomBar = viewHolder.mItemLayout?.findViewById(R.id.boredomBar)
            viewHolder.catName = viewHolder.mItemLayout?.findViewById(R.id.catName)
            viewHolder.background = viewHolder.mItemLayout?.findViewById(R.id.catStatusView)
            viewHolder.button = viewHolder.mItemLayout?.findViewById(R.id.catIcon)

        } else {
            viewHolder = convertView.tag as ViewHolder
        }


        var catName = currItem.getName()
        var hungerPercentMax = currItem.getHungerMax()
        var thirstPercentMax = currItem.getThirstMax()
        var boredomPercentMax = currItem.getBoredomMax()

        if(catName != null && catName != "") {
            val newName = "     $catName"
            viewHolder.catName?.text = newName
        }


        viewHolder.hungerBar?.max = hungerPercentMax
        viewHolder.hungerBar?.progress = hungerPercentMax

        viewHolder.thirstBar?.max = thirstPercentMax
        viewHolder.thirstBar?.progress = thirstPercentMax

        viewHolder.boredomBar?.max = boredomPercentMax
        viewHolder.boredomBar?.progress = boredomPercentMax
        Log.i("HELLO", "HELLO0")
        viewHolder.button?.setOnClickListener(
            View.OnClickListener {
                Log.i("HELLO", "HELLO1")
            }
        )

        return viewHolder.mItemLayout
    }

    internal class ViewHolder: Serializable{
        var position: Int = 0
        var mItemLayout: RelativeLayout? = null
        var hungerBar: ProgressBar? = null
        var thirstBar: ProgressBar? = null
        var boredomBar: ProgressBar? = null
        var catName: TextView? = null
        var background: View? = null
        var button: Button? = null
    }

}