// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.datasource.repository

import com.microsoft.walletlibrary.datasource.db.entities.HolderIdentifierData
import com.microsoft.walletlibrary.did.sdk.VerifiableCredentialSdk
import com.microsoft.walletlibrary.identifier.DidMethod
import com.microsoft.walletlibrary.identifier.EncryptedSharedPreferencesIdentifierCreator
import com.microsoft.walletlibrary.identifier.HolderIdentifier
import com.microsoft.walletlibrary.util.HolderIdentifierCreationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions

internal class HolderIdentifierDataRepository {

    private val database = VerifiableCredentialSdk.identifierService.getDatabase()
    private val holderIdentifierDataDao =  database.holderIdentifierDataDao()

    private val keyStore = VerifiableCredentialSdk.identifierService.getKeyStore()
    private val holderIdentifierCreator = EncryptedSharedPreferencesIdentifierCreator(keyStore)

    private suspend fun insert(holderIdentifier: HolderIdentifierData) =
        holderIdentifierDataDao.insert(holderIdentifier)

    private suspend fun queryAllHolderIdentifierData(): List<HolderIdentifierData> =
        holderIdentifierDataDao.queryAllHolderIdentifiers()

    suspend fun getMainHolderIdentifier(): HolderIdentifier {
        val savedHolderIdentifier = queryAllHolderIdentifierData().firstOrNull()
        return if (savedHolderIdentifier != null) {
            // Convert the data to a HolderIdentifier object and return it.
            convertToHolderIdentifier(savedHolderIdentifier)
        } else {
            // If no holder identifiers are created already, create a new holder identifier using FIPS compliant keys and "did:jwk" did method.
            createNewHolderIdentifierAndStore()
        }
    }

    private suspend fun createNewHolderIdentifierAndStore(): HolderIdentifier {
        val holderIdentifier =
            holderIdentifierCreator.createHolderIdentifier("ES256", DidMethod.DID_JWK)
        insert(holderIdentifier.convertToHolderIdentifierData())
        return holderIdentifier
    }

    private fun convertToHolderIdentifier(holderIdentifierData: HolderIdentifierData): HolderIdentifier {
        return holderIdentifierCreator.createHolderIdentifier(
            holderIdentifierData.algorithm,
            DidMethod.values().find { it.value == holderIdentifierData.didMethod }
                ?: throw HolderIdentifierCreationException(
                    "Provided DID method is not supported",
                    VerifiedIdExceptions.HOLDER_IDENTIFIER_EXCEPTION.value
                ),
            holderIdentifierData.keyId
        )
    }
}