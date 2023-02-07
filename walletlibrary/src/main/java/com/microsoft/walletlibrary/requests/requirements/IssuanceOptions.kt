/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

/**
 * Represents the information needed for issuance of a Verified ID invoked from a presentation.
 */
data class IssuanceOptions(
    // Information like contract url which describes where to get the contract form.
    val credentialIssuerMetadata: List<String>
)