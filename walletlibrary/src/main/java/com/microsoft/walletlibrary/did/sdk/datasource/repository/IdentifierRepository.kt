/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.datasource.repository

import com.microsoft.walletlibrary.did.sdk.datasource.db.SdkDatabase
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.datasource.network.identifierOperations.ResolveIdentifierNetworkOperation
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import javax.inject.Inject
import javax.inject.Named

internal class IdentifierRepository @Inject constructor(
    val database: SdkDatabase,
    val apiProvider: HttpAgentApiProvider,
    @Named("didResolverHardeningEnabled") private val didResolverHardeningEnabled: Boolean
) {
    private val identifierDao = database.identifierDao()

    suspend fun resolveIdentifier(url: String, identifier: String) = ResolveIdentifierNetworkOperation(
        apiProvider, url, identifier, didResolverHardeningEnabled
    ).fire()

    suspend fun insert(identifier: Identifier) = identifierDao.insert(identifier)

    suspend fun queryByIdentifier(identifier: String): Identifier? = identifierDao.queryByIdentifier(identifier)

    suspend fun queryByName(name: String): Identifier? = identifierDao.queryByName(name)

    suspend fun queryAllLocal(): List<Identifier> = identifierDao.queryAllLocal()

    suspend fun deleteIdentifier(identifier: String) = identifierDao.deleteIdentifier(identifier)

    suspend fun deleteAll() = identifierDao.deleteAll()
}