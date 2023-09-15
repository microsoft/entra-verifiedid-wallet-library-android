package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.walletlibrary.did.sdk.credential.service.PresentationResponse
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.util.IdInVerifiedIdRequirementDoesNotMatchRequestException
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.UnSupportedRequirementException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
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
    if (verifiedIdRequirement.verifiedId == null)
        throw RequirementNotMetException(
            "Verified ID has not been set.",
            VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value
        )
    val matchingInputDescriptors =
        request.getPresentationDefinitions().filter {
            presentationDefinition ->
            // Limit to only this response' presentationDefinition
            presentationDefinition.id == this.requestedVcPresentationDefinitionId
        }.map {
            presentationDefinition ->
            // Filter to only matching inputDescriptor(s)
            presentationDefinition.credentialPresentationInputDescriptors
                .filter {
                    inputDescriptor ->
                    inputDescriptor.id == verifiedIdRequirement.id
                }
        }.reduce {
            allInputDescriptors, inputDescriptor ->
            // Flatten the inputDescriptors into a single array
            allInputDescriptors + inputDescriptor
        }
    if (matchingInputDescriptors.isEmpty())
        throw IdInVerifiedIdRequirementDoesNotMatchRequestException("Id in VerifiedId Requirement does not match the id in request.")
    if (matchingInputDescriptors.size > 1)
        throw VerifiedIdRequirementIdConflictException("Multiple VerifiedId Requirements have the same Ids.")
    verifiedIdRequirement.validate().getOrThrow()
    requestedVcPresentationSubmissionMap[matchingInputDescriptors.first()] =
        (verifiedIdRequirement.verifiedId as VerifiableCredential).raw
}

private fun PresentationResponse.addGroupRequirement(groupRequirement: GroupRequirement) {
    groupRequirement.validate().getOrThrow()
    val requirements = groupRequirement.requirements
    for (requirement in requirements) {
        addRequirements(requirement)
    }
}