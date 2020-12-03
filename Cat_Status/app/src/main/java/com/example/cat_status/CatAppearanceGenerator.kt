package com.example.cat_status

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.Log
import android.view.View
import java.io.Serializable


class CatAppearanceGenerator(resources: Resources, applicationContext: Context) : Serializable{

    var catView : View? = null
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

    val resources = resources
    val applicationContext = applicationContext

    fun generateCat(catId: Int) : Bitmap? {

        catBase = BitmapFactory.decodeResource(resources, R.drawable.catbase)
        catCollar = BitmapFactory.decodeResource(resources, R.drawable.catcollar)
        catLine = BitmapFactory.decodeResource(resources, R.drawable.catline)

        pats = arrayOf(
            R.drawable.pat01,
            R.drawable.pat02,
            R.drawable.pat03,
            R.drawable.pat04,
            R.drawable.pat05,
            R.drawable.pat06,
            R.drawable.pat07,
            R.drawable.pat08,
            R.drawable.pat09,
            R.drawable.pat10
        )

        syms = arrayOf(
            R.drawable.sym01,
            R.drawable.sym02,
            R.drawable.sym03,
            R.drawable.sym04,
            R.drawable.sym05,
            R.drawable.sym06,
            R.drawable.sym07,
            R.drawable.sym08,
            R.drawable.sym09,
            R.drawable.sym10
        )

        // setting up color options, if changing amounts of colors be sure to change r3, r4, and r5
        val black = Color.BLACK
        val gray = Color.GRAY
        val white = Color.WHITE
        val brown = Color.argb(1, 150, 102, 59)
        val orange = Color.argb(1, 255, 150, 56)
        val yellow = Color.argb(1, 252, 245, 141)
        val red = Color.RED
        val blue = Color.BLUE
        val green = Color.GREEN

        // possible base and pattern colors
        catColors = arrayOf(black, gray, white, brown, orange, yellow)
        // possible collar colors
        collarColors = arrayOf(red, blue, green)

        Log.i(TAG, "calling generate")
        val currView = generate(resources, applicationContext)
        if(currView != null) {
            Log.i("Test123", "HELLO")

        } else {
            Log.i("TEST123", "OH NO")
        }
        return currView?.let { loadBitmapFromView(it, BITMAP_WIDTH, BITMAP_HEIGHT) }
    }

    fun loadBitmapFromView(v: View, width: Int, height: Int): Bitmap? {
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.layout(0, 0, width, height)
        //Get the view’s background
        val bgDrawable = v.background
        if (bgDrawable != null) //has background drawable, then draw it on the canvas
            bgDrawable.draw(c) else  //does not have background drawable, then draw white background on the canvas
            c.drawColor(Color.WHITE)
        v.draw(c)
        return b
    }


    fun generate(resources: Resources, applicationContext: Context) : View?{
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

        catView = CatView(applicationContext)
        return catView
    }

    inner class CatView internal constructor(context: Context) : View(context), Serializable{

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
            Log.i(TAG, "init CatView" + (pat is Bitmap).toString())
            mPainter.isAntiAlias = true
            createScaledBitmap()
        }

        fun createScaledBitmap() {

            this.scaledBase = Bitmap.createScaledBitmap(catBase!!, scaledWidth, scaledHeight, false)
            this.scaledCollar = Bitmap.createScaledBitmap(
                catCollar!!,
                scaledWidth,
                scaledHeight,
                false
            )
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