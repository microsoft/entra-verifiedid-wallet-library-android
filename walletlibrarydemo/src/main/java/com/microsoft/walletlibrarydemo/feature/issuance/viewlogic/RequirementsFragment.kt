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
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrarydemo.databinding.RequirementsFragmentBinding
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.RequirementsAdapter
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.SampleViewModel
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
            viewModel.initiateIssuance(args.requestUrl)
            val request = viewModel.verifiedIdRequest
            request?.let { binding.requestTitle.text = if (it is VerifiedIdIssuanceRequest) "Issuance Request" else "Presentation Request" }
            val requirement = request?.requirement
            requirement?.let {
                val requirementList = if (requirement !is GroupRequirement) listOf(requirement) else requirement.requirements
                val adapter = RequirementsAdapter(
                    requireContext(),
                    requirementList
                )
                binding.requirementsList.adapter = adapter
            }
        }
    }

    private fun completeRequest() {
        runBlocking {
            viewModel.completeIssuance()
            findNavController().navigate(RequirementsFragmentDirections.actionRequirementsFragmentToVerifiedIdViewFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}