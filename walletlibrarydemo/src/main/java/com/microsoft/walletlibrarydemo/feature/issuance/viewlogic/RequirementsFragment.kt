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
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedIdClaim
import com.microsoft.walletlibrarydemo.databinding.RequirementsFragmentBinding
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.*
import kotlinx.coroutines.runBlocking

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
        runBlocking {
            viewModel.initiateRequest(args.requestUrl)
            val requestResult = viewModel.verifiedIdRequestResult
            requestResult?.let {
                if (requestResult.isSuccess) {
                    val request = requestResult.getOrNull()
                    request?.let {
                        if (it is VerifiedIdIssuanceRequest) {
                            binding.requestTitle.text = "Issuance Request"
                        } else {
                            binding.requestTitle.text = "Presentation Request"
                            binding.requestCompletion.visibility = View.GONE
                        }
                    }
                    val requirement = request?.requirement
                    requirement?.let {
                        val requirementList =
                            if (requirement !is GroupRequirement) listOf(requirement) else requirement.requirements
                        val adapter = RequirementsAdapter(this@RequirementsFragment, requireContext(), requirementList)
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
                    binding.verifiedIdClaims.visibility = View.VISIBLE
                    if (viewModel.verifiedIdResult?.isSuccess == true) {
                        if (viewModel.verifiedIdResult?.getOrNull() != null)
                            configureVerifiedIdView()
                    } else
                        binding.requestTitle.text =
                            "Issuance Failed ${viewModel.verifiedIdResult?.exceptionOrNull()}"
                } else if (request is VerifiedIdPresentationRequest) {
                    viewModel.completePresentation()
                    binding.requirementsList.visibility = View.GONE
                    binding.verifiedIdClaims.visibility = View.GONE
                    if (viewModel.verifiedIdResult?.isSuccess == true) {
                        if (viewModel.verifiedIdResult?.getOrNull() != null)
                            binding.requestTitle.text = "Presentation Complete!!"
                    } else
                        binding.requestTitle.text =
                            "Presentation Failed ${viewModel.verifiedIdResult?.exceptionOrNull()}"
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
            val verifiableCredential = verifiedId as VerifiableCredential
            binding.requestTitle.text = verifiableCredential.types.last()
            val claims = verifiableCredential.getClaims()
            claims.add(VerifiedIdClaim("Issued On", verifiableCredential.issuedOn))
            verifiableCredential.expiresOn?.let { claims.add(VerifiedIdClaim("Expiry", it)) }
            claims.add(VerifiedIdClaim("Id", verifiableCredential.id))
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

    override fun navigateToVerifiedId(requirement: VerifiedIdRequirement) {
        runBlocking {
            binding.requirementsList.visibility = View.GONE
            binding.verifiedIdClaims.visibility = View.GONE
            binding.verifiedIds.visibility = View.VISIBLE
            val verifiedIds = viewModel.getVerifiedIds().filter { it.verifiableCredential.types.contains(requirement.types.last()) }
            val adapter = VerifiedIdsAdapter(this@RequirementsFragment, verifiedIds as ArrayList<com.microsoft.walletlibrarydemo.db.entities.VerifiedId>, requirement)
            binding.verifiedIds.layoutManager = LinearLayoutManager(context)
            binding.verifiedIds.isNestedScrollingEnabled = false
            binding.verifiedIds.adapter = adapter
        }
    }

    override fun fulfillVerifiedIdRequirement(verifiedId: com.microsoft.walletlibrarydemo.db.entities.VerifiedId, requirement: VerifiedIdRequirement) {
        requirement.fulfill(verifiedId.verifiableCredential)
        binding.requestCompletion.visibility = View.VISIBLE
    }
}