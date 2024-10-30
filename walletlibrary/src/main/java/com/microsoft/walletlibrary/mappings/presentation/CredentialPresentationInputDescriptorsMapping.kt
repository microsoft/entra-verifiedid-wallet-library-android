/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.presentation

import android.net.Uri
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.Fields
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.PresentationExchangeVerifiedIdFormat
import com.microsoft.walletlibrary.requests.requirements.PresentationExchangeVerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraintOperator
import com.microsoft.walletlibrary.requests.requirements.constraints.VcPathRegexConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.VerifiedIdConstraint
import com.microsoft.walletlibrary.util.MalformedInputException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

/**
 * Maps CredentialPresentationInputDescriptor object of presentation in SDK to VerifiedIdRequirement object in library.
 */
internal fun CredentialPresentationInputDescriptor.toVerifiedIdRequirement(): VerifiedIdRequirement {
    if (this.schemas.isEmpty())
        throw MalformedInputException(
            "There is no Verified ID type in the request.",
            VerifiedIdExceptions.MALFORMED_INPUT_EXCEPTION.value
        )
    val verifiedIdRequirement = PresentationExchangeVerifiedIdRequirement(
        this.id,
        this.schemas.map { it.uri },
        encrypted = false,
        required = true,
        this.purpose,
        this.issuanceMetadataList.map { VerifiedIdRequestURL(Uri.parse(it.issuerContract)) },
        inputDescriptorId = this.id
    )
    val verifiedIdConstraint = toConstraint(verifiedIdRequirement)
    verifiedIdRequirement.constraint = verifiedIdConstraint
    return verifiedIdRequirement
}

internal fun CredentialPresentationInputDescriptor.toConstraint(verifiedIdRequirement: VerifiedIdRequirement): VerifiedIdConstraint {
    val vcTypeConstraint =
        if (schemas.isNotEmpty()) verifiedIdRequirement.constraint
        else throw MalformedInputException(
            "There is no Verified ID type in the request.",
            VerifiedIdExceptions.MALFORMED_INPUT_EXCEPTION.value
        )
    val vcPathRegexConstraint = constraints?.fields?.let { toVcPathRegexConstraint(it) }
    vcPathRegexConstraint?.let {
        return GroupConstraint(
            listOf(vcTypeConstraint, vcPathRegexConstraint),
            GroupConstraintOperator.ALL
        )
    } ?: return vcTypeConstraint
}

internal fun toVcPathRegexConstraint(fields: List<Fields>): VerifiedIdConstraint? {
    if (fields.isEmpty()) return null
    if (fields.size == 1) return VcPathRegexConstraint(
        fields.first().path,
        fields.first().filter?.pattern ?: ""
    )
    val vcPathRegexConstraints = mutableListOf<VcPathRegexConstraint>()
    fields.forEach {
        vcPathRegexConstraints.add(
            VcPathRegexConstraint(
                it.path,
                it.filter?.pattern ?: ""
            )
        )
    }
    return GroupConstraint(vcPathRegexConstraints, GroupConstraintOperator.ALL)
}
