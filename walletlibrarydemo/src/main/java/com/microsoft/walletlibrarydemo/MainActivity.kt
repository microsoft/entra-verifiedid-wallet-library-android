package com.microsoft.walletlibrarydemo

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.resolvers.OpenIdURLRequestResolver
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener { onClickButton() }

    }

    private fun onClickButton() {
        val text = findViewById<TextView>(R.id.textview)
        VerifiedIdClientBuilder(applicationContext).build()
        runBlocking {
            // Use the test uri here
            val requestResolver = OpenIdURLRequestResolver().resolve(VerifiedIdRequestURL(Uri.parse("")))
            text.text = (requestResolver.rawRequest as PresentationRequest).entityIdentifier
        }
    }
}