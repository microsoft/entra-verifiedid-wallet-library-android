/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

data class IssuanceOptions(
    // Information like contract url which describes where to get the contract form
    val credentialIssuerMetadata: List<String>
)