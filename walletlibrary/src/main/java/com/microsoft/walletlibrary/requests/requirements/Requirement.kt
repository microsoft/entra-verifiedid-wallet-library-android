/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.walletlibrary.requests.requirements

/**
 * Represents the necessary information required in order to complete a Verified ID request (issuance or presentation)
 */
interface Requirement {
    // Indicates whether the requirement is required or optional
    val required: Boolean

    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled
    fun validate()
}