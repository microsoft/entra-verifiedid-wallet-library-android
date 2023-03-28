package com.microsoft.walletlibrarydemo.feature.issuance.selfattestedflow.presentationlogic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.walletlibrary.verifiedid.VerifiedIdClaim
import com.microsoft.walletlibrarydemo.R
import com.microsoft.walletlibrarydemo.databinding.RequirementVerifiedclaimRowBinding

sealed class VerifiableCredentialViewHolder(view: View) : RecyclerView.ViewHolder(view)
class VerifiableCredentialHolder(val binding: RequirementVerifiedclaimRowBinding) :
    VerifiableCredentialViewHolder(binding.root)

class VerifiableCredentialAdapter(
    private val context: Context,
    private val claims: List<VerifiedIdClaim>
) : RecyclerView.Adapter<VerifiableCredentialViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VerifiableCredentialViewHolder {
        return when (viewType) {
            VerifiedIdClaim::class.java.name.hashCode() ->
                VerifiableCredentialHolder(
                    RequirementVerifiedclaimRowBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else -> throw IllegalStateException("Unknown viewType ($viewType) provided")
        }
    }

    override fun getItemCount(): Int = claims.size

    override fun getItemViewType(position: Int) = claims[position]::class.java.name.hashCode()

    override fun onBindViewHolder(holder: VerifiableCredentialViewHolder, position: Int) {
        when (holder) {
            is VerifiableCredentialHolder -> setupSelfAttestedRow(
                holder,
                claims[position]
            )
        }
    }

    private fun setupSelfAttestedRow(
        holder: VerifiableCredentialHolder,
        claim: VerifiedIdClaim
    ) {
        holder.binding.claimTitle.text = claim.id
        holder.binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        holder.binding.claimValue.setTextColor(ContextCompat.getColor(context, R.color.gray))
        holder.binding.claimValue.text = claim.value.toString()
    }
}