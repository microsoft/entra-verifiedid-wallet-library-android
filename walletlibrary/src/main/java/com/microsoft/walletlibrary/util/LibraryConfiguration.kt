package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.identifier.IdentifierManager
import com.microsoft.walletlibrary.did.sdk.identifier.resolvers.RootOfTrustResolver
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result
import com.microsoft.walletlibrary.identifier.IdentifierFactory
import com.microsoft.walletlibrary.mappings.identifier.toHolderIdentifier
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * Configuration such as preview feature flags used by the library.
 */
internal class LibraryConfiguration(
    private val previewFeatureFlags: PreviewFeatureFlags,
    val httpAgentApiProvider: HttpAgentApiProvider,
    val serializer: Json,
    val rootOfTrustResolver: RootOfTrustResolver? = null,
    val identifierManager: IdentifierManager,
    val tokenSigner: TokenSigner,
    val logger: WalletLibraryLogger,
    val identifierFactory: IdentifierFactory
) {

    init {
        runBlocking {
            when (val defaultIdentifier = VerifiableCredentialSdk.identifierService.getMasterIdentifier()) {
                is Result.Success -> {
                    val holderIdentifier = defaultIdentifier.payload.toHolderIdentifier(VerifiableCredentialSdk.identifierService.getKeyStore())
                    identifierFactory.identifiers.add(holderIdentifier)
                }
                is Result.Failure -> {
                    throw IllegalStateException("Unable to fetch master identifier")
                }
            }
        }
    }

    // Determine if a preview feature is enabled.
    fun isPreviewFeatureEnabled(previewFeatureFlag: String): Boolean {
        return previewFeatureFlags.isPreviewFeatureSupported(previewFeatureFlag)
    }
}