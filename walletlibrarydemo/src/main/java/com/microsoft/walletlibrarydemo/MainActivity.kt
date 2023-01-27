package com.microsoft.walletlibrarydemo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.walletlibrary.URLVerifiedIdClientInput
import com.microsoft.walletlibrary.VerifiedIdClientBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener { onClickButton() }

    }

    private fun onClickButton() {
        val text = findViewById<TextView>(R.id.textview)
        val builder = VerifiedIdClientBuilder(applicationContext, URLVerifiedIdClientInput("testsource"))
//            .verifiedIdClientInput(URLVerifiedIdClientInput("testsource"))
            .build()
        text.text = builder.rootOfTrust.source
    }
}