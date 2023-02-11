package com.microsoft.walletlibrary.mappings

import android.net.Uri
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement

fun CredentialPresentationInputDescriptor.toVerifiedIdRequirement(): VerifiedIdRequirement {
    return VerifiedIdRequirement(
        this.id,
        this.schemas.map { it.uri },
        encrypted = false,
        required = true,
        this.purpose,
        this.issuanceMetadataList.map { VerifiedIdRequestURL(Uri.parse(it.issuerContract)) }
    )
}