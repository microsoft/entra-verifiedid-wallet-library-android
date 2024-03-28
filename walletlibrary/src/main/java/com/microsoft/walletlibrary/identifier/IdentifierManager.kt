package com.microsoft.walletlibrary.identifier

import com.microsoft.walletlibrary.did.sdk.IdentifierService
import com.microsoft.walletlibrary.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.walletlibrary.did.sdk.identifier.IdentifierManager
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.controlflow.Result

internal class IdentifierManager constructor(
    private val identifierService: IdentifierService
): IdentifierManager {
    override suspend fun getMasterIdentifier(): Result<Identifier> {
        return identifierService.getMasterIdentifier()
    }

    override fun getTokenSigner(): TokenSigner {
        return identifierService.getTokenSigner()
    }
}