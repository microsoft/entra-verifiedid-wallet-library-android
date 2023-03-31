// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrarydemo.databinding.RequirementVerifiedidRowBinding
import com.microsoft.walletlibrarydemo.db.entities.VerifiedId

class VerifiedIdsAdapter(
    private val clickListener: ClickListener,
    private val verifiedIds: ArrayList<VerifiedId>,
    private val requirement: VerifiedIdRequirement?
) :
    RecyclerView.Adapter<VerifiedIdsAdapter.VerifiedIdsViewHolder>() {

    sealed class VerifiedIdsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class VerifiedIdVc(val binding: RequirementVerifiedidRowBinding) :
            VerifiedIdsViewHolder(binding.root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerifiedIdsViewHolder {
        return when (viewType) {
            VerifiedId::class.java.name.hashCode() -> VerifiedIdsViewHolder.VerifiedIdVc(
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
        holder.binding.name.text = "Title: ${vc.verifiableCredential.style.name}"
        holder.binding.type.text = "Type: ${vc.verifiableCredential.types.last()}"
        holder.binding.issuedOn.text = "Issued On: ${vc.verifiableCredential.issuedOn}"
        holder.binding.issuer.text = "Issuer: ${(vc.verifiableCredential.style as BasicVerifiedIdStyle).issuer}"
        requirement?.let { req -> holder.binding.root.setOnClickListener { fulfillVerifiedIdRequirement(vc, req) } }
    }

    private fun fulfillVerifiedIdRequirement(verifiedId: VerifiedId, requirement: VerifiedIdRequirement) {
        clickListener.fulfillVerifiedIdRequirement(verifiedId, requirement)
    }
}