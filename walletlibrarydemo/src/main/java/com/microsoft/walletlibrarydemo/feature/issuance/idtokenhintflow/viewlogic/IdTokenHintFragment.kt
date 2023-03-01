package com.microsoft.walletlibrarydemo.feature.issuance.idtokenhintflow.viewlogic

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrarydemo.databinding.IdTokenHintFragmentBinding
import kotlinx.coroutines.runBlocking

class IdTokenHintFragment: Fragment() {
    private var _binding: IdTokenHintFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var verifiedIdRequest: VerifiedIdRequest<*>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = IdTokenHintFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureViews()
    }

    private fun configureViews() {
        binding.initiateIssuance.setOnClickListener { initiateIssuance() }
        binding.completeIssuance.setOnClickListener { completeIssuance() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initiateIssuance() {
        val verifiedIdClient = VerifiedIdClientBuilder(requireContext()).build()
        binding.completeIssuance.isEnabled = true
        binding.initiateIssuance.isEnabled = false
        runBlocking {
            // Use the test uri here
            verifiedIdRequest =
                verifiedIdClient.createRequest(VerifiedIdRequestURL(Uri.parse("openid-vc://?request_uri=https://verifiedid.did.msidentity.com/v1.0/tenants/9c59be8b-bd18-45d9-b9d9-082bc07c094f/verifiableCredentials/issuanceRequests/07fce9f9-328a-4b8e-82a7-0e9d3f867bc5")))
            if (verifiedIdRequest is OpenIdPresentationRequest)
                binding.textview.text =
                    "Presentation request from ${verifiedIdRequest.requesterStyle.requester}"
            else if (verifiedIdRequest is ManifestIssuanceRequest) {
                binding.textview.text =
                    "Issuance request for ${(verifiedIdRequest as ManifestIssuanceRequest).verifiedIdStyle?.title}"
                (verifiedIdRequest.requirement as IdTokenRequirement).fulfill(verifiedIdRequest.)
            }
        }
    }

    private fun completeIssuance() {
        runBlocking {
            val response = verifiedIdRequest.complete()
            binding.textview.text = response.getOrDefault("").toString()
        }
        binding.completeIssuance.isEnabled = false
    }
}