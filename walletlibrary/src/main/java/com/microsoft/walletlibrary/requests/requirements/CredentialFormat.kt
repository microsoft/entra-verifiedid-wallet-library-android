/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.walletlibrary.requests.requirements

internal data class CredentialFormat(
    internal val format: String,
    internal val types: List<String>
)