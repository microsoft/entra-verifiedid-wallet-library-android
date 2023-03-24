package com.microsoft.walletlibrarydemo.feature.issuance.viewlogic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrarydemo.databinding.VerifiedidviewFragmentBinding
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.SampleViewModel
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.VerifiedIdAdapter
import com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic.ViewModelFactory

class VerifiedIdViewFragment : Fragment() {
    private var _binding: VerifiedidviewFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SampleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VerifiedidviewFragmentBinding.inflate(inflater, container, false)
        viewModelFactory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, viewModelFactory).get(SampleViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureViews()
    }

    private fun configureViews() {
        val verifiedId = viewModel.verifiedIdResult?.let { if (it.isSuccess) it.getOrNull() }
        verifiedId?.let {
            val adapter = VerifiedIdAdapter((verifiedId as VerifiableCredential).getClaims())
            binding.verifiedidClaims.layoutManager = LinearLayoutManager(context)
            binding.verifiedidClaims.isNestedScrollingEnabled = false
            binding.verifiedidClaims.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}