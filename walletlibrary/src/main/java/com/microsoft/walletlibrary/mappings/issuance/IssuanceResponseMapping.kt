package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.models.pin.IssuancePin
import com.microsoft.walletlibrary.requests.requirements.*

internal fun IssuanceResponse.addRequirements(requirement: Requirement) {
    when (requirement) {
        is SelfAttestedClaimRequirement -> addSelfAttestedClaimRequirement(requirement)
        is IdTokenRequirement -> addIdTokenRequirement(requirement)
        is AccessTokenRequirement -> addAccessTokenRequirement(requirement)
        is PinRequirement -> addPinRequirement(requirement)
        is GroupRequirement -> addGroupRequirement(requirement)
    }
}

private fun IssuanceResponse.addSelfAttestedClaimRequirement(selfAttestedClaimRequirement: SelfAttestedClaimRequirement) {
    selfAttestedClaimRequirement.value?.let {
        requestedSelfAttestedClaimMap[selfAttestedClaimRequirement.claim] =
            it
    }
}

private fun IssuanceResponse.addIdTokenRequirement(idTokenRequirement: IdTokenRequirement) {
    idTokenRequirement.idToken?.let {
        requestedSelfAttestedClaimMap[idTokenRequirement.configuration] =
            it
    }
}

private fun IssuanceResponse.addAccessTokenRequirement(accessTokenRequirement: AccessTokenRequirement) {
    accessTokenRequirement.accessToken?.let {
        requestedSelfAttestedClaimMap[accessTokenRequirement.configuration] =
            it
    }
}

private fun IssuanceResponse.addPinRequirement(pinRequirement: PinRequirement) {
    pinRequirement.pin?.let {
        issuancePin = IssuancePin(it)
    }
}

private fun IssuanceResponse.addGroupRequirement(groupRequirement: GroupRequirement) {
    val requirements = groupRequirement.requirements
    for (requirement in requirements) {
        addRequirements(requirement)
    }
}