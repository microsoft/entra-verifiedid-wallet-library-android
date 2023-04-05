package com.microsoft.walletlibrarydemo.feature.presentationlogic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.walletlibrary.verifiedid.VerifiedIdClaim
import com.microsoft.walletlibrarydemo.databinding.RequirementVerifiedclaimRowBinding

class VerifiedIdAdapter(private val verifiedIdClaims: ArrayList<VerifiedIdClaim>) :
    RecyclerView.Adapter<VerifiedIdAdapter.VerifiedIdClaimsViewHolder>() {

    sealed class VerifiedIdClaimsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class VcClaim(val binding: RequirementVerifiedclaimRowBinding) :
            VerifiedIdClaimsViewHolder(binding.root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerifiedIdClaimsViewHolder {
        return when (viewType) {
            VerifiedIdClaim::class.java.name.hashCode() -> VerifiedIdClaimsViewHolder.VcClaim(
                RequirementVerifiedclaimRowBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("Unknown VerifiedId Type ($viewType) provided")
        }
    }

    override fun getItemCount(): Int {
        return verifiedIdClaims.size
    }

    override fun getItemViewType(position: Int) =
        verifiedIdClaims[position]::class.java.name.hashCode()

    override fun onBindViewHolder(holder: VerifiedIdClaimsViewHolder, position: Int) {
        when (holder) {
            is VerifiedIdClaimsViewHolder.VcClaim -> configureVerifiedIdClaim(
                holder,
                verifiedIdClaims[position]
            )
        }
    }

    private fun configureVerifiedIdClaim(
        holder: VerifiedIdClaimsViewHolder.VcClaim,
        claim: VerifiedIdClaim
    ) {
        holder.binding.claimTitle.text = claim.id
        holder.binding.claimValue.text = claim.value.toString()
    }
}