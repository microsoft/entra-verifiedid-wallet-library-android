package com.microsoft.walletlibrary.verifiedid

import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import java.util.*

/**
 * Holds the information related to a VerifiedID like the claims, issued and expiry dates.
 */
internal class VerifiableCredential(
    private val raw: com.microsoft.did.sdk.credential.models.VerifiableCredential,
    private val contract: VerifiableCredentialContract
): VerifiedId {
    override val id = raw.jti
    override val issuedOn = Date(raw.contents.iat * 1000L)
    override val expiresOn = raw.contents.exp?.let { Date(it * 1000L) }

    override fun getClaims(): List<VerifiedIdClaim> {
        val claimDescriptors = contract.display.claims
        val claimValues = raw.contents.vc.credentialSubject

        //TODO("Add support for type and path in Claims)
        val claims = ArrayList<VerifiedIdClaim>()
        for ((claimIdentifier, claimValue) in claimValues) {
            val claimDescriptor = claimDescriptors["vc.credentialSubject.$claimIdentifier"]
            claimDescriptor?.let { claims.add(VerifiedIdClaim(claimDescriptor.label, claimValue)) }
                ?: claims.add(VerifiedIdClaim(claimIdentifier, claimValue))
        }
        return claims
    }
}