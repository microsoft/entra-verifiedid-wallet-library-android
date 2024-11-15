/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.identifier

internal interface HolderIdentifierCreator {
    /**
     * Creates a Holder Identifier based on the provided parameters.
     * @param algorithm The algorithm to use for cryptographic operations
     * @param didMethod The method for creating the DID (eg. did:jwk)
     * @param keyId The reference to the key in the keyStore
     * @param id The DID of the Holder Identifier if there is one already created
     * @return The Holder Identifier with the provided parameters.
     */
    fun createHolderIdentifier(
        algorithm: String,
        didMethod: DidMethod,
        keyId: String? = null,
        id: String? = null
    ): HolderIdentifier
}