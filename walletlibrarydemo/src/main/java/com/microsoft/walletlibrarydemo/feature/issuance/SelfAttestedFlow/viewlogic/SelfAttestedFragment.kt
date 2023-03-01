package com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.viewlogic

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrarydemo.databinding.SelfAttestedFragmentBinding
import com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.presentationlogic.RequirementsAdapter
import kotlinx.coroutines.runBlocking

class SelfAttestedFragment: Fragment() {
    private var _binding: SelfAttestedFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var verifiedIdRequest: VerifiedIdRequest<*>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SelfAttestedFragmentBinding.inflate(inflater, container, false)
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
                verifiedIdClient.createRequest(VerifiedIdRequestURL(Uri.parse("")))
            if (verifiedIdRequest is OpenIdPresentationRequest)
                binding.textview.text =
                    "Presentation request from ${verifiedIdRequest.requesterStyle.requester}"
            else if (verifiedIdRequest is ManifestIssuanceRequest) {
                binding.textview.text =
                    "Issuance request for ${(verifiedIdRequest as ManifestIssuanceRequest).verifiedIdStyle?.title}"
                configureViewsForSelfAttestedRequirements(verifiedIdRequest.requirement)
            }
        }
    }

    private fun configureViewsForSelfAttestedRequirements(requirements: Requirement) {
        val requirement = if (requirements is GroupRequirement)
            requirements.requirements.map { it as SelfAttestedClaimRequirement }
        else listOf(requirements as SelfAttestedClaimRequirement)
        val adapter = RequirementsAdapter(requireContext(), requirement)
        binding.requirementsList.layoutManager = LinearLayoutManager(context)
        binding.requirementsList.isNestedScrollingEnabled = false
        binding.requirementsList.adapter = adapter
    }

    private fun completeIssuance() {
        runBlocking {
            val response = verifiedIdRequest.complete()
            binding.textview.text = response.getOrDefault("").toString()
        }
        binding.completeIssuance.isEnabled = false
    }
}