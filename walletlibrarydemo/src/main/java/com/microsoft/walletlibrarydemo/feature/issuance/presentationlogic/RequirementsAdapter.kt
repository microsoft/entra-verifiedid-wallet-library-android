package com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.walletlibrary.requests.requirements.*
import com.microsoft.walletlibrarydemo.R
import com.microsoft.walletlibrarydemo.databinding.RequirementTextRowBinding
import com.microsoft.walletlibrarydemo.databinding.RequirementVerifiedclaimRowBinding
import com.microsoft.walletlibrarydemo.databinding.RequirementVerifiedidBinding

sealed class RequirementViewHolder(view: View): RecyclerView.ViewHolder(view)
class SelfAttestedHolder(val binding: RequirementTextRowBinding): RequirementViewHolder(binding.root)
class PinHolder(val binding: RequirementTextRowBinding): RequirementViewHolder(binding.root)
class VerifiedIdHolder(val binding: RequirementVerifiedidBinding): RequirementViewHolder(binding.root)
class IdTokenHolder(val binding: RequirementVerifiedclaimRowBinding): RequirementViewHolder(binding.root)

class RequirementsAdapter(
    private val clickListener: ClickListener,
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
                    RequirementVerifiedidBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            IdTokenRequirement::class.java.name.hashCode() ->
                IdTokenHolder(
                    RequirementVerifiedclaimRowBinding.inflate(
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
            is IdTokenHolder -> setupIdTokenRow(
                holder,
                requirements[position] as IdTokenRequirement
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
                holder.binding.title.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.checkmark,
                    0
                )
            }
        }
    }

    private fun setupIdTokenRow(
        holder: IdTokenHolder,
        requirement: IdTokenRequirement
    ) {
        holder.binding.claimTitle.text = "Sign in"
        holder.binding.claimValue.text = Uri.parse(requirement.configuration).host
        if (requirement.validate().isSuccess) {
            holder.binding.claimTitle.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.checkmark,
                0
            )
        }
    }

    private fun setupVerifiedIdRow(
        holder: VerifiedIdHolder,
        requirement: VerifiedIdRequirement
    ) {
        holder.binding.type.text = "Requesting ${requirement.types.first()}"
        holder.binding.purpose.text = "Purpose: ${requirement.purpose}"
        holder.binding.root.setOnClickListener { credentialClickListener(requirement) }
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
                holder.binding.title.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.checkmark,
                    0
                )
            }
        }
    }

    private fun credentialClickListener(requirement: VerifiedIdRequirement) {
        clickListener.navigateToVerifiedIds(requirement)
    }
}