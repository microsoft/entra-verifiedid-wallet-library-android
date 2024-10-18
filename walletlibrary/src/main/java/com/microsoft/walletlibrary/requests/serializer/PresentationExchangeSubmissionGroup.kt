package com.microsoft.walletlibrary.requests.serializer

import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationSubmissionDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.walletlibrary.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.createIssuedAndExpiryTime
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.requests.requirements.PresentationExchangeRequirement
import com.microsoft.walletlibrary.requests.requirements.PresentationExchangeVerifiedIdRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.verifiedid.VCVerifiedIdSerializer
import kotlinx.serialization.json.Json
import java.util.UUID

internal class PresentationExchangeSubmissionGroup (
    private val subject: Identifier
) {
    private var requirementAndCredential: MutableList<Pair<PresentationExchangeRequirement, String>> = mutableListOf()
    private var excludeInputDescriptors: MutableSet<String> = mutableSetOf()

    fun canIncludeInGroup(requirement: Requirement): Boolean {
        (requirement as? PresentationExchangeVerifiedIdRequirement)?.let result@{
            // Only those of the same subject can be presented together
            it.verifiedId?.let {
                verifiedId ->
                val credentialSubject = VCVerifiedIdSerializer.serialize(verifiedId).contents.sub
                if (credentialSubject != subject.id) {
                    return@result false
                }
            }
            // Are any credentials exclusive?
            if (excludeInputDescriptors.contains(it.inputDescriptorId)) {
                return@result false
            }
            requirementAndCredential.forEach { requirementAndCredential ->
                if (it.exclusivePresentationWith?.contains(requirementAndCredential.first.inputDescriptorId) == true) {
                    return@result  false
                }
            }
            return@result true
        }
        return false
    }

    fun include(requirement: PresentationExchangeRequirement, rawCredential: String) {
        requirementAndCredential.add(Pair(requirement, rawCredential))
        requirement.exclusivePresentationWith?.let {
            excludeInputDescriptors.addAll(it)
        }
    }

    fun getVerifiablePresentation(signer: TokenSigner,
                                  serializer: Json,
                                  validityInterval: Int,
                                  audience: String,
                                  nonce: String): String {
        val verifiablePresentation = VerifiablePresentationDescriptor(
            verifiableCredential = requirementAndCredential.map { it.second },
            context = listOf(Constants.VP_CONTEXT_URL),
            type = listOf(Constants.VERIFIABLE_PRESENTATION_TYPE)
        )

        val (issuedTime, expiryTime: Long) = createIssuedAndExpiryTime(validityInterval)
        val vpId = UUID.randomUUID().toString()
        val responderDid = subject.id
        val contents =
            VerifiablePresentationContent(
                vpId = vpId,
                verifiablePresentation = verifiablePresentation,
                issuerOfVp = responderDid,
                tokenIssuedTime = issuedTime,
                tokenNotValidBefore = issuedTime,
                tokenExpiryTime = expiryTime,
                audience = audience,
                nonce = nonce
            )
        val serializedContents = serializer.encodeToString(VerifiablePresentationContent.serializer(), contents)
        return signer.signWithIdentifier(serializedContents, subject)
    }

    fun getPresentationSubmissionMap(presentationIndex: Int): List<PresentationSubmissionDescriptor> {
        return requirementAndCredential.mapIndexed { index, requirementAndCredential ->
            val requirement = requirementAndCredential.first
            return@mapIndexed PresentationSubmissionDescriptor(
                idFromPresentationRequest = requirement.inputDescriptorId,
                format = "jwt_vp",
                path = "$[${presentationIndex}]",
                pathNested = PresentationSubmissionDescriptor(
                    idFromPresentationRequest = requirement.inputDescriptorId,
                    format = requirement.format.name,
                    path = "$.verifiableCredential[${index}]"
                )
            )
        }
    }
}