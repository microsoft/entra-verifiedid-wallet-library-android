/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.presentation

import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationDefinition
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.MissingInputDescriptorException

/**
 * Maps PresentationDefinition object of Presentation Exchange in SDK to corresponding Requirement to library.
 */
internal fun PresentationDefinition.toRequirement(): Requirement {
    if (this.credentialPresentationInputDescriptors.isEmpty())
        throw MissingInputDescriptorException("There is no credential input descriptor in presentation definition.")
    return if (this.credentialPresentationInputDescriptors.size == 1)
        this.credentialPresentationInputDescriptors.first().toVerifiedIdRequirement()
    else
        GroupRequirement(
            true,
            this.credentialPresentationInputDescriptors.map { it.toVerifiedIdRequirement() },
            GroupRequirementOperator.ANY
        )
}