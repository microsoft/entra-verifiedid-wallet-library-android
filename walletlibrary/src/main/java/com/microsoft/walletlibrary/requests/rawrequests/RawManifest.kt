/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.mappings.issuance.getRequesterStyle
import com.microsoft.walletlibrary.mappings.issuance.toRequirement
import com.microsoft.walletlibrary.mappings.toRootOfTrust
import com.microsoft.walletlibrary.requests.VerifiedIdRequestContent

/**
 * Represents the raw issuance request from VC SDK.
 */
class RawManifest(
    override val rawRequest: IssuanceRequest,
    override val requestType: RequestType = RequestType.ISSUANCE
): RawRequest {
    internal fun mapToRequestContent(): VerifiedIdRequestContent {
        return VerifiedIdRequestContent(
            rawRequest.getRequesterStyle(),
            rawRequest.getAttestations().toRequirement(),
            rawRequest.linkedDomainResult.toRootOfTrust(),
            null
        )
    }
}