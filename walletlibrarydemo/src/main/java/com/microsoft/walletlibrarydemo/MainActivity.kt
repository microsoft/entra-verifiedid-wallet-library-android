package com.microsoft.walletlibrarydemo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener { onClickButton() }

    }

    private fun onClickButton() {
        val text = findViewById<TextView>(R.id.textview)
        text.text = "Hello from library"
    }
}