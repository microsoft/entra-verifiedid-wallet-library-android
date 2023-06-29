/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.presentation

import android.net.Uri
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.Fields
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.constraints.ClaimRegexConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraintOperator
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.VerifiedIdConstraint
import com.microsoft.walletlibrary.util.MissingVerifiedIdTypeException

/**
 * Maps CredentialPresentationInputDescriptor object of presentation in SDK to VerifiedIdRequirement object in library.
 */
internal fun CredentialPresentationInputDescriptor.toVerifiedIdRequirement(): VerifiedIdRequirement {
    if (this.schemas.isEmpty())
        throw MissingVerifiedIdTypeException("There is no VerifiedId Type in credential input descriptor.")
    return VerifiedIdRequirement(
        this.id,
        this.schemas.map { it.uri },
        toConstraint(),
        encrypted = false,
        required = true,
        this.purpose,
        this.issuanceMetadataList.map { VerifiedIdRequestURL(Uri.parse(it.issuerContract)) }
    )
}

internal fun CredentialPresentationInputDescriptor.toConstraint(): VerifiedIdConstraint? {
    val vcTypeConstraint =
        if (schemas.isNotEmpty()) toVcTypeConstraint(schemas.map { it.uri }) else null
    val claimConstraint = constraints?.fields?.let { toClaimRegexConstraint(it) }
    if (claimConstraint == null && vcTypeConstraint == null) return null
    if (vcTypeConstraint != null) {
        claimConstraint?.let {
            return GroupConstraint(
                listOf(vcTypeConstraint, claimConstraint),
                GroupConstraintOperator.ALL
            )
        } ?: return vcTypeConstraint
    }
    return claimConstraint
}

internal fun toClaimRegexConstraint(fields: List<Fields>): VerifiedIdConstraint? {
    if (fields.isEmpty()) return null
    if (fields.size == 1) return ClaimRegexConstraint(
        fields.first().path,
        fields.first().filter?.pattern ?: ""
    )
    val claimRegexConstraints = mutableListOf<ClaimRegexConstraint>()
    fields.forEach {
        claimRegexConstraints.add(
            ClaimRegexConstraint(
                it.path,
                it.filter?.pattern ?: ""
            )
        )
    }
    return GroupConstraint(claimRegexConstraints, GroupConstraintOperator.ALL)
}

internal fun toVcTypeConstraint(vcTypes: List<String>): VerifiedIdConstraint? {
    if (vcTypes.isEmpty()) return null
    if (vcTypes.size == 1) return VcTypeConstraint(vcTypes.first())
    val vcTypeConstraints = mutableListOf<VcTypeConstraint>()
    vcTypes.forEach { vcTypeConstraints.add(VcTypeConstraint(it)) }
    return GroupConstraint(vcTypeConstraints, GroupConstraintOperator.ANY)
}