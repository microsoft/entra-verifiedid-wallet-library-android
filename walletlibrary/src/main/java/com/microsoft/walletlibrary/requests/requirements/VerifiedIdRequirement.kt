/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.createIssuedAndExpiryTime
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestInput
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraintOperator
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.VerifiedIdConstraint
import com.microsoft.walletlibrary.util.MalformedInputException
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.RequirementValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import okhttp3.internal.filterList
import java.util.UUID
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Represents information that describes Verified IDs required in order to complete a VerifiedID request.
 */
@Serializable
class VerifiedIdRequirement(
    internal val id: String?,

    // The types of Verified ID required.
    val types: List<String>,

    // Indicates if the requirement must be encrypted.
    internal val encrypted: Boolean = false,

    // Indicates if the requirement is required or optional.
    override val required: Boolean = false,

    // Purpose of the requested Verified ID which could be displayed to user if needed.
    var purpose: String = "",

    // Information needed for issuance from presentation.
    val issuanceOptions: List<VerifiedIdRequestInput> = mutableListOf(),

    internal var _verifiedId: VerifiedId? = null
) : Requirement {

    companion object {
        private val SELF_SIGN_VALIDITY_INTERVAL = 5.toDuration(DurationUnit.MINUTES)
    }

    // Readonly Verified ID that is currently fulfilling the requirement (if any)
    val verifiedId: VerifiedId?
        get() = this._verifiedId

    // Constraint that represents how the requirement is fulfilled
    internal var constraint: VerifiedIdConstraint = toVcTypeConstraint()

    @Transient
    internal lateinit var signer: TokenSigner

    @Transient
    internal lateinit var json: Json

    internal fun toVcTypeConstraint(): VerifiedIdConstraint {
        if (types.isEmpty() || types.filterList { isNotBlank() }
                .isEmpty()) throw MalformedInputException(
            "There is no Verified ID type in the request.",
            VerifiedIdExceptions.MALFORMED_INPUT_EXCEPTION.value
        )
        if (types.size == 1) return VcTypeConstraint(types.first())
        val vcTypeConstraints = mutableListOf<VcTypeConstraint>()
        types.forEach { vcTypeConstraints.add(VcTypeConstraint(it)) }
        return GroupConstraint(vcTypeConstraints, GroupConstraintOperator.ANY)
    }

    // Validates the requirement and throws an exception if the requirement is invalid or not fulfilled.
    override fun validate(): VerifiedIdResult<Unit> {
        if (_verifiedId == null)
            return RequirementNotMetException(
                "Verified ID has not been set.",
                VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value
            ).toVerifiedIdResult()
        _verifiedId?.let {
            try {
                constraint.matches(it)
            } catch (constraintException: RequirementValidationException) {
                return RequirementNotMetException(
                    "Verified ID constraint do not match.",
                    VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value,
                    listOf(constraintException)
                ).toVerifiedIdResult()
            }
        }
        return VerifiedIdResult.success(Unit)
    }

    // Fulfills the requirement in the request with specified value.
    fun fulfill(selectedVerifiedId: VerifiedId): VerifiedIdResult<Unit> {
        try {
            constraint.matches(selectedVerifiedId)
        } catch (constraintException: RequirementValidationException) {
            return RequirementNotMetException(
                "Verified ID constraint do not match.",
                VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value,
                listOf(constraintException)
            ).toVerifiedIdResult()
        }
        _verifiedId = selectedVerifiedId
        return VerifiedIdResult.success(Unit)
    }

    // Fulfills the requirement with a self signed verified ID containing the included claims
    suspend fun fulfillWithClaims(claims: Map<String, String>): VerifiedIdResult<Unit> {
        val primaryIdentifier = when (val primaryIdentifierResult = VerifiableCredentialSdk.identifierService.getMasterIdentifier()) {
            is Result.Success -> {
                primaryIdentifierResult.payload
            }
            is Result.Failure -> {
                return VerifiedIdResult.failure(primaryIdentifierResult.payload)
            }
        }
        val content = createSelfSignedContent(claims, primaryIdentifier)
        val verifiedId = selfSignVerifiedId(content, primaryIdentifier)

        return fulfill(verifiedId)
    }

    // Retrieves list of Verified IDs from the provided list that matches this requirement.
    fun getMatches(verifiedIds: List<VerifiedId>): List<VerifiedId> {
        return verifiedIds.filter { constraint.doesMatch(it) }
    }

    // Creates verifiable credential contents for the requirement with the given claims and identifier
    private fun createSelfSignedContent(claims: Map<String, String>, identifier: Identifier): VerifiableCredentialContent {
        val descriptor = VerifiableCredentialDescriptor(
            context = listOf(Constants.CONTEXT),
            type = types,
            credentialSubject = claims
        )
        // Unique headers for a short-lived self-signed verifiable credential.
        val (issuedTime, expiryTime: Long) = createIssuedAndExpiryTime(SELF_SIGN_VALIDITY_INTERVAL.inWholeSeconds.toInt())
        return VerifiableCredentialContent(
            jti = UUID.randomUUID().toString(),
            vc = descriptor,
            sub = identifier.id,
            iss = identifier.id,
            iat = issuedTime,
            exp = expiryTime
        )
    }

    // Creates a self signed Verified ID from verifiable credential content and a signing identifier
    private fun selfSignVerifiedId(content: VerifiableCredentialContent, identifier: Identifier): VerifiedId {
        // Client API does not understand "credentialStatus: null" so a custom serializer must exclude the field.
        val nonNullSerializer = Json(from = json) {
            this.encodeDefaults = false
        }
        // Sign the data.
        val serialized = nonNullSerializer.encodeToString(VerifiableCredentialContent.serializer(), content)
        val rawCredential = signer.signWithIdentifier(serialized, identifier)
        val verifiableCredential = VerifiableCredential(
            content.jti,
            rawCredential,
            content
        )
        return com.microsoft.walletlibrary.verifiedid.VerifiableCredential(
            verifiableCredential
        )
    }
}