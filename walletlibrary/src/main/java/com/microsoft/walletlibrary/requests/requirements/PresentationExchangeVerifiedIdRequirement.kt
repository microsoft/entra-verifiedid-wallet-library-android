package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.util.RequirementValidationException
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId

/**
 * A VerifiedIdRequirement with additional PresentationExchange claims
 */
class PresentationExchangeVerifiedIdRequirement(
    id: String?,
    // The types of Verified ID required.
    types: List<String>,
    // Indicates if the requirement must be encrypted.
    encrypted: Boolean = false,
    // Indicates if the requirement is required or optional.
    required: Boolean = false,
    // Purpose of the requested Verified ID which could be displayed to user if needed.
    purpose: String = "",
    // Information needed for issuance from presentation.
    issuanceOptions: List<VerifiedIdRequestInput> = mutableListOf(),

    /**
     * Presentation Exchange Input Descriptor ID for this request
     * @see https://identity.foundation/presentation-exchange/spec/v2.0.0/#input-descriptor
     */
    val inputDescriptorId: String,

    /**
     * Presentation Exchange format
     */
    val format: PresentationExchangeVerifiedIdFormat = PresentationExchangeVerifiedIdFormat.JWT_VC,

    /**
     * List of other input_descriptor IDs this credential should NOT form a presentation object with
     */
    val exclusivePresentationWith: List<String>?
) : VerifiedIdRequirement(id, types, encrypted, required, purpose, issuanceOptions) {
    /**
     * Credential submission to be encoded into the presentation
     */
    internal var encodedSubmission: String? = ""

    override fun fulfill(selectedVerifiedId: VerifiedId): VerifiedIdResult<Unit> {
        val response = super.fulfill(selectedVerifiedId)
        return if (response.isSuccess) {
            // handle encodedSubmission if possible, else throw
            when (verifiedId) {
                is VerifiableCredential -> {
                    encodedSubmission = (verifiedId as VerifiableCredential).raw.raw
                    response
                }
                else -> {
                    VerifiedIdResult.failure(RequirementValidationException("Unsupported Verified ID Format"))
                }
            }
        } else {
            // failure by VerifiedIdRequirements, can be returned directly
            response
        }
    }
}

/**
 * Format of the Verified ID fulfilling the requirement
 */
enum class PresentationExchangeVerifiedIdFormat {
    JWT_VC
}