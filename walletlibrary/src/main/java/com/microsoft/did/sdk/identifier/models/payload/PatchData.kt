package com.microsoft.did.sdk.identifier.models.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing list of patch operations that can be performed on a identifier document and commit/reveal value
 * to be used during the next update operation
 */
@Serializable
internal data class PatchData(
    @SerialName("updateCommitment")
    val nextUpdateCommitmentHash: String,
    @SerialName("patches")
    val patches: List<IdentifierDocumentPatch>
)