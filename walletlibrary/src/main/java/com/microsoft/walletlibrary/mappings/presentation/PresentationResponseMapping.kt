package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.util.IdInVerifiedIdRequirementDoesNotMatchRequestException
import com.microsoft.walletlibrary.util.UnSupportedRequirementException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementIdConflictException
import com.microsoft.walletlibrary.util.VerifiedIdRequirementMissingIdException
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential

/**
 * Fills the requested verifiable credentials in PresentationResponse object with Requirements object in library.
 */
internal fun PresentationResponse.addRequirements(requirement: Requirement) {
    when (requirement) {
        is GroupRequirement -> addGroupRequirement(requirement)
        is VerifiedIdRequirement -> addVerifiedIdRequirement(requirement)
        else -> throw UnSupportedRequirementException("Requirement type ${requirement::class.simpleName} is not unsupported.")
    }
}

private fun PresentationResponse.addVerifiedIdRequirement(verifiedIdRequirement: VerifiedIdRequirement) {
    if (verifiedIdRequirement.id == null)
        throw VerifiedIdRequirementMissingIdException("Id is missing in the VerifiedId Requirement.")
    val credentialPresentationInputDescriptor =
        request.getPresentationDefinition().credentialPresentationInputDescriptors.filter { it.id == verifiedIdRequirement.id }
    if (credentialPresentationInputDescriptor.isEmpty())
        throw IdInVerifiedIdRequirementDoesNotMatchRequestException("Id in VerifiedId Requirement does not match the id in request.")
    if (credentialPresentationInputDescriptor.size > 1)
        throw VerifiedIdRequirementIdConflictException("Multiple VerifiedId Requirements have the same Ids.")
    verifiedIdRequirement.validate()
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