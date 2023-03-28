package com.microsoft.walletlibrarydemo.feature.issuance.viewlogic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedIdClaim
import com.microsoft.walletlibrarydemo.databinding.RequirementsFragmentBinding
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.RequirementsAdapter
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.SampleViewModel
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.VerifiedIdAdapter
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.ViewModelFactory
import kotlinx.coroutines.runBlocking

class RequirementsFragment : Fragment() {
    private var _binding: RequirementsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SampleViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private val args by navArgs<RequirementsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RequirementsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureViews()
    }

    private fun configureViews() {
        binding.requestCompletion.setOnClickListener { completeRequest() }
        binding.requirementsList.layoutManager = LinearLayoutManager(context)
        binding.requirementsList.isNestedScrollingEnabled = false
        runBlocking {
            viewModel.initiateRequest(args.requestUrl)
            val requestResult = viewModel.verifiedIdRequestResult
            requestResult?.let {
                if (requestResult.isSuccess) {
                    val request = requestResult.getOrNull()
                    request?.let {
                        binding.requestTitle.text =
                            if (it is VerifiedIdIssuanceRequest) "Issuance Request" else "Presentation Request"
                    }
                    val requirement = request?.requirement
                    requirement?.let {
                        val requirementList =
                            if (requirement !is GroupRequirement) listOf(requirement) else requirement.requirements
                        val adapter = RequirementsAdapter(
                            requireContext(),
                            requirementList
                        )
                        binding.requirementsList.adapter = adapter
                    }
                }
            }
        }
    }

    private fun completeRequest() {
        runBlocking {
            if (viewModel.verifiedIdRequestResult?.isSuccess == true && viewModel.verifiedIdRequestResult?.getOrNull() != null) {
                val request = viewModel.verifiedIdRequestResult?.getOrNull()
                if (request is VerifiedIdIssuanceRequest) {
                    viewModel.completeIssuance()
                    binding.requirementsList.visibility = View.GONE
                    binding.verifiedidClaims.visibility = View.VISIBLE
                    if (viewModel.verifiedIdResult?.isSuccess == true) {
                        if (viewModel.verifiedIdResult?.getOrNull() != null)
                            configureVerifiedIdView()
                    } else
                        binding.requestTitle.text = "Issuance Failed ${viewModel.verifiedIdResult?.exceptionOrNull()}"
                } else if (request is VerifiedIdPresentationRequest) {
                    viewModel.completePresentation()
                    binding.requirementsList.visibility = View.GONE
                    binding.verifiedidClaims.visibility = View.GONE
                    if (viewModel.verifiedIdResult?.isSuccess == true) {
                        if (viewModel.verifiedIdResult?.getOrNull() != null)
                            binding.requestTitle.text = "Presentation Complete!!"
                    } else
                        binding.requestTitle.text = "Presentation Failed ${viewModel.verifiedIdResult?.exceptionOrNull()}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configureVerifiedIdView() {
        var verifiedId: VerifiedId? = null
        viewModel.verifiedIdResult?.let {
            if (it.isSuccess)
                verifiedId = it.getOrNull() as VerifiedId
        }
        verifiedId?.let {
            binding.requestTitle.text = "Verified Id"
            val verifiableCredential = verifiedId as VerifiableCredential
            val claims = verifiableCredential.getClaims()
            claims.add(VerifiedIdClaim("Issued On", verifiableCredential.issuedOn))
            verifiableCredential.expiresOn?.let { claims.add(VerifiedIdClaim("Expiry", it)) }
            claims.add(VerifiedIdClaim("Id", verifiableCredential.id))
            val adapter = VerifiedIdAdapter(claims)
            binding.verifiedidClaims.layoutManager = LinearLayoutManager(context)
            binding.verifiedidClaims.isNestedScrollingEnabled = false
            binding.verifiedidClaims.adapter = adapter
            binding.requestCompletion.text = "Complete Presentation"
            binding.requestCompletion.setOnClickListener { navigateToPresentation() }
        }
    }

    private fun navigateToPresentation() {
        findNavController().navigate(RequirementsFragmentDirections.actionRequirementsFragmentToLoadRequestFragment())
    }
}