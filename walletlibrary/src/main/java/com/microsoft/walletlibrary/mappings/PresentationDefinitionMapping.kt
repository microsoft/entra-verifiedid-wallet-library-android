package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.Requirement

fun PresentationDefinition.toGroupRequirement(): Requirement {
    return if (this.credentialPresentationInputDescriptors.size == 1)
        this.credentialPresentationInputDescriptors.first().toVerifiedIdRequirement()
    else {
        val groupRequirement = GroupRequirement()
        groupRequirement.required = true
        groupRequirement.requirements.addAll(this.credentialPresentationInputDescriptors.map { it.toVerifiedIdRequirement() })
        groupRequirement.requirementOperator = GroupRequirementOperator.ANY
        groupRequirement
    }
}