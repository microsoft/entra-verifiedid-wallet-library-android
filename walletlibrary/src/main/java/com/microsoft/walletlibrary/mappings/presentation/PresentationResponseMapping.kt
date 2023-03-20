package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.util.VerifiedIdRequirementIdConflictException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementMissingIdException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementNotFulfilledException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential

internal fun PresentationResponse.addRequirements(requirement: Requirement) {
    when (requirement) {
        is GroupRequirement -> addGroupRequirement(requirement)
        is VerifiedIdRequirement -> addVerifiedIdRequirement(requirement)
    }
}

private fun PresentationResponse.addVerifiedIdRequirement(verifiedIdRequirement: VerifiedIdRequirement) {
    if (verifiedIdRequirement.id == null)
        throw VerifiedIdRequirementMissingIdException("Id is missing in the VerifiedId Requirement.")
    if (verifiedIdRequirement.verifiedId == null)
        throw VerifiedIdRequirementNotFulfilledException("Verified Id is not selected to fulfill the requirement.")
    val credentialPresentationInputDescriptor = request.getPresentationDefinition().credentialPresentationInputDescriptors.filter { it.id == verifiedIdRequirement.id }
    if (credentialPresentationInputDescriptor.size > 1)
        throw VerifiedIdRequirementIdConflictException("Multiple VerifiedId Requirements have the same Ids.")
    requestedVcPresentationSubmissionMap[credentialPresentationInputDescriptor.first()] =
        (verifiedIdRequirement.verifiedId as VerifiableCredential).raw
}

private fun PresentationResponse.addGroupRequirement(groupRequirement: GroupRequirement) {
    groupRequirement.validate()
    val requirements = groupRequirement.requirements
    for (requirement in requirements) {
        addRequirements(requirement)
    }
}