// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.datasource.repository

import com.microsoft.walletlibrary.datasource.db.entities.HolderIdentifierData
import com.microsoft.walletlibrary.util.LibraryConfiguration

internal class HolderIdentifierDataRepository(val libraryConfiguration: LibraryConfiguration) {

    private val holderIdentifierDataDao = libraryConfiguration.database.holderIdentifierDataDao()

    private suspend fun insert(holderIdentifier: HolderIdentifierData) = holderIdentifierDataDao.insert(holderIdentifier)

    private suspend fun queryAllHolderIdentifierData(): List<HolderIdentifierData> = holderIdentifierDataDao.queryAllHolderIdentifiers()

    internal suspend fun getMainHolderIdentifier() {
        val savedHolderIdentifier = queryAllHolderIdentifierData().firstOrNull()
        savedHolderIdentifier?.let { holderIdentifier ->
            // Convert the data to a HolderIdentifier object and return it.
        } ?: {
            // If no holder identifiers are created already, create a new holder identifier using FIPS compliant keys and "did:jwk" did method.
        }

    }

}