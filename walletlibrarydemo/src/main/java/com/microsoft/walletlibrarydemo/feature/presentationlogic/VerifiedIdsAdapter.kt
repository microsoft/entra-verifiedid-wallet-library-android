package com.microsoft.walletlibrarydemo.feature.presentationlogic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrarydemo.R
import com.microsoft.walletlibrarydemo.databinding.RequirementVerifiedidRowBinding

class VerifiedIdsAdapter(
    private val clickListener: ClickListener,
    private val verifiedIds: List<VerifiedId>,
    private val requirement: VerifiedIdRequirement?
) :
    RecyclerView.Adapter<VerifiedIdsAdapter.VerifiedIdsViewHolder>() {
    private var selected = false

    sealed class VerifiedIdsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class VerifiedIdVc(val binding: RequirementVerifiedidRowBinding) :
            VerifiedIdsViewHolder(binding.root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerifiedIdsViewHolder {
        return when (viewType) {
            VerifiableCredential::class.java.name.hashCode() -> VerifiedIdsViewHolder.VerifiedIdVc(
                RequirementVerifiedidRowBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("Unknown VerifiedId Type ($viewType) provided")
        }
    }

    override fun getItemCount(): Int {
        return verifiedIds.size
    }

    override fun getItemViewType(position: Int) =
        verifiedIds[position]::class.java.name.hashCode()

    override fun onBindViewHolder(holder: VerifiedIdsViewHolder, position: Int) {
        when (holder) {
            is VerifiedIdsViewHolder.VerifiedIdVc -> configureVerifiedIdClaim(
                holder,
                verifiedIds[position]
            )
        }
    }

    private fun configureVerifiedIdClaim(
        holder: VerifiedIdsViewHolder.VerifiedIdVc,
        vc: VerifiedId
    ) {
        holder.binding.name.text = "Title: ${vc.style.name}"
        holder.binding.issuedOn.text = "Issued On: ${vc.issuedOn}"
        holder.binding.issuer.text = "Issuer: ${(vc.style as BasicVerifiedIdStyle).issuer}"
        holder.binding.expiry.text = "Expiry: ${vc.expiresOn}"
        requirement?.let { req ->
            holder.binding.root.setOnClickListener {
                if (!selected) {
                    holder.binding.name.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.checkmark,
                        0
                    )
                    fulfillVerifiedIdRequirement(vc, req)
                    selected = true
                }
            }
        }
    }

    private fun fulfillVerifiedIdRequirement(verifiedId: VerifiedId, requirement: VerifiedIdRequirement) {
        clickListener.fulfillVerifiedIdRequirement(verifiedId, requirement)
    }
}