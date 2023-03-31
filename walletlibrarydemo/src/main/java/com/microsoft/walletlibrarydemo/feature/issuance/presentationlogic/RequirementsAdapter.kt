package com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrarydemo.R
import com.microsoft.walletlibrarydemo.databinding.RequirementIdtokenRowBinding
import com.microsoft.walletlibrarydemo.databinding.RequirementTextRowBinding

sealed class RequirementViewHolder(view: View): RecyclerView.ViewHolder(view)
class SelfAttestedHolder(val binding: RequirementTextRowBinding): RequirementViewHolder(binding.root)
class PinHolder(val binding: RequirementTextRowBinding): RequirementViewHolder(binding.root)
class VerifiedIdHolder(val binding: RequirementIdtokenRowBinding): RequirementViewHolder(binding.root)

class RequirementsAdapter(
    private val context: Context,
    private val requirements: List<Requirement>
): RecyclerView.Adapter<RequirementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequirementViewHolder {
        return when (viewType) {
            SelfAttestedClaimRequirement::class.java.name.hashCode() ->
                SelfAttestedHolder(
                    RequirementTextRowBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            PinRequirement::class.java.name.hashCode() ->
                PinHolder(
                    RequirementTextRowBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            VerifiedIdRequirement::class.java.name.hashCode() ->
                VerifiedIdHolder(
                    RequirementIdtokenRowBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else -> throw IllegalStateException("Do not support ($viewType)")
        }
    }

    override fun getItemCount(): Int = requirements.size

    override fun getItemViewType(position: Int) = requirements[position]::class.java.name.hashCode()

    override fun onBindViewHolder(holder: RequirementViewHolder, position: Int) {
        when (holder) {
            is SelfAttestedHolder -> setupSelfAttestedRow(
                holder,
                requirements[position] as SelfAttestedClaimRequirement
            )
            is PinHolder -> setupPinHolder(
                holder,
                requirements[position] as PinRequirement
            )
            is VerifiedIdHolder -> setupVerifiedIdRow(
                holder,
                requirements[position] as VerifiedIdRequirement
            )
        }
    }

    private fun setupSelfAttestedRow(
        holder: SelfAttestedHolder,
        requirement: SelfAttestedClaimRequirement
    ) {
        holder.binding.title.text = requirement.claim
        holder.binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        holder.binding.claimValue.setTextColor(ContextCompat.getColor(context, R.color.gray))
        holder.binding.claimValue.apply {
            doAfterTextChanged {
                requirement.fulfill(holder.binding.claimValue.text.toString())
            }
        }
    }

    private fun setupVerifiedIdRow(
        holder: VerifiedIdHolder,
        requirement: VerifiedIdRequirement
    ) {
        holder.binding.title.text = "Requesting ${requirement.types.first()}"
        holder.binding.subtitle.text = "Purpose: ${requirement.purpose}"
    }

    private fun setupPinHolder(
        holder: PinHolder,
        requirement: PinRequirement
    ) {
        holder.binding.title.text = "Pin"
        holder.binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        holder.binding.claimValue.setTextColor(ContextCompat.getColor(context, R.color.gray))
        holder.binding.claimValue.apply {
            doAfterTextChanged {
                requirement.fulfill(holder.binding.claimValue.text.toString())
            }
        }
    }
}