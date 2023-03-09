/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.models.pin.IssuancePin
import com.microsoft.walletlibrary.requests.requirements.*

/**
 * Fills the attestation requirement in IssuanceResponse object with Requirements object in library.
 */
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
    selfAttestedClaimRequirement.validate()
    selfAttestedClaimRequirement.value?.let {
        requestedSelfAttestedClaimMap[selfAttestedClaimRequirement.claim] =
            it
    }
}

private fun IssuanceResponse.addIdTokenRequirement(idTokenRequirement: IdTokenRequirement) {
    idTokenRequirement.validate()
    idTokenRequirement.idToken?.let {
        requestedIdTokenMap[idTokenRequirement.configuration] =
            it
    }
}

private fun IssuanceResponse.addAccessTokenRequirement(accessTokenRequirement: AccessTokenRequirement) {
    accessTokenRequirement.validate()
    accessTokenRequirement.accessToken?.let {
        requestedAccessTokenMap[accessTokenRequirement.configuration] =
            it
    }
}

private fun IssuanceResponse.addPinRequirement(pinRequirement: PinRequirement) {
    pinRequirement.validate()
    pinRequirement.pin?.let {
        issuancePin = IssuancePin(it)
        issuancePin?.pinSalt = pinRequirement.salt
    }
}

private fun IssuanceResponse.addGroupRequirement(groupRequirement: GroupRequirement) {
    groupRequirement.validate()
    val requirements = groupRequirement.requirements
    for (requirement in requirements) {
        addRequirements(requirement)
    }
}