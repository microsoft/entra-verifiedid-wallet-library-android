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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class ExtensionIdentifierManager internal constructor(private val identifierManager: IdentifierManager, private val signer: TokenSigner, private val serializer: Json) {
    internal object Constants {
        const val VC_DATA_MODEL_CONTEXT = "https://www.w3.org/2018/credentials/v1"
        const val VC_DATA_MODEL_TYPE = "VerifiableCredential"
    }

    fun createEphemeralSelfSignedVerifiedId(
        claims: Map<String, String>,
        types: Array<String>
    ): VerifiedId? {
        try {
            val vcTypes = mutableListOf(Constants.VC_DATA_MODEL_TYPE)
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

            val (issuedTime, expiryTime) = createIssuedAndExpiryTime(5 * 60)    // 5 minutes
            val jti = UUID.randomUUID().toString()
            val content = VerifiableCredentialContent(
                jti,
                vcDescriptor,
                identifier.id,
                identifier.id,
                issuedTime,
                expiryTime
            )
            val jsonContent = serializer.encodeToString (content)
            val vcToken = signer.signWithIdentifier(jsonContent, identifier)
            return VerifiableCredential(com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential(
                jti,
                vcToken,
                content
            ))
        } catch (_: Exception) {
            return null
        }
    }
}
