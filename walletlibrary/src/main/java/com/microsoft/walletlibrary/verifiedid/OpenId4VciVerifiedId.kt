// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import kotlinx.serialization.Serializable
import java.util.Date

/**
 * Holds the information related to a VerifiedID issued using OpenID4VCI like the claims, issued and expiry dates.
 */
@Serializable
internal class OpenId4VciVerifiedId(
    // The deserialized Verifiable Credential received during issuance.
    val raw: VerifiableCredential,

    // Name of the issuer.
    val issuerName: String,

    // Information about the issuer and credential issued.
    val credentialConfiguration: CredentialConfiguration
) : VerifiedId {
    override val id = raw.jti

    @Serializable(with = DateSerializer::class)
    override val issuedOn = Date(raw.contents.iat * 1000L)

    @Serializable(with = DateSerializer::class)
    override val expiresOn = raw.contents.exp?.let { Date(it * 1000L) }

    val types = raw.contents.vc.type

    override val style = credentialConfiguration.getVerifiedIdStyleInPreferredLocale(issuerName)

    override fun getClaims(): ArrayList<VerifiedIdClaim> {
        val claimValues = raw.contents.vc.credentialSubject

        // TODO("Add support for path in Claims)
        val claims = ArrayList<VerifiedIdClaim>()
        for ((claimIdentifier, claimValue) in claimValues) {
            claims.add(createVerifiedIdClaim(claimIdentifier, claimValue))
        }
        return claims
    }

    private fun createVerifiedIdClaim(claimReference: String, claimValue: Any): VerifiedIdClaim {
        val claimDefinitions = credentialConfiguration.credential_definition?.credentialSubject
        val claimDisplayDefinition = claimDefinitions?.get("vc.credentialSubject.$claimReference")
            ?: return VerifiedIdClaim(claimReference, claimValue, null)
        val localizedDisplayDefinition =
            claimDisplayDefinition.getPreferredLocalizedDisplayDefinition()
        return if (localizedDisplayDefinition?.name != null)
            VerifiedIdClaim(
                localizedDisplayDefinition.name,
                claimValue,
                claimDisplayDefinition.value_type
            )
        else
            VerifiedIdClaim(claimReference, claimValue, claimDisplayDefinition.value_type)
    }
}