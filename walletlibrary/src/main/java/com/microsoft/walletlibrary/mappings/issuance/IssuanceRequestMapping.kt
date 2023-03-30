/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle

/**
 * Maps IssuanceRequest object from VC SDK to RequesterStyle and ManifestIssuanceRequest in library.
 */
internal fun IssuanceRequest.getRequesterStyle(): RequesterStyle {
    return VerifiedIdManifestIssuerStyle(
        entityName
    )
}
