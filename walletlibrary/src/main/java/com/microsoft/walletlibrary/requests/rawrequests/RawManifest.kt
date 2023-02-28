/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.IssuanceRequest

/**
 * Represents the raw issuance request from VC SDK.
 */
class RawManifest(
    override val rawRequest: IssuanceRequest,
    override val requestType: RequestType
): RawRequest