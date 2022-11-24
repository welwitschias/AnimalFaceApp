package com.example.animalfaceapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var start: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start = findViewById(R.id.start_btn)

        start.setOnClickListener {
            val intent = Intent(applicationContext, InputActivity::class.java)
            startActivity(intent)
        }
    }
}