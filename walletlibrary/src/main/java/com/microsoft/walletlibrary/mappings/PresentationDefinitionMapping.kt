package com.microsoft.walletlibrary.mappings

import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.MissingInputDescriptorException

fun PresentationDefinition.toRequirement(): Requirement {
    if(this.credentialPresentationInputDescriptors.isEmpty())
        throw MissingInputDescriptorException("There is no credential input descriptor in presentation definition.")
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