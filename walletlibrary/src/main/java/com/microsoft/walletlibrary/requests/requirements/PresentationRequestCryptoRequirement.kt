// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.Registration
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.SubjectIdentifierTypeNotSupported
import com.microsoft.walletlibrary.did.sdk.util.controlflow.VpFormatNotSupported
import com.microsoft.walletlibrary.identifier.HolderIdentifier
import com.microsoft.walletlibrary.identifier.IdentifierFactory

internal class PresentationRequestCryptoRequirement(private val registration: Registration) : CryptoRequirement {
    private val usesLegacyRegistration: Boolean =
        registration.subjectSyntaxTypesSupported.contains(Constants.SUBJECT_IDENTIFIER_TYPE_DID_ION) &&
            (registration.vpFormats == null ||
                (registration.vpFormats.jwtVp.contains(Constants.ALGORITHM_SUPPORTED_IN_VP) &&
                    registration.vpFormats.jwtVc.contains(Constants.ALGORITHM_SUPPORTED_IN_VC)))

    override fun isSupported(holderIdentifier: HolderIdentifier): Boolean =
        registration.subjectSyntaxTypesSupported.contains(holderIdentifier.method) &&
            holderIdentifier.algorithm in holderSigningAlgorithms &&
            (registration.vpFormats == null ||
                registration.vpFormats.jwtVp.contains(holderIdentifier.algorithm))

    fun validate(identifiers: List<HolderIdentifier>) {
        // Preserve negotiation and holder selection for metadata accepted by the legacy validator.
        if (usesLegacyRegistration) return
        if (identifiers.none { registration.subjectSyntaxTypesSupported.contains(it.method) })
            throw SubjectIdentifierTypeNotSupported("The subject identifier type in registration of request is not supported")
        if (identifiers.none { isSupported(it) })
            throw VpFormatNotSupported("VP format algorithm in registration of request is not supported")
        // VCs retain their issuer's signature; their algorithms are not holder signing capabilities.
        if (registration.vpFormats != null &&
            registration.vpFormats.jwtVc.none { it in credentialSigningAlgorithms })
            throw VpFormatNotSupported("VC format algorithm in registration of request is not supported")
    }

    fun getIdentifier(factory: IdentifierFactory, subject: String? = null): HolderIdentifier {
        if (usesLegacyRegistration)
            return factory.getIdentifier(subject?.let { MatchingSubjectCryptoRequirement(it) })
        val identifiers = factory.identifiers.filter { subject == null || it.id == subject }
        validate(identifiers)
        return identifiers.first { isSupported(it) }
    }

    fun getResponseIdentifier(factory: IdentifierFactory, credentialSubjects: List<String>): HolderIdentifier {
        if (usesLegacyRegistration) return factory.getIdentifier()
        val subjects = credentialSubjects.distinct()
        if (subjects.size > 1)
            throw SubjectIdentifierTypeNotSupported("The single-holder presentation response cannot represent multiple credential subjects")
        return getIdentifier(factory, subjects.singleOrNull())
    }

    private companion object {
        // ECDSASignerWrapper supports these two algorithms, including for injected holders.
        val holderSigningAlgorithms = setOf("ES256", "ES256K")
        val credentialSigningAlgorithms = setOf("ES256", "ES256K", "ES384", "ES512", "EdDSA")
    }
}
