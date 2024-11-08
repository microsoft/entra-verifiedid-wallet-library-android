/*---------------------------------------------------------------------------------------------
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License. See License.txt in the project root for license information.
*--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.ExtensionIdentifierManager.Constants.SELF_ISSUED_ISSUER_NAME
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.JwsHeaderFormatter
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.createIssuedAndExpiryTime
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.identifier.HolderIdentifier
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialConfiguration
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.verifiedid.OpenId4VciVerifiedId
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import java.util.UUID

class ExtensionIdentifierManager internal constructor(private val libraryConfiguration: LibraryConfiguration) {
    private val serializer = libraryConfiguration.serializer

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
            val identifier = libraryConfiguration.identifierFactory.getIdentifier()

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
            val vcToken = createAndSignToken(identifier, jsonContent)
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

    private fun createAndSignToken(identifier: HolderIdentifier, jsonContent: String): String {
        val jwsHeader = JwsHeaderFormatter.formatHeader(identifier)
        val jwsToken = JwsToken(jsonContent, jwsHeader)
        return jwsToken.sign(identifier)
    }
}
