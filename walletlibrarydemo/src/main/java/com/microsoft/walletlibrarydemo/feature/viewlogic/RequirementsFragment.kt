package com.microsoft.walletlibrarydemo.feature.viewlogic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedIdClaim
import com.microsoft.walletlibrarydemo.databinding.RequirementsFragmentBinding
import com.microsoft.walletlibrarydemo.feature.presentationlogic.*
import kotlinx.coroutines.launch

class RequirementsFragment : Fragment(), ClickListener {
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
        binding.home.setOnClickListener { goBackHome() }
        binding.requirementsList.layoutManager = LinearLayoutManager(context)
        binding.requirementsList.isNestedScrollingEnabled = false
        lifecycleScope.launch {
            // Creates a VerifiedIdRequest using the input provided which initiates the flow whether it is an issuance or presentation.
            viewModel.initiateRequest(args.requestUrl)
            binding.progress.visibility = View.GONE
            if (viewModel.state == SampleViewModel.State.CREATE_REQUEST_SUCCESS) {
                val verifiedIdRequest = viewModel.verifiedIdRequest
                verifiedIdRequest?.let { loadRequirements(it) }
            } else
                showError("Failed: ${viewModel.state.value}")
        }
    }

    private fun showError(error: String) {
        binding.errorMessage.text = error
        binding.requestCompletion.isEnabled = false
    }

    private fun loadRequirements(request: VerifiedIdRequest<*>) {
        request.let {
            if (it is VerifiedIdIssuanceRequest) {
                binding.requestTitle.text = "Issuance Request"
            } else {
                binding.requestTitle.text = "Presentation Request"
                binding.requestCompletion.visibility = View.GONE
            }
        }
        val requirement = request.requirement
        configureRequirement(requirement)
    }

    private fun configureRequirement(requirement: Requirement) {
        // If request has more than one requirement, they are listed in a GroupRequirement.
        val requirementList =
            if (requirement !is GroupRequirement) listOf(requirement) else requirement.requirements
        // Get the list of requirements and configure them in the UI.
        val adapter =
            RequirementsAdapter(this@RequirementsFragment, requireContext(), requirementList)
        binding.requirementsList.adapter = adapter
    }

    // Completes the request after all its requirements are fulfilled.
    private fun completeRequest() {
        binding.progress.visibility = View.VISIBLE
        binding.requirementsList.visibility = View.GONE
        lifecycleScope.launch {
            viewModel.verifiedIdRequest?.let {
                val request = it
                if (request is VerifiedIdIssuanceRequest)
                    completeIssuanceRequest()
                else if (request is VerifiedIdPresentationRequest)
                    completePresentationRequest()
            }
        }
            viewModel.verifiedIdRequest ?: run { binding.errorMessage.text = "Request not loaded" }
    }

    private suspend fun completeIssuanceRequest() {
        viewModel.completeIssuance()
        binding.verifiedIdClaims.visibility = View.VISIBLE
        binding.progress.visibility = View.GONE
        if (viewModel.state == SampleViewModel.State.ISSUANCE_SUCCESS) {
            configureVerifiedIdView()
        } else if (viewModel.state == SampleViewModel.State.ERROR)
            showError("Issuance Failed: ${viewModel.state.value}")
    }

    private suspend fun completePresentationRequest() {
        viewModel.completePresentation()
        binding.verifiedIdClaims.visibility = View.GONE
        binding.progress.visibility = View.GONE
        if (viewModel.state == SampleViewModel.State.PRESENTATION_SUCCESS) {
            binding.requestTitle.text = "Presentation Complete!!"
            binding.requestCompletion.visibility = View.GONE
        } else if (viewModel.state == SampleViewModel.State.ERROR)
            showError("Presentation Failed: ${viewModel.state.value}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configureVerifiedIdView() {
        binding.requestTitle.text = "Issuance Complete!!"
        val verifiedId = viewModel.verifiedId
        verifiedId?.let {
            val claims = it.getClaims()
            claims.add(VerifiedIdClaim("Issued On", it.issuedOn))
            it.expiresOn?.let { expiry -> claims.add(VerifiedIdClaim("Expiry", expiry)) }
            claims.add(VerifiedIdClaim("Id", it.id))
            val adapter = VerifiedIdAdapter(claims)
            binding.verifiedIdClaims.layoutManager = LinearLayoutManager(context)
            binding.verifiedIdClaims.isNestedScrollingEnabled = false
            binding.verifiedIdClaims.adapter = adapter
            binding.requestCompletion.visibility = View.GONE
        }
    }

    private fun goBackHome() {
        findNavController().navigate(RequirementsFragmentDirections.actionRequirementsFragmentToLoadRequestFragment())
    }

    override fun listMatchingVerifiedIds(requirement: VerifiedIdRequirement) {
        binding.requirementsList.visibility = View.GONE
        binding.verifiedIdClaims.visibility = View.GONE
        binding.verifiedIds.visibility = View.VISIBLE
        binding.matchingIds.visibility = View.VISIBLE
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val decodedVerifiedIds = viewModel.getMatchingVerifiedIds(requirement)
            binding.progress.visibility = View.GONE
            if (decodedVerifiedIds.isNotEmpty()) {
                binding.matchingIds.text = "Matching Verified Ids:"
                val adapter = VerifiedIdsAdapter(
                    this@RequirementsFragment,
                    decodedVerifiedIds,
                    requirement
                )
                binding.verifiedIds.layoutManager = LinearLayoutManager(context)
                binding.verifiedIds.isNestedScrollingEnabled = false
                binding.verifiedIds.adapter = adapter
            }
        }
    }

    override fun fulfillVerifiedIdRequirement(
        verifiedId: VerifiedId,
        requirement: VerifiedIdRequirement
    ) {
        // The fulfill methods fulfills the requirement with the provided VerifiedId value.
        requirement.fulfill(verifiedId)
        binding.requestCompletion.visibility = View.VISIBLE
    }
}