/*---------------------------------------------------------------------------------------------
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License. See License.txt in the project root for license information.
*--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.createIssuedAndExpiryTime
import com.microsoft.walletlibrary.did.sdk.identifier.IdentifierManager
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.UUID

class ExtensionIdentifierManager internal constructor(private val identifierManager: IdentifierManager) {
    internal object Constants {
        const val VC_DATA_MODEL_CONTEXT = "https://www.w3.org/2018/credentials/v1"
        const val VC_DATA_MODEL_TYPE = "VerifiableCredential"
    }

    fun createEphemeralSelfSignedVerifiedId(
        claims: Map<String, String>,
        types: Array<String>
    ): VerifiedId? {
        try {
            var vcTypes = listOf(Constants.VC_DATA_MODEL_TYPE)
            vcTypes += types
            val vcDescriptor = VerifiableCredentialDescriptor(
                listOf(Constants.VC_DATA_MODEL_CONTEXT), vcTypes, claims
            )
            val identifier = runBlocking {
                when (val result =
                    this@ExtensionIdentifierManager.identifierManager.getMasterIdentifier()) {
                    is Result.Success -> result.payload
                    is Result.Failure -> {
                        SdkLog.e("Could not get DID", result.payload)
                        null
                    }
                }
            } ?: return null

            // TODO produce VerifiedId
            return null
            /*
            val signingKey = identifier.didDocumentKeys.first ?: return null
            val (issuedTime, expiryTime) = createIssuedAndExpiryTime(5 * 60)    // 5 minutes
            val content = VerifiableCredentialContent(
                UUID.randomUUID().toString(),
                vcDescriptor,
                identifier.id,
                identifier.id,
                issuedTime,
                expiryTime
            )
            val signer = TokenSigner()
            val vcToken = signer.signWithIdentifier(
                Json.encodeToString(
                    VerifiableCredentialContent.serializer(), content
                ), identifier
            )
            return VerifiableCredential()
             */
        } catch (_: Exception) {
            return null
        }
    }

    private fun createTokenHeader(keyId: String): JWSHeader {
        return JWSHeader.Builder(JWSAlgorithm.ES256K).type(JOSEObjectType.JWT).keyID(keyId).build()
    }
}
