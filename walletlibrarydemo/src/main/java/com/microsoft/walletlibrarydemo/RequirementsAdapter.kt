package com.microsoft.walletlibrarydemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrarydemo.databinding.RequirementSelfattestedRowBinding

sealed class RequirementViewHolder(view: View) : RecyclerView.ViewHolder(view)
class SelfAttestedHolder(val binding: RequirementSelfattestedRowBinding): RequirementViewHolder(binding.root)

class RequirementsAdapter(
    private val context: Context,
    private val requirements: List<SelfAttestedClaimRequirement>
): RecyclerView.Adapter<RequirementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequirementViewHolder {
        return when (viewType) {
            SelfAttestedClaimRequirement::class.java.name.hashCode() ->
                SelfAttestedHolder(
                    RequirementSelfattestedRowBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else -> throw IllegalStateException("Unknown viewType ($viewType) provided")
        }
    }

    override fun getItemCount(): Int = requirements.size

    override fun getItemViewType(position: Int) = requirements[position]::class.java.name.hashCode()

    override fun onBindViewHolder(holder: RequirementViewHolder, position: Int) {
        when (holder) {
            is SelfAttestedHolder -> setupSelfAttestedRow(
                holder,
                requirements[position]
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
}
