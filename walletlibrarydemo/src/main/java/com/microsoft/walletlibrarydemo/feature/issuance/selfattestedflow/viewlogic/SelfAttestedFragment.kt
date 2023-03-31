package com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.viewlogic

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedIdClaim
import com.microsoft.walletlibrarydemo.databinding.SelfAttestedFragmentBinding
import com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.presentationlogic.RequirementsAdapter
import com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.presentationlogic.SelfAttestedFlowViewModel
import com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.presentationlogic.VerifiableCredentialAdapter
import kotlinx.coroutines.runBlocking


class SelfAttestedFragment : Fragment() {
    private var _binding: SelfAttestedFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SelfAttestedFlowViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            viewModel.initiateIssuance(Uri.parse(binding.requestUrl.text.toString()))
            val verifiedIdRequestResult = viewModel.verifiedIdRequestResult
            binding.requestUrl.text.clear()
            binding.requestUrl.visibility = View.GONE
            binding.requestUrlLabel.visibility = View.GONE
            verifiedIdRequestResult?.let {
                if (verifiedIdRequestResult.isSuccess) {
                    val verifiedIdRequest = verifiedIdRequestResult.getOrNull()
                    binding.initiateIssuance.isEnabled = false
                    if (verifiedIdRequest is VerifiedIdPresentationRequest)
                        binding.textview.text =
                            "Presentation request from ${verifiedIdRequest.requesterStyle.name}"
                    else if (verifiedIdRequest is VerifiedIdIssuanceRequest) {
                        binding.textview.text =
                            "Issuance request from ${verifiedIdRequest.requesterStyle.name}"
                        configureViewsForSelfAttestedRequirements(verifiedIdRequest.requirement)
                        binding.completeIssuance.isEnabled = true
                    }
                }
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
            switchToPresentation()
        }
    }

    private fun configureIssuanceCompletionView() {
        binding.completeIssuance.isEnabled = false
        val response = viewModel.verifiedIdResult
        response?.let {
            if (response.isSuccess) {
                configureViewsForVerifiedIdClaims((response.getOrDefault("") as VerifiableCredential))
            } else
                binding.textview.text = response.exceptionOrNull().toString()
        }
        binding.requirementsList.visibility = View.GONE
    }

    private fun switchToPresentation() {
        binding.initiateIssuance.text = "Initiate Presentation"
        binding.completeIssuance.text = "Complete Presentation"
        binding.initiateIssuance.isEnabled = true
        binding.requestUrl.visibility = View.VISIBLE
        binding.requestUrlLabel.visibility = View.VISIBLE
        binding.initiateIssuance.setOnClickListener { initiatePresentation() }
        binding.completeIssuance.setOnClickListener { completePresentation() }
    }

    private fun initiatePresentation() {
        runBlocking {
            viewModel.initiatePresentation(Uri.parse(binding.requestUrl.text.toString()))
            val verifiedIdRequestResult = viewModel.verifiedIdRequestResult
            binding.requestUrl.text.clear()
            binding.requestUrl.visibility = View.GONE
            binding.requestUrlLabel.visibility = View.GONE
            verifiedIdRequestResult?.let {
                if (verifiedIdRequestResult.isSuccess) {
                    val verifiedIdRequest = verifiedIdRequestResult.getOrNull()
                    binding.initiateIssuance.isEnabled = false
                    if (verifiedIdRequest is VerifiedIdPresentationRequest) {
                        binding.textview.text =
                            "Presentation request from ${verifiedIdRequest.requesterStyle.name}"
                        binding.completeIssuance.text = "Complete Presentation"
                    } else if (verifiedIdRequest is VerifiedIdIssuanceRequest) {
                        binding.textview.text =
                            "Issuance request from ${verifiedIdRequest.requesterStyle.name}"
                    }
                    binding.completeIssuance.isEnabled = true
                    binding.claimsList.visibility = View.GONE
                }
            }
        }
    }

    private fun configureViewsForVerifiedIdClaims(verifiableCredential: VerifiableCredential) {
        val claims = verifiableCredential.getClaims()
        claims.add(VerifiedIdClaim("Issued On", verifiableCredential.issuedOn))
        verifiableCredential.expiresOn?.let { claims.add(VerifiedIdClaim("Expiry", it)) }
        claims.add(VerifiedIdClaim("Id", verifiableCredential.id))
        val adapter =
            VerifiableCredentialAdapter(requireContext(), claims)
        binding.claimsList.layoutManager = LinearLayoutManager(context)
        binding.claimsList.isNestedScrollingEnabled = false
        binding.claimsList.adapter = adapter
    }

    private fun completePresentation() {
        runBlocking {
            viewModel.completePresentation()
            binding.textview.text = "Presentation Complete!!"
            binding.completeIssuance.isEnabled = false
            binding.claimsList.visibility = View.VISIBLE
        }
    }
}