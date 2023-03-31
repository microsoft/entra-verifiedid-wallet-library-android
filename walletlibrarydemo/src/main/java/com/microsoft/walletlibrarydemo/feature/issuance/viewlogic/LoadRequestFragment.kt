package com.microsoft.walletlibrarydemo.feature.issuance.viewlogic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.microsoft.walletlibrarydemo.databinding.LoadRequestFragmentBinding
import kotlinx.coroutines.runBlocking

class LoadRequestFragment : Fragment() {
    private var _binding: LoadRequestFragmentBinding? = null
    private val binding get() = _binding!!

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
}