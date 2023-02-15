/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.mappings.presentation

import android.net.Uri
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.util.MissingVerifiedIdTypeException

internal fun CredentialPresentationInputDescriptor.toVerifiedIdRequirement(): VerifiedIdRequirement {
    if (this.schemas.isEmpty())
        throw MissingVerifiedIdTypeException("There is no VerifiedId Type in credential input descriptor.")
    return VerifiedIdRequirement(
        this.id,
        this.schemas.map { it.uri },
        encrypted = false,
        required = true,
        this.purpose,
        this.issuanceMetadataList.map { VerifiedIdRequestURL(Uri.parse(it.issuerContract)) }
    )
}