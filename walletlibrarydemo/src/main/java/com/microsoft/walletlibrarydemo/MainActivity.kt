package com.microsoft.walletlibrarydemo

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.service.PresentationRequest
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
        VerifiableCredentialSdk.init(applicationContext)
        runBlocking {
            val requestResolver =
                OpenIdURLRequestResolver().resolve(VerifiedIdRequestURL(Uri.parse("openid-vc://?request_uri=https://beta.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/presentationRequests/1ae058df-7659-4b15-8dcc-ab895ce9dbc4")))
            text.text = (requestResolver.rawRequest as PresentationRequest).entityIdentifier
        }
    }
}