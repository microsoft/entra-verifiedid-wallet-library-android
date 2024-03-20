package com.microsoft.walletlibrary.requests.openid4vci

import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.requests.RootOfTrust
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiedId

internal class OpenId4VciIssuanceRequest(
    // Attributes describing the requester (eg. name, logo).
    override val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    override val requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    override val rootOfTrust: RootOfTrust,

    // Attributes describing the Verified ID (eg. name, issuer, logo, background and text colors).
    override val verifiedIdStyle: VerifiedIdStyle,

    val credentialOffer: CredentialOffer,

    val credentialMetadata: CredentialMetadata
) : VerifiedIdIssuanceRequest {
    override suspend fun complete(): VerifiedIdResult<VerifiedId> {
        TODO("Not yet implemented")
    }

    override fun isSatisfied(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun cancel(message: String?): VerifiedIdResult<Unit> {
        TODO("Not yet implemented")
    }
}