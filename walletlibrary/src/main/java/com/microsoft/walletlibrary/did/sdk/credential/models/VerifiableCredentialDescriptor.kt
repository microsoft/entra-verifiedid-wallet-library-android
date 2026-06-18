/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.models

import com.microsoft.walletlibrary.did.sdk.util.Constants.CONTEXT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model to describe a Verifiable Credential.
 */
@Serializable
internal data class VerifiableCredentialDescriptor(
    @SerialName(CONTEXT)
    val context: List<String>,

    val type: List<String>,

    @Serializable(with = CredentialSubjectSerializer::class)
    val credentialSubject: Map<String, String>,

    @Serializable(with = CredentialStatusDescriptorSerializer::class)
    val credentialStatus: CredentialStatusDescriptor? = null,

    val revokeService: ServiceDescriptor? = null
)