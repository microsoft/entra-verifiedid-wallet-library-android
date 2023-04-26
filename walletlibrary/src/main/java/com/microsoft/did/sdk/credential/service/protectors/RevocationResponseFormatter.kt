/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.RevocationRequest
import com.microsoft.did.sdk.credential.service.models.oidc.RevocationResponseClaims
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that forms Response Contents Properly.
 */
@Singleton
internal class RevocationResponseFormatter @Inject constructor(
    private val serializer: Json,
    private val signer: TokenSigner,
    private val keyStore: EncryptedKeyStore
) {

    fun formatResponse(revocationRequest: RevocationRequest, expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS): String {
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val responder = revocationRequest.owner
        val keyJwk = keyStore.getKey(revocationRequest.owner.signatureKeyReference)
        val responseId = UUID.randomUUID().toString()
        val contents =
            RevocationResponseClaims(revocationRequest.rpList, revocationRequest.reason, revocationRequest.verifiableCredential.raw).apply {
                subject = keyJwk.computeThumbprint().toString()
                audience = revocationRequest.audience
                did = responder.id
                publicKeyJwk = keyJwk.toPublicJWK()
                responseCreationTime = issuedTime
                responseExpirationTime = expiryTime
                this.responseId = responseId
            }
        return signContents(contents, responder)
    }

    private fun signContents(contents: RevocationResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.encodeToString(RevocationResponseClaims.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }
}