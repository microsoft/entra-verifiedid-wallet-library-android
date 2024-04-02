// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
internal class OpenId4VciVerifiedId(
    val raw: VerifiableCredential,
    val issuerName: String,
    val credentialConfiguration: CredentialConfiguration
) : VerifiedId {
    override val id = raw.jti

    @Serializable(with = DateSerializer::class)
    override val issuedOn = Date(raw.contents.iat * 1000L)

    @Serializable(with = DateSerializer::class)
    override val expiresOn = raw.contents.exp?.let { Date(it * 1000L) }

    override val style = credentialConfiguration.getVerifiedIdStyleInPreferredLocale(issuerName)

    override val types: List<String> = raw.contents.vc.type

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
        val localizedDisplayDefinition = claimDisplayDefinition.getPreferredLocalizedDisplayDefinition()
        return if (localizedDisplayDefinition?.name != null)
            VerifiedIdClaim(localizedDisplayDefinition.name, claimValue, claimDisplayDefinition.value_type)
        else
            VerifiedIdClaim(claimReference, claimValue, claimDisplayDefinition.value_type)
    }
}