/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.identifier.models.Identifier

/**
 * Sealed Class for Requests to the Verifiable Credential Service to do a certain action on a Verifiable Credential.
 */
internal sealed class VcServiceActionRequest(val audience: String)

internal data class RevocationRequest(
    val verifiableCredential: VerifiableCredential,
    val owner: Identifier,
    val rpList: List<String>,
    val reason: String
) : VcServiceActionRequest(verifiableCredential.contents.vc.revokeService?.id ?: "")

internal data class StatusRequest(val verifiableCredential: VerifiableCredential, val owner: Identifier) :
    VcServiceActionRequest(verifiableCredential.contents.vc.credentialStatus?.id ?: "")

