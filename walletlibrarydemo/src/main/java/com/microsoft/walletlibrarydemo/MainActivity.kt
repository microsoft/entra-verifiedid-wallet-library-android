package com.microsoft.walletlibrarydemo

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
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
        val verifiedIdClient = VerifiedIdClientBuilder(applicationContext).build()
        runBlocking {
            // Use the test uri here
            val requestResolver: VerifiedIdRequest? =
                verifiedIdClient.createRequest(VerifiedIdRequestURL(Uri.parse("openid-vc://?request_uri=https://beta.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/issuanceRequests/58f80671-50e4-4ef9-b93b-ef0b5b11ae27")))
            if (requestResolver != null) {
                if (requestResolver is VerifiedIdPresentationRequest)
                    text.text =
                        "Presentation request from ${requestResolver.requesterStyle.requester}"
                else if (requestResolver is VerifiedIdIssuanceRequest)
                    text.text =
                        "Issuance request for ${requestResolver.verifiedIdStyle?.title} issued by ${requestResolver.verifiedIdStyle?.issuer}"
            } else {
                text.text = "Fetch Request failed"
            }
        }
    }
}