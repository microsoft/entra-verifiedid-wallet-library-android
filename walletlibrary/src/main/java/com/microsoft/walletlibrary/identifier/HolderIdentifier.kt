/*---------------------------------------------------------------------------------------------
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License. See License.txt in the project root for license information.
*--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.identifier

/**
 * Data model for an identifier that describes the holder of a Verified ID.
 */
interface HolderIdentifier {
    // The unique identifier for the Verified ID.
    val id: String

    // The algorithm used for cryptographic operations like signing (eg. ES256 as defined in https://datatracker.ietf.org/doc/html/rfc7518#section-3.1).
    val algorithm: String

    // The method of the identifier (eg. did:jwk).
    val method: String

    // The key reference used for cryptographic operations like signing.
    val keyReference: String

    /** Sign the supplied data using the key reference and algorithm specified.
     * @param data The data to sign.
     * @return The signed data.
     */
    fun sign(data: String): String
}