package com.example.cat_status

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable

// author: Isaiah Bentz
class CatAppearanceGenerator(var resources: Resources, var applicationContext: Context) : Serializable{

    var mFrame: FrameLayout? = null

    var catBase: Bitmap? = null
    var catCollar: Bitmap? = null
    var catLine: Bitmap? = null
    var baseColor: Int? = null
    var patColor: Int? = null
    var collarColor: Int? = null
    var pats: Array<Int>? = null
    var syms: Array<Int>? = null
    var catColors: Array<Int>? = null
    var collarColors: Array<Int>? = null
    var pat: Bitmap? = null

    var sym: Bitmap? = null

    fun generateCats(CatID : Int) : String? {
        Log.i(TAG, "Entered onCreate")


        catBase = BitmapFactory.decodeResource(resources, R.drawable.catbase)
        catCollar = BitmapFactory.decodeResource(resources, R.drawable.catcollar)
        catLine = BitmapFactory.decodeResource(resources, R.drawable.catline)

        pats = arrayOf(R.drawable.pat01, R.drawable.pat02, R.drawable.pat03, R.drawable.pat04, R.drawable.pat05,
            R.drawable.pat06, R.drawable.pat07, R.drawable.pat08, R.drawable.pat09, R.drawable.pat10)

        syms = arrayOf(R.drawable.sym01, R.drawable.sym02, R.drawable.sym03, R.drawable.sym04, R.drawable.sym05,
            R.drawable.sym06, R.drawable.sym07, R.drawable.sym08, R.drawable.sym09, R.drawable.sym10)

        // setting up color options, if changing amounts of colors be sure to change r3, r4, and r5
        val black = Color.argb(1,38,38,38)
        val gray = Color.GRAY
        val white = Color.WHITE
        val brown = Color.argb(1,150, 102, 59)
        val orange = Color.argb(1,255, 150, 56)
        val yellow = Color.argb(1, 252, 250, 210)
        val red = Color.RED
        val blue = Color.BLUE
        val green = Color.GREEN

        // possible base and pattern colors
        catColors = arrayOf(black, gray, white, brown, orange, yellow)
        // possible collar colors
        collarColors = arrayOf(red, blue, green)

        Log.i(TAG, "calling generate")
        return generate(CatID.toString())
    }

    // convert catView to bitmap, adapted from https://dev.to/pranavpandey/android-create-bitmap-from-a-view-3lck
    private fun viewToBitmap(view: View, width: Int, height: Int, ): Bitmap? {
        val useWidth = (width * Resources.getSystem().displayMetrics.density).toInt()
        val useHeight = (height * Resources.getSystem().displayMetrics.density).toInt()
        Log.i(TAG, "Height: $useHeight , Width: $useWidth")
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(useWidth, useHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap

    }

    // convert bitmap from catView to png, file can be found in files app under images, adapted from https://stackoverflow.com/questions/7769806/convert-bitmap-to-file
    private fun bitmapToPng(bitmap: Bitmap, fileName: String): File? {
        var file: File? = null
        return try {
            file = File(applicationContext.filesDir, fileName)
            file.createNewFile()

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val bitMapData = byteArrayOutputStream.toByteArray()
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bitMapData)
            fileOutputStream.flush()
            fileOutputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file
        }
    }

    private fun generate(CatID: String): String? {
        Log.i(TAG, "generating")

        // getting pattern
        val r1 = (pats?.indices)?.random()
        pat = BitmapFactory.decodeResource(resources, pats?.get(r1!!)!!)

        // getting symbol
        val r2 = (syms?.indices)?.random()
        sym = BitmapFactory.decodeResource(resources, syms?.get(r2!!)!!)

        // getting colors
        val r3 = (catColors?.indices)?.random()
        val r4 = (catColors?.indices)?.random()
        val r5 = (collarColors?.indices)?.random()
        baseColor = catColors?.get(r3!!)
        patColor = catColors?.get(r4!!)
        collarColor = collarColors?.get(r5!!)

        val catView = CatView(applicationContext)

        mFrame?.addView(catView)
        // convert catView to file
        val file = bitmapToPng(viewToBitmap(catView, BITMAP_WIDTH, BITMAP_HEIGHT)!!, "cat_$CatID.png")
        if (file != null) {
            Log.i("HELLO", file.absolutePath)
            return file.absolutePath
        } else {
            return null
        }
    }

    inner class CatView internal constructor(context: Context) : View(context) {

        var scaledBase: Bitmap? = null
        var scaledCollar: Bitmap? = null
        var scaledLine: Bitmap? = null
        var scaledPat: Bitmap? = null
        var scaledSym: Bitmap? = null
        val mPainter = Paint()
        // cat position
        val xPos = 200f
        val yPos = 200f
        // change scaled width and height by multiplying/dividing BITMAP_WIDTH/HEIGHT by the same value
        var scaledWidth = BITMAP_WIDTH
        var scaledHeight = BITMAP_HEIGHT

        init {
            Log.i(TAG, "init CatView"+(pat is Bitmap).toString())
            mPainter.isAntiAlias = true
            createScaledBitmap()
        }

        fun createScaledBitmap() {

            this.scaledBase = Bitmap.createScaledBitmap(catBase!!, scaledWidth, scaledHeight, false)
            this.scaledCollar = Bitmap.createScaledBitmap(catCollar!!, scaledWidth, scaledHeight, false)
            this.scaledLine = Bitmap.createScaledBitmap(catLine!!, scaledWidth, scaledHeight, false)
            this.scaledPat = Bitmap.createScaledBitmap(pat!!, scaledWidth, scaledHeight, false)
            this.scaledSym = Bitmap.createScaledBitmap(sym!!, scaledWidth, scaledHeight, false)

        }


        @Synchronized
        override fun onDraw(canvas: Canvas) {
            Log.i(TAG, "onDraw")

            canvas.save()
            // change color
            var filter = baseColor?.let { LightingColorFilter(it, 1) }
            mPainter.colorFilter = filter
            canvas.drawBitmap(scaledBase!!, xPos, yPos, mPainter)
            canvas.restore()
            canvas.save()
            filter = collarColor?.let { LightingColorFilter(it, 1) }
            mPainter.colorFilter = filter
            canvas.drawBitmap(scaledCollar!!, xPos, yPos, mPainter)
            canvas.restore()
            canvas.save()
            filter = patColor?.let { LightingColorFilter(it, 1) }
            mPainter.colorFilter = filter
            canvas.drawBitmap(scaledPat!!, xPos, yPos, mPainter)
            canvas.restore()
            canvas.save()
            // remove colorFilter
            mPainter.reset()
            mPainter.isAntiAlias = true
            canvas.drawBitmap(scaledLine!!, xPos, yPos, mPainter)
            canvas.restore()
            canvas.save()
            canvas.drawBitmap(scaledSym!!, xPos, yPos, mPainter)
            canvas.restore()
        }

    }

    companion object {
        val BITMAP_HEIGHT = 250
        val BITMAP_WIDTH  = 185
        val TAG = "cat"
    }




}
