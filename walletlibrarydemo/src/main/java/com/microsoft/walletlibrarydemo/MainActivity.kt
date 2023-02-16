package com.microsoft.walletlibrarydemo

import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.ContractIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    private lateinit var verifiedIdRequest: VerifiedIdRequest<*>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener { onClickButton() }
        val buttonComplete = findViewById<Button>(R.id.buttonComplete)
        buttonComplete.setOnClickListener { completeIssuance() }

    }

    private fun onClickButton() {
        val text = findViewById<TextView>(R.id.textview)
        val nameLabel = findViewById<TextView>(R.id.nameLabel)
        val name = findViewById<EditText>(R.id.name)
        val companyLabel = findViewById<TextView>(R.id.companyLabel)
        val company = findViewById<EditText>(R.id.company)
        val button = findViewById<Button>(R.id.button)
        val buttonComplete = findViewById<Button>(R.id.buttonComplete)
        val verifiedIdClient = VerifiedIdClientBuilder(applicationContext).build()
        runBlocking {
            // Use the test uri here
            verifiedIdRequest =
                verifiedIdClient.createRequest(VerifiedIdRequestURL(Uri.parse("openid-vc://?request_uri=https://verifiedid.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/issuanceRequests/c7893e52-9131-40b2-9c02-621791a1182d")))
            if (verifiedIdRequest is OpenIdPresentationRequest)
                text.text =
                    "Presentation request from ${verifiedIdRequest.requesterStyle.requester}"
            else if (verifiedIdRequest is ContractIssuanceRequest) {
                text.text =
                    "Issuance request for ${(verifiedIdRequest as ContractIssuanceRequest).verifiedIdStyle?.title}"
                nameLabel.visibility = View.VISIBLE
                name.visibility = View.VISIBLE
                companyLabel.visibility = View.VISIBLE
                company.visibility = View.VISIBLE
                buttonComplete.visibility = View.VISIBLE
                button.visibility = View.GONE
            }
        }
    }

    private fun completeIssuance() {
        val text = findViewById<TextView>(R.id.textview)
        val name = findViewById<EditText>(R.id.name)
        val company = findViewById<EditText>(R.id.company)
/*        name.setOnEditorActionListener { textView: TextView, actionId: Int, event: KeyEvent? ->
            onEditorAction(textView, actionId, event, "name")
        }
        company.setOnEditorActionListener { textView: TextView, actionId: Int, event: KeyEvent? ->
            onEditorAction(textView, actionId, event, "company")
        }*/
        val requirement = verifiedIdRequest.requirement
        if (requirement is GroupRequirement) {
            val requirements = requirement.requirements
            for (req in requirements) {
                if (req is SelfAttestedClaimRequirement) {
                    if (req.claim == "name")
                        req.fulfill(name.text.toString())
                    if (req.claim == "company")
                        req.fulfill(company.text.toString())
                }
            }
        }
        runBlocking {
            val response = verifiedIdRequest.complete()
            text.text = response.getOrDefault("").toString()
        }
    }

    private fun onEditorAction(
        view: TextView,
        actionId: Int,
        event: KeyEvent?,
        claimName: String
    ): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || (actionId == EditorInfo.IME_NULL && event?.action == KeyEvent.ACTION_DOWN)) {
            val requirement = verifiedIdRequest.requirement
            if (requirement is GroupRequirement) {
                val requirements = requirement.requirements
                for (req in requirements) {
                    if (req is SelfAttestedClaimRequirement) {
                        if (req.claim == claimName)
                            req.fulfill(view.text.toString())
                    }
                }
            }
            return true
        }
        return false
    }
}