/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.requests.handlers.RequestProcessorSerializer
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiedIdSerializer
import kotlin.reflect.safeCast

/**
 * Represents information that describes self-attested claims required in order to complete a VerifiedID request.
 */
class SelfAttestedClaimRequirement(
    internal val id: String,

    // Claim requested.
    val claim: String,

    // Indicates if the requirement must be encrypted.
    internal val encrypted: Boolean = false,

    // Indicates if the requirement is required or optional.
    override val required: Boolean = false,

    internal var value: String? = null
) : Requirement {
    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate(): VerifiedIdResult<Unit> {
        //TODO("should required field be checked?")
        if (value == null)
            return RequirementNotMetException(
                "Self Attested Claim has not been set.",
                VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value
            ).toVerifiedIdResult()
        return VerifiedIdResult.success(Unit)
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(selfAttestedClaimValue: String) {
        value = selfAttestedClaimValue
    }

    @Throws
    override fun <T: Any> serialize(
        protocolSerializer: RequestProcessorSerializer,
        verifiedIdSerializer: VerifiedIdSerializer<T>
    ): T? {
        return verifiedIdSerializer.serializedFormat.safeCast(value)
    }
}
