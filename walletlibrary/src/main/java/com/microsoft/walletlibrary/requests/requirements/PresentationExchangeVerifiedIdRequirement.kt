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
    // Presentation Exchange Input Descriptor ID for this request
    override val inputDescriptorId: String,
    // Presentation Exchange format
    override val format: PresentationExchangeVerifiedIdFormat = PresentationExchangeVerifiedIdFormat.JWT_VC,
    // List of other input_descriptor IDs this credential should NOT form a presentation object with
    override val exclusivePresentationWith: List<String>? = null,
) : VerifiedIdRequirement(
    id,
    types,
    encrypted,
    required,
    purpose,
    issuanceOptions), PresentationExchangeRequirement {}