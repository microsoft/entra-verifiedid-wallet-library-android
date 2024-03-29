package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import kotlinx.serialization.Serializable

/**
 * The types of proofs supported.
 */
@Serializable
data class ProofTypesSupported(
    // The type of proof that can be used to show ownership of keys bound to crypto binding method (ex. jwt).
    val proof_signing_alg_values_supported: List<String>
)