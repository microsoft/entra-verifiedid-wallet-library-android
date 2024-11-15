package com.microsoft.walletlibrary.requests.serializer

import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc.VpTokenInResponse
import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.JwsHeaderFormatter
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.createIssuedAndExpiryTime
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.identifier.HolderIdentifier
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
                    val identifier = libraryConfiguration.identifierFactory.getIdentifier()
                    val group = PresentationExchangeSubmissionGroup(identifier)
                    group.include(requirement, rawCredential)
                    vpTokens.add(group)
                    return@let
                }
            }

            is GroupRequirement -> {
                requirement.serialize(this, verifiedIdSerializer)
            }

            else -> {
                libraryConfiguration.logger.w(
                    "Unknown credential type ${requirement.javaClass.name} returned" +
                        " credential data that cannot be formatted in response"
                )
            }
        }
    }

    fun buildVpTokens(audience: String, nonce: String, ttlInSeconds: Int = 3600): List<String> {
        return vpTokens.map {
            it.getVerifiablePresentation(
                libraryConfiguration.serializer,
                ttlInSeconds,
                audience,
                nonce
            )
        }
    }

    suspend fun buildIdToken(
        definitionId: String,
        clientId: String,
        requestNonce: String,
        ttlInSeconds: Int = 3600
    ): String {
        val identifier = libraryConfiguration.identifierFactory.getIdentifier()
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(ttlInSeconds)
        val vpTokens = this.vpTokens.mapIndexed { index, vpToken ->
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
        return createAndSignToken(identifier, token)
    }

    private fun createAndSignToken(identifier: HolderIdentifier, jsonContent: String): String {
        val jwsHeader = JwsHeaderFormatter.formatHeader(identifier)
        val jwsToken = JwsToken(jsonContent, jwsHeader)
        return jwsToken.sign(identifier)
    }
}