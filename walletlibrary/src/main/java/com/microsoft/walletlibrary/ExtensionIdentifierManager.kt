/*---------------------------------------------------------------------------------------------
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License. See License.txt in the project root for license information.
*--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.ExtensionIdentifierManager.Constants.SELF_ISSUED_ISSUER_NAME
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.createIssuedAndExpiryTime
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.verifiedid.OpenId4VciVerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.coroutines.runBlocking
import java.util.UUID

class ExtensionIdentifierManager internal constructor(libraryConfiguration: LibraryConfiguration) {
    private val identifierManager = libraryConfiguration.identifierManager
    private val serializer = libraryConfiguration.serializer
    private val signer = libraryConfiguration.tokenSigner

    internal object Constants {
        const val VC_DATA_MODEL_CONTEXT = "https://www.w3.org/2018/credentials/v1"
        const val VC_DATA_MODEL_TYPE = "VerifiableCredential"
        const val SELF_ISSUED_ISSUER_NAME = "Self"
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
            val jsonContent = serializer.encodeToString(VerifiableCredentialContent.serializer(), content)
            val vcToken = signer.signWithIdentifier(jsonContent, identifier)
            return OpenId4VciVerifiedId(
                com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential(
                    jti,
                    vcToken,
                    content
                ),
                SELF_ISSUED_ISSUER_NAME,
                CredentialConfiguration()
            )
        } catch (_: Exception) {
            return null
        }
    }
}
