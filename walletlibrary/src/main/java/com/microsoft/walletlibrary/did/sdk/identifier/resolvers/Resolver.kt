/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.identifier.resolvers

import com.microsoft.walletlibrary.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.walletlibrary.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ResolverException
import javax.inject.Inject
import javax.inject.Named

internal class Resolver @Inject constructor(
    @Named("resolverUrl") private val baseUrl: String,
    private val identifierRepository: IdentifierRepository,
    @Named("didResolverHardeningEnabled") private val didResolverHardeningEnabled: Boolean
) {
    suspend fun resolve(identifier: String): Result<IdentifierDocument> {
        return identifierRepository.resolveIdentifier(baseUrl, identifier)
            .mapCatching {
                val resolvedDidDocument = it.didDocument
                val resolvedId = resolvedDidDocument.id
                if (didResolverHardeningEnabled && (resolvedId.isNullOrBlank() || resolvedId != identifier)) {
                    throw ResolverException(
                        "Resolved DID document id '$resolvedId' does not match requested identifier '$identifier'"
                    )
                }
                resolvedDidDocument
            }
            .onFailure {
                return Result.failure(ResolverException("Unable to resolve identifier $identifier", it))
            }
    }
}