/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.credential.models

import kotlinx.serialization.Serializable

/**
 * Data model to describe a service provided in Verifiable Credential.
 * (e.g. status service, revocation service)
 */
@Serializable
internal data class ServiceDescriptor(
    val id: String,
    val type: String
)