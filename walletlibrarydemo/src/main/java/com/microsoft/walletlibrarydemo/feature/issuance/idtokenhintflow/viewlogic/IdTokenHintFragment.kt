package com.microsoft.walletlibrarydemo.feature.issuance.idtokenhintflow.viewlogic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.util.WalletLibraryException
import com.microsoft.walletlibrarydemo.databinding.IdTokenHintFragmentBinding
import com.microsoft.walletlibrarydemo.feature.issuance.idtokenhintflow.presentationlogic.IdTokenFlowViewModel
import kotlinx.coroutines.runBlocking

class IdTokenHintFragment : Fragment() {
    private var _binding: IdTokenHintFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: IdTokenFlowViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
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
        viewModel = IdTokenFlowViewModel(requireContext())
        runBlocking {
            viewModel.initiateIssuance()
            val verifiedIdRequestResult = viewModel.verifiedIdRequestResult
            verifiedIdRequestResult?.let {
                if (verifiedIdRequestResult.isSuccess) {
                    val verifiedIdRequest = verifiedIdRequestResult.getOrNull()
                    binding.initiateIssuance.isEnabled = false
                    if (verifiedIdRequest is VerifiedIdPresentationRequest) binding.textview.text =
                        "Presentation request from ${verifiedIdRequest.requesterStyle.requester}"
                    else if (verifiedIdRequest is VerifiedIdIssuanceRequest) {
                        binding.textview.text =
                            "Issuance request from ${verifiedIdRequest.requesterStyle.requester}"
                        when (verifiedIdRequest.requirement) {
                            is GroupRequirement -> configureIdTokenHintWithPinView(verifiedIdRequest)
                            is IdTokenRequirement -> configureIdTokenHintWithoutPinView(
                                verifiedIdRequest
                            )
                        }
                        binding.completeIssuance.isEnabled = true
                    }
                } else binding.textview.text = verifiedIdRequestResult.exceptionOrNull().toString()
            }
        }
    }

    private fun configureIdTokenHintWithPinView(verifiedIdRequest: VerifiedIdRequest<*>) {
        val requirements = (verifiedIdRequest.requirement as GroupRequirement).requirements
        for (requirement in requirements) {
            if (requirement is PinRequirement) {
                binding.pin.visibility = View.VISIBLE
            } else if (requirement is IdTokenRequirement) {
                configureIdTokenHintView(requirement)
            }
        }
    }

    private fun configureIdTokenHintWithoutPinView(verifiedIdRequest: VerifiedIdRequest<*>) {
        configureIdTokenHintView(verifiedIdRequest.requirement as IdTokenRequirement)
    }

    private fun configureIdTokenHintView(requirement: IdTokenRequirement) {
        try {
            requirement.validate()
            binding.idTokenHint.visibility = View.VISIBLE
        } catch (_: WalletLibraryException) {
        }
    }

    private fun completeIssuance() {
        if (binding.pin.text.toString().isNotEmpty()) viewModel.pin = binding.pin.text.toString()
        runBlocking {
            viewModel.completeIssuance()
            configureIssuanceCompletionView()
        }
    }

    private fun configureIssuanceCompletionView() {
        binding.idTokenHint.visibility = View.GONE
        binding.pin.visibility = View.GONE
        binding.completeIssuance.isEnabled = false
        val response = viewModel.verifiedIdResult
        binding.textview.text = response?.let {
            if (response.isSuccess) response.getOrDefault("").toString()
            else response.exceptionOrNull().toString()
        } ?: "Issuance completion failed"
    }
}