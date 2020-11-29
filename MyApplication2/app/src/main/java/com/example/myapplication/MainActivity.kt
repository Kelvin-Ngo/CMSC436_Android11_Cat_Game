package com.example.myapplication

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var foodbar: ProgressBar
    lateinit var waterbar: ProgressBar
    lateinit var toyButton: ImageButton
    lateinit var cathouseButton: ImageButton
    lateinit var food: ImageView
    lateinit var water: ImageView
    lateinit var mCountDownTimerFood: CountDownTimer
    lateinit var mCountDownTimerWater: CountDownTimer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // set up food bar and image
        foodbar = findViewById(R.id.food)
        mCountDownTimerFood.start()
        foodbar.setOnClickListener(View.OnClickListener {
            foodbar.setProgress(100, true)
            mCountDownTimerFood.start()
        })
        food = findViewById(R.id.foodPNG)
        food.setImageResource(R.drawable.food)

        //set up water bar and image
        waterbar = findViewById(R.id.water)
        mCountDownTimerWater = object : CountDownTimer(100000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                waterbar.setProgress(100-(100000 - millisUntilFinished.toInt())/1000)

            }

            override fun onFinish() {
                waterbar.setProgress(0)
            }
        }
        mCountDownTimerWater.start()
        waterbar.setOnClickListener(View.OnClickListener {
            waterbar.setProgress(100, true)
            mCountDownTimerWater.start()
        })
        water = findViewById(R.id.waterPNG)
        water.setImageResource(R.drawable.water)

        //set up toy and image
        toyButton = findViewById(R.id.toy)
        toyButton.setImageResource(R.drawable.toy)

        //cat house button and iamge
        cathouseButton = findViewById(R.id.cathouse)
        cathouseButton.setImageResource(R.drawable.house)
    }

}