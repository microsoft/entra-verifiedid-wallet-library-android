package com.microsoft.walletlibrarydemo.feature.issuance.idtokenhintflow.viewlogic

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.util.WalletLibraryException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedIdClaim
import com.microsoft.walletlibrarydemo.databinding.IdTokenHintFragmentBinding
import com.microsoft.walletlibrarydemo.feature.issuance.idtokenhintflow.presentationlogic.IdTokenFlowViewModel
import com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.presentationlogic.VerifiableCredentialAdapter
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
            viewModel.initiateIssuance(Uri.parse(binding.requestUrl.text.toString()))
            val verifiedIdRequestResult = viewModel.verifiedIdRequestResult
            binding.requestUrl.text.clear()
            binding.requestUrl.visibility = View.GONE
            binding.requestUrlLabel.visibility = View.GONE
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
            switchToPresentation()
        }
    }

    private fun configureIssuanceCompletionView() {
        binding.idTokenHint.visibility = View.GONE
        binding.pin.visibility = View.GONE
        binding.completeIssuance.isEnabled = false
        val response = viewModel.verifiedIdResult
        response?.let {
            if (response.isSuccess) {
                configureViewsForVerifiedIdClaims((response.getOrDefault("") as VerifiableCredential))
            } else
                binding.textview.text = response.exceptionOrNull().toString()
        }
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
                            "Presentation request from ${verifiedIdRequest.requesterStyle.requester}"
                        binding.completeIssuance.text = "Complete Presentation"
                    } else if (verifiedIdRequest is VerifiedIdIssuanceRequest) {
                        binding.textview.text =
                            "Issuance request from ${verifiedIdRequest.requesterStyle.requester}"
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