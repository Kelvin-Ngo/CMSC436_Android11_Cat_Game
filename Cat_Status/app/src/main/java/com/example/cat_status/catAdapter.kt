package com.example.cat_status
import android.app.AlertDialog
import android.bluetooth.BluetoothClass
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.text.InputFilter
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.Serializable

// author: Kelvin Ngo
// Adaptor class for listView. Code is based off of Lab4: UILabs and AudioVideoAudioManager example
class catAdapter(private val mContext: Context) : BaseAdapter(), Serializable {
    private val mItems = ArrayList<Cat>()
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var catsToBeDelete = ArrayList<Cat>()
    private lateinit var mSendCat: Cat
    private var multiDelete = false
    private var isMute = false
    private var favCat : Cat? = null


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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var currItem = mItems[position]
        val viewHolder: ViewHolder

        if (convertView == null) {
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

        setUpFavCat(viewHolder, currItem)
        setUpCatImage(viewHolder, currItem)
        setUpMediaPlayerAndMenu(viewHolder, currItem)


        viewHolder.checkBox = viewHolder.mItemLayout?.findViewById(R.id.checkbox)
        if(multiDelete) {
            viewHolder.checkBox?.visibility = View.VISIBLE
            setUpCheckBox(viewHolder, currItem)
        } else {
            viewHolder.checkBox?.visibility = View.GONE
        }

        return viewHolder.mItemLayout
    }

    fun setMultiDelete(bol : Boolean) {
        multiDelete = bol
    }

    fun setIsMute(bol : Boolean) {
        isMute = bol
    }

    //sets up checkboxes, need a viewHolder that is the current view and a cat that is the
    //current cat
    private fun setUpCheckBox(view :ViewHolder, currItem: Cat) {
        if(view == null) {
            Log.i("HELLO", "HELLO123")
        }
        view.checkBox!!.isChecked = currItem.isMarkedDelete()
        view.button?.setOnLongClickListener(null)

        // toggling button press to confirm whether the currItem should be deleted
        view.button?.setOnClickListener {
            if (currItem.isMarkedDelete()) {
                currItem.markedForDelete(false)
                catsToBeDelete.remove(currItem)
                view.checkBox!!.isChecked = false

            } else {
                catsToBeDelete.add(currItem)
                currItem.markedForDelete(true)
                view.checkBox!!.isChecked = true
            }
        }

        // toggle the checkbox to do the same option as button press
        view.checkBox?.setOnClickListener {
            if (currItem.isMarkedDelete()) {
                view.checkBox!!.isChecked = false
                currItem.markedForDelete(false)
                catsToBeDelete.remove(currItem)
            } else {
                view.checkBox!!.isChecked = true
                catsToBeDelete.add(currItem)
                currItem.markedForDelete(true)
            }
        }
    }

    fun clearDeleteList() {
        catsToBeDelete = ArrayList()
    }

    fun getCatsToBeDeleted(): ArrayList<Cat> {
        return catsToBeDelete
    }

    // sets the favorite cat's view as red
    private fun setUpFavCat(view :ViewHolder, currItem: Cat) {
        if (currItem == favCat) {
            view.mItemLayout?.findViewById<ImageView>(R.id.catStatusView)
                ?.setBackgroundResource(R.drawable.fav_background_cat_status)
            view.mItemLayout?.findViewById<TextView>(R.id.catName)
                ?.setBackgroundResource(R.drawable.fav_catname)
        } else {
            view.mItemLayout?.findViewById<ImageView>(R.id.catStatusView)
                ?.setBackgroundResource(R.drawable.background_cat_status)
            view.mItemLayout?.findViewById<TextView>(R.id.catName)
                ?.setBackgroundResource(R.drawable.catname)
        }
    }


    private fun setUpCatImage(view :ViewHolder, currItem: Cat) {
        var catName = currItem.getName()
        val file = File(currItem.getImageLoc())

        val bmOptions = BitmapFactory.Options()

        var currBitmap = BitmapFactory.decodeFile(file.absolutePath, bmOptions)
        view.button?.setImageBitmap(currBitmap)

        if (catName != null && catName != "") {
            val newName = "  $catName"
            view.catName?.text = newName
            view.catName?.textSize = 17F
        }
    }

    private fun setUpMediaPlayerAndMenu(view :ViewHolder, currItem: Cat) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(mContext, R.raw.cat_meow)
        }

        if (audioManager == null) {
            audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        }
        if (isMute) {
            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        } else {
            audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.let {
                audioManager!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    it / 2, 0
                )
            }
        }

        view.button?.setOnLongClickListener(
            View.OnLongClickListener {
                mediaPlayer?.start()
                val context = ContextThemeWrapper(mContext, R.style.menuTheme)
                val popUpMenu = PopupMenu(context, it)

                popUpMenu.setOnMenuItemClickListener {
                    val rootContext = view.mItemLayout?.rootView?.context

                    when (it.itemId) {
                        R.id.share -> {
                            shareButtonPressed(currItem, rootContext)
                            true
                        }
                        R.id.delete -> {
                            if (rootContext != null) {
                                delete(currItem, rootContext)
                            } else {
                                Toast.makeText(mContext, "FAILED", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }
                        R.id.rename -> {
                            if (rootContext != null) {
                                changeName(currItem, this, rootContext)
                            }
                            this.notifyDataSetChanged()
                            true
                        }
                        R.id.favorite -> {
                            setFav(currItem)

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
    }

    fun getFav() : Cat? {
        return favCat
    }

    fun setFav(currCat: Cat) {

        if (currCat != favCat) {
            favCat = currCat
            notifyDataSetChanged()
        }
    }

    fun multiDelete(cats: ArrayList<Cat>, currContext : Context) {
        if(cats.isEmpty()) {

        } else {
            val builder = AlertDialog.Builder(currContext)
            builder.setTitle("Do You Really Want to Delete these Cats?")
            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                for (currCat in cats) {
                    if (favCat != null && currCat == favCat) {
                        favCat = null
                    }

                    val file = File(currCat.getImageLoc())
                    file.delete()

                    remove(currCat)
                }
                notifyDataSetChanged()
            })
            builder.setNegativeButton("CANCEL", null)

            builder.show()
        }
    }


    // function to change name of cats, takes a cat and the adapter the cat is stored in.
    // it first creates a dialog box asking for what the new name of the cat is. When user clicks
    // ok, the method first checks to see if favCat is the current cat getting its name changed.
    // If so it will change the name of favCat and current cat if not, it will just change
    // the current cat's name
    fun changeName(currCat: Cat, mAdapter: catAdapter, rootContext: Context) {
        val builder = AlertDialog.Builder(rootContext)
        builder.setTitle("New Name (Limit ${CatHouseActivity.MAXCHARNAME} characters):")

        val textInput = EditText(rootContext)
        textInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(CatHouseActivity.MAXCHARNAME))
        builder.setView(textInput)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            if (favCat != null && currCat == favCat) {
                val newName = textInput.text.toString()
                currCat.setName(" $newName")
                favCat = currCat
            } else {
                val newName = textInput.text.toString()
                currCat.setName(" $newName")
            }
        })

        builder.setNegativeButton("CANCEL", null)

        builder.show()
    }


    // This method is to handle the shared button being pressed. We need permission to share
    // data so it first checks for permission and if it doesn't have it, it ask for permission
    // It will then call sendImage()

    // opens up a dialog box confirming if user really wants to delete the cat. If so, then the
    // cat will be removed from the adapter and if favCat was the cat removed, favCat will be set
    // to null
    private fun delete(currCat: Cat, rootContext : Context) {
        val builder = AlertDialog.Builder(rootContext)
        builder.setTitle("Do You Really Want to Delete this Cat?")

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            if (favCat != null && currCat == favCat) {
                favCat = null
            }

            val file = File(currCat.getImageLoc())

            file.delete()
            remove(currCat)
            notifyDataSetChanged()
        })

        builder.setNegativeButton("CANCEL", null)

        builder.show()
    }

    private fun shareButtonPressed(cat: Cat, rootContext: Context?) {
        val permissions = ActivityCompat.checkSelfPermission(
            mContext,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissions == PackageManager.PERMISSION_GRANTED) {
            mSendCat = cat
            sendImage(cat)
        } else {
            Toast.makeText(rootContext, "Requires permissions, please give access if you want to share cats", Toast.LENGTH_LONG).show()


        }
    }

    // Handles sending the Image of the clicked cat. It first needs to retrieve the image and the
    // user will choose how to send it and how to send it to.
    private fun sendImage(cat: Cat) {
        val file =  File(cat.getImageLoc())

        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val photoURI = FileProvider.getUriForFile(
            mContext,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )

        intent.putExtra(Intent.EXTRA_STREAM, photoURI)

        val components = arrayOf(ComponentName(mContext, BluetoothClass::class.java))
        intent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, components)

        if (favCat != null && favCat == cat) {
            intent.putExtra(Intent.EXTRA_TEXT, "My favorite cat, ${cat.getName()}!!!")
        } else {
            intent.putExtra(Intent.EXTRA_TEXT, "My cat, ${cat.getName()}!!!")
        }

        intent.type = "image/png"

        val chooser = Intent.createChooser(intent, "Share File")

        val resInfoList =
            mContext.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            mContext.grantUriPermission(
                packageName,
                photoURI,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext.startActivity(chooser)
    }

    internal class ViewHolder: Serializable{
        var position: Int = 0
        var mItemLayout: RelativeLayout? = null
        var catName: TextView? = null
        var button: ImageView? = null
        var checkBox: CheckBox? = null


    }
}