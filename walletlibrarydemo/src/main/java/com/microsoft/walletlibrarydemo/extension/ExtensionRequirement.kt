package com.microsoft.walletlibrarydemo.extension

import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiedIdSerializer
import com.microsoft.walletlibrary.requests.handlers.RequestProcessorSerializer

class ExtensionRequirement(val displayName: String) : Requirement {


    override val required: Boolean = false

    override fun validate(): VerifiedIdResult<Unit> {
        return VerifiedIdResult.success(Unit)
    }

    /**
     * Serializes the requirement into its raw format.
     * If this requirement is composed or an aggregate of other requirements, MUST call the protocolSerializer's serialize function on all used requirements.
     * returns the raw format for a given SerializedFormat type (if it has output).
     */
    override suspend fun <T> serialize(
        protocolSerializer: RequestProcessorSerializer<T>,
        verifiedIdSerializer: VerifiedIdSerializer<T>
    ): T? {
        return null
    }
}