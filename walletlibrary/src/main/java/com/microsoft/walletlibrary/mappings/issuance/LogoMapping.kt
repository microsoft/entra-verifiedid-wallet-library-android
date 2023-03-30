/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.contracts.display.Logo
import com.microsoft.walletlibrary.requests.styles.VerifiedIdLogo

internal fun Logo.toLogo(): VerifiedIdLogo {
    return VerifiedIdLogo(this.uri, this.description)
}