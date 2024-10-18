package com.microsoft.walletlibrary.requests.serializer

import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.VpTokenInResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.createIssuedAndExpiryTime
import com.microsoft.walletlibrary.did.sdk.util.controlflow.toNative
import com.microsoft.walletlibrary.requests.handlers.RequestProcessorSerializer
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.PresentationExchangeRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.verifiedid.VerifiedIdSerializer
import java.util.UUID

internal class PresentationExchangeResponseBuilder(
    private val libraryConfiguration: LibraryConfiguration
) : RequestProcessorSerializer<String> {

    private var vpTokens: MutableList<PresentationExchangeSubmissionGroup> = mutableListOf()

    /**
     * Processes and serializes this requirement using Requirement.serialize
     * note: Requirement.Serialize must be called and is expected to call this method on any child requirements before returning
     */
    override suspend fun serialize(
        requirement: Requirement,
        verifiedIdSerializer: VerifiedIdSerializer<String>
    ) {
        when (requirement) {
            is PresentationExchangeRequirement -> {
                requirement.serialize(this, verifiedIdSerializer)?.let { rawCredential ->
                    // try adding requirement to a group
                    vpTokens.forEach {
                        if (it.canIncludeInGroup(requirement)) {
                            it.include(requirement, rawCredential)
                            return@let
                        }
                    }
                    // create a new group
                    val exception =
                        libraryConfiguration.identifierManager.getMasterIdentifier().toNative().map { identifier ->
                            val group = PresentationExchangeSubmissionGroup(identifier)
                            group.include(requirement, rawCredential)
                            vpTokens.add(group)
                            return@let
                        }.exceptionOrNull()

                    if (exception != null) {
                        throw exception
                    }
                }
            }
            is GroupRequirement -> {
                requirement.serialize(this, verifiedIdSerializer)
            }
            else -> {
                libraryConfiguration.logger.w("Unknown credential type ${requirement.javaClass.name} returned" +
                        " credential data that cannot be formatted in response")
            }
        }
    }

    fun buildVpTokens(audience: String, nonce: String, ttlInSeconds: Int = 3600): List<String> {
        return vpTokens.map {
            it.getVerifiablePresentation(
                libraryConfiguration.tokenSigner,
                libraryConfiguration.serializer,
                ttlInSeconds,
                audience,
                nonce
            )
        }
    }
    suspend fun buildIdToken(definitionId: String,
                             clientId: String,
                             requestNonce: String,
                             ttlInSeconds: Int = 3600): String {
        val exception = libraryConfiguration.identifierManager.getMasterIdentifier().toNative().map { identifier ->
            val (issuedTime, expiryTime) = createIssuedAndExpiryTime(ttlInSeconds)
            val vpTokens = this.vpTokens.mapIndexed {
                index, vpToken ->
                vpToken.getPresentationSubmissionMap(index)
            }.flatten()

            val submission = VpTokenInResponse(
                PresentationSubmission(
                    id = UUID.randomUUID().toString(),
                    definitionId = definitionId,
                    presentationSubmissionDescriptors = vpTokens
                )
            )

            val vpClaims = PresentationResponseClaims(listOf(submission))

            val oidcResponseClaims = vpClaims.apply {
                subject = identifier.id
                audience = clientId
                nonce = requestNonce
                responseCreationTime = issuedTime
                responseExpirationTime = expiryTime
            }

            val token = libraryConfiguration.serializer.encodeToString(PresentationResponseClaims.serializer(), oidcResponseClaims)
            return libraryConfiguration.tokenSigner.signWithIdentifier(token, identifier)
        }.exceptionOrNull()

        if (exception != null) {
            throw exception
        }
        // unreachable
        return ""
    }

}