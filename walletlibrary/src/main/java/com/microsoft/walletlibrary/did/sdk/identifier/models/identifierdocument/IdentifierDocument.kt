package com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument

import com.microsoft.walletlibrary.did.sdk.identifier.models.payload.document.IdentifierDocumentService
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Document returned on resolving an identifier
 * Refer to https://www.w3.org/TR/did-core/#core-properties for more details on identifier document
 */
@Serializable
internal data class IdentifierDocument(
    val verificationMethod: List<IdentifierDocumentPublicKey>? = null,
    val id: String
) {
    var service: List<IdentifierDocumentService> = emptyList()
}