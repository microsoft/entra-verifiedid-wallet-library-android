package com.microsoft.walletlibrary.requests.requirements

sealed interface PresentationExchangeRequirement {

    /**
     * Presentation Exchange Input Descriptor ID for this request
     * @see https://identity.foundation/presentation-exchange/spec/v2.0.0/#input-descriptor
     */
    val inputDescriptorId: String

    /**
     * Presentation Exchange format
     */
    val format: PresentationExchangeVerifiedIdFormat

    /**
     * List of other input_descriptor IDs this credential should NOT form a presentation object with
     */
    val exclusivePresentationWith: List<String>?
}

/**
 * Format of the Verified ID fulfilling the requirement
 */
enum class PresentationExchangeVerifiedIdFormat {
    JWT_VC
}