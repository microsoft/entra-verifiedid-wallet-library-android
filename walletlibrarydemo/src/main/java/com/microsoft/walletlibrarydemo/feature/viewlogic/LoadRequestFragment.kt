package com.microsoft.walletlibrarydemo.feature.viewlogic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrarydemo.databinding.LoadRequestFragmentBinding
import com.microsoft.walletlibrarydemo.feature.presentationlogic.ClickListener
import com.microsoft.walletlibrarydemo.feature.presentationlogic.SampleViewModel
import com.microsoft.walletlibrarydemo.feature.presentationlogic.VerifiedIdsAdapter
import com.microsoft.walletlibrarydemo.feature.presentationlogic.ViewModelFactory
import com.microsoft.walletlibrarydemo.feature.presentationlogic.model.VerifiedIdDisplay
import kotlinx.coroutines.runBlocking

class LoadRequestFragment : Fragment(), ClickListener {
    private var _binding: LoadRequestFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SampleViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoadRequestFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureViews()
    }

    private fun configureViews() {
        binding.createRequest.setOnClickListener { initiateIssuance() }
        runBlocking {
            val verifiedIds = viewModel.getVerifiedIds().map { VerifiedIdDisplay(it)  }
            if (verifiedIds.isNotEmpty()) {
                binding.issuedVerifiedIds.visibility = View.VISIBLE
                val adapter = VerifiedIdsAdapter(this@LoadRequestFragment, verifiedIds, null)
                binding.verifiedIds.layoutManager = LinearLayoutManager(context)
                binding.verifiedIds.isNestedScrollingEnabled = false
                binding.verifiedIds.adapter = adapter
            } else
                binding.issuedVerifiedIds.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initiateIssuance() {
        runBlocking {
            if (binding.request.text.toString().isNotEmpty()) {
                findNavController().navigate(
                    LoadRequestFragmentDirections.actionLoadRequestFragmentToRequirementsFragment(
                        binding.request.text.toString()
                    )
                )
            } else
                binding.request.error = "You need to provide a URL"
        }
    }

    override fun listMatchingVerifiedIds(requirement: VerifiedIdRequirement) {}

    override fun fulfillVerifiedIdRequirement(
        verifiedId: VerifiedId,
        requirement: VerifiedIdRequirement
    ) {}
}