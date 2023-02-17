/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedIdClaim
import com.microsoft.walletlibrary.verifiedid.VerifiedIdType

/**
 * Maps VerifiableCredential object in SDK to VerifiedId object in library.
 */
internal fun VerifiableCredential.toVerifiedId(): VerifiedId {
    val map = this.contents.vc.credentialSubject.mapValues { it.value }
    val claims = mutableListOf<VerifiedIdClaim>()
    for (entry in map) {
        claims.add(VerifiedIdClaim(entry.key, entry.value))
    }
    return VerifiedId(this.jti, VerifiedIdType.VERIFIABLE_CREDENTIAL, claims, this.contents.iat, this.contents.exp, this.raw)
}