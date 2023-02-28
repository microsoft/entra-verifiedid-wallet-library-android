package com.microsoft.walletlibrarydemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.walletlibrarydemo.databinding.ActivityMainBinding
import com.microsoft.walletlibrarydemo.feature.issuance.viewlogic.SelfAttestedFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureViews()
    }

    private fun configureViews() {
        binding.selfAttested.setOnClickListener { navigateToSelfAttestedFlow() }
        binding.idTokenHint.setOnClickListener { navigateToIdTokenHintFlow() }
    }

    private fun navigateToSelfAttestedFlow() {
        binding.selfAttested.visibility = View.GONE
        binding.idTokenHint.visibility = View.GONE
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.apply {
            replace(R.id.fragment, SelfAttestedFragment())
            commitAllowingStateLoss()
        }
    }

    private fun navigateToIdTokenHintFlow() {

    }
}