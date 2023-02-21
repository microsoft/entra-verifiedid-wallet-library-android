package com.microsoft.walletlibrarydemo

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrarydemo.databinding.ActivityMainBinding
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var verifiedIdRequest: VerifiedIdRequest<*>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener { onClickButton() }
        binding.buttonComplete.setOnClickListener { completeIssuance() }

    }

    private fun onClickButton() {
        val verifiedIdClient = VerifiedIdClientBuilder(applicationContext).build()
        binding.buttonComplete.isEnabled = true
        binding.button.isEnabled = false
        runBlocking {
            // Use the test uri here
            verifiedIdRequest =
                verifiedIdClient.createRequest(VerifiedIdRequestURL(Uri.parse("openid-vc://?request_uri=https://verifiedid.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/issuanceRequests/624bcccb-294f-4742-b76e-8d528c728541")))
            if (verifiedIdRequest is OpenIdPresentationRequest)
                binding.textview.text = "Presentation request from ${verifiedIdRequest.requesterStyle.requester}"
            else if (verifiedIdRequest is ManifestIssuanceRequest) {
                binding.textview.text = "Issuance request for ${(verifiedIdRequest as ManifestIssuanceRequest).verifiedIdStyle?.title}"
                configureSelfIssuedFields()
            }
        }
    }

    private fun configureSelfIssuedFields() {
        binding.nameLabel.visibility = View.VISIBLE
        binding.name.visibility = View.VISIBLE
        binding.companyLabel.visibility = View.VISIBLE
        binding.company.visibility = View.VISIBLE
    }

    private fun completeIssuance() {
        val requirement = verifiedIdRequest.requirement
        if (requirement is GroupRequirement) {
            val requirements = requirement.requirements
            for (req in requirements) {
                if (req is SelfAttestedClaimRequirement) {
                    if (req.claim == "name")
                        req.fulfill(binding.name.text.toString())
                    if (req.claim == "company")
                        req.fulfill(binding.company.text.toString())
                }
            }
        }
        runBlocking {
            val response = verifiedIdRequest.complete()
            binding.textview.text = response.getOrDefault("").toString()
        }
        binding.buttonComplete.isEnabled = false
    }
}