package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.mappings.issuance.toVerifiedIdStyle
import kotlinx.serialization.Serializable
import java.util.Date

/**
 * Holds the information related to a VerifiedID like the claims, issued and expiry dates.
 */
@Serializable
internal class VerifiableCredential(
    internal val raw: VerifiableCredential,
    internal val contract: VerifiableCredentialContract? = null,
    override val types: List<String> = raw.contents.vc.type
): VerifiedId {
    override val id = raw.jti

    @Serializable(with = DateSerializer::class)
    override val issuedOn = Date(raw.contents.iat * 1000L)

    @Serializable(with = DateSerializer::class)
    override val expiresOn = raw.contents.exp?.let { Date(it * 1000L) }

    override val style = contract?.display?.toVerifiedIdStyle()

    override fun getClaims(): ArrayList<VerifiedIdClaim> {
        val claimDescriptors = contract?.display?.claims
        val claimValues = raw.contents.vc.credentialSubject

        //TODO("Add support for type and path in Claims)
        val claims = ArrayList<VerifiedIdClaim>()
        for ((claimIdentifier, claimValue) in claimValues) {
            val claimDescriptor = claimDescriptors?.get("vc.credentialSubject.$claimIdentifier")
            claimDescriptor?.let { claims.add(VerifiedIdClaim(claimIdentifier, claimValue, claimDescriptor.label, claimDescriptor.type)) }
                ?: claims.add(VerifiedIdClaim(claimIdentifier, claimValue, null, null))
        }
        return claims
    }
}