package com.example.animalfaceapp

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.example.animalfaceapp.databinding.ActivityResultBinding
import java.io.IOException
import java.util.*

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var cwm: ClassifierWithModel
    private lateinit var morebtn: Button
    private lateinit var resultimg: ImageView
    private lateinit var resulttxt: TextView
    private lateinit var resultpercent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        cwm = ClassifierWithModel(this)
        try {
            cwm.init()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        morebtn = findViewById(R.id.restart_btn)
        resultimg = findViewById(R.id.result_img)
        resulttxt = findViewById(R.id.result_txt)
        resultpercent = findViewById(R.id.result_percent)

        val byteArray = intent.getByteArrayExtra("image")
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        if (bitmap != null) {
            val (first, second) = cwm.classify(bitmap)
            val resultStr: String =
                java.lang.String.format(Locale.ENGLISH, "%s", first)
            resultimg.setImageBitmap(bitmap)
            resulttxt.text = resultStr
            resultpercent.text = "닮은 비율 : " + String.format("%.2f", second * 100) + "%"
        }

        morebtn.setOnClickListener {
            val intent = Intent(applicationContext, InputActivity::class.java)
            finish()
            cwm.finish()
            startActivity(intent)
        }
    }
}