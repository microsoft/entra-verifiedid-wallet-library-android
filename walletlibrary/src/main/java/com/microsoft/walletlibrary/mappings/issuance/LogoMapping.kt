/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.walletlibrary.requests.styles.Logo

internal fun com.microsoft.did.sdk.credential.service.models.contracts.display.Logo.toLogo(): Logo {
    return Logo(this.uri, this.image, this.description)
}