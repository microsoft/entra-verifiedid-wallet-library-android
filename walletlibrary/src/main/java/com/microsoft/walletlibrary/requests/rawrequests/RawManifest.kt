/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.walletlibrary.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.mappings.issuance.getRequesterStyle
import com.microsoft.walletlibrary.mappings.issuance.toRequirement
import com.microsoft.walletlibrary.mappings.issuance.toVerifiedIdStyle
import com.microsoft.walletlibrary.mappings.toRootOfTrust
import com.microsoft.walletlibrary.requests.IssuanceRequestContent
import kotlinx.serialization.Serializable

/**
 * Represents the raw issuance request from VC SDK.
 */

@Serializable
internal class RawManifest(
    override val rawRequest: IssuanceRequest,
    override val requestType: RequestType = RequestType.ISSUANCE
): RawRequest {
    internal fun mapToIssuanceRequestContent(): IssuanceRequestContent {
        return IssuanceRequestContent(
            rawRequest.getRequesterStyle(),
            rawRequest.getAttestations().toRequirement(),
            rawRequest.linkedDomainResult.toRootOfTrust(),
            rawRequest.contract.display.toVerifiedIdStyle()
        )
    }
}