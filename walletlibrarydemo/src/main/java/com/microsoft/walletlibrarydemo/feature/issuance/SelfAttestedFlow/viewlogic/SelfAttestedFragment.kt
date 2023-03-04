package com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.viewlogic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrarydemo.databinding.SelfAttestedFragmentBinding
import com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.presentationlogic.RequirementsAdapter
import com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.presentationlogic.SelfAttestedFlowViewModel
import kotlinx.coroutines.runBlocking

class SelfAttestedFragment: Fragment() {
    private var _binding: SelfAttestedFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SelfAttestedFlowViewModel

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
        viewModel = SelfAttestedFlowViewModel(requireContext())
        runBlocking {
            viewModel.initiateIssuance()
            val verifiedIdRequest = viewModel.verifiedIdRequest
            binding.initiateIssuance.isEnabled = false
            if (verifiedIdRequest is OpenIdPresentationRequest)
                binding.textview.text =
                    "Presentation request from ${verifiedIdRequest.requesterStyle.requester}"
            else if (verifiedIdRequest is ManifestIssuanceRequest) {
                binding.textview.text =
                    "Issuance request for ${verifiedIdRequest.verifiedIdStyle.title}"
                configureViewsForSelfAttestedRequirements(verifiedIdRequest.requirement)
                binding.completeIssuance.isEnabled = true
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
            viewModel.completeIssuance()
            configureIssuanceCompletionView()
        }
    }

    private fun configureIssuanceCompletionView() {
        binding.completeIssuance.isEnabled = false
        val response = viewModel.verifiedIdResult
        response?.let {
            if (response.isSuccess)
                binding.textview.text = response.getOrDefault("").toString()
            else
                binding.textview.text = response.exceptionOrNull().toString()
        }
    }
}