// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2024

import com.microsoft.walletlibrary.datasource.db.entities.HolderIdentifierData
import com.microsoft.walletlibrary.datasource.db.entities.HolderIdentifierStoredProperties
import com.microsoft.walletlibrary.datasource.repository.HolderIdentifierDataRepository
import com.microsoft.walletlibrary.did.sdk.backup.content.UnprotectedBackupData
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.RawIdentifierConverter
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.RawIdentity
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.BackupException
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.identifier.EncryptedSharedPreferencesIdentifier
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class IdentifierConverter @Inject constructor(
    private val identityRepository: IdentifierRepository,
    private val rawIdentifierConverter: RawIdentifierConverter,
    private val serializer: Json
) {
    private val holderIdentifierDataRepository = HolderIdentifierDataRepository()

    internal suspend fun getAllIdentifiers(): List<String> {
        val encodedHolderIdentifiers = holderIdentifierDataRepository.queryAllHolderIdentifierData().map {
            val rawHolderIdentifier = createRawHolderIdentifier(it)
            encodeRawHolderIdentifier(rawHolderIdentifier)
        }
        val rawIdentities = identityRepository.queryAllLocal().map { did -> rawIdentifierConverter.createRawIdentifier(did) }
        val encodedRawIdentities = rawIdentities.map { encodeIdentifier(it) }
        return encodedRawIdentities + encodedHolderIdentifiers
    }

    private fun createRawHolderIdentifier(holderIdentifierData: HolderIdentifierData): RawHolderIdentifier {
        val keyJwk = when (val holderIdentifier = holderIdentifierDataRepository.convertToHolderIdentifier(holderIdentifierData)) {
            is EncryptedSharedPreferencesIdentifier -> holderIdentifier.getPublicKey()
            else -> throw BackupException("Unsupported HolderIdentifier type ${holderIdentifier::class.simpleName}")
        }
        return RawHolderIdentifier(
            holderIdentifierData.id,
            holderIdentifierData.didMethod,
            holderIdentifierData.algorithm,
            listOf(keyJwk),
            holderIdentifierData.keyReference,
            EncryptedSharedPreferencesIdentifier::class.simpleName ?: "EncryptedSharedPreferencesIdentifier"
        )
    }

    private fun encodeRawHolderIdentifier(rawHolderIdentifier: RawHolderIdentifier): String {
        return serializer.encodeToString(rawHolderIdentifier)
    }

    private fun parseRawHolderIdentifier(rawHolderIdentifier: RawHolderIdentifier): HolderIdentifierData {
        return HolderIdentifierData(
            rawHolderIdentifier.keys.first().keyID,
            rawHolderIdentifier.id,
            rawHolderIdentifier.didMethod,
            rawHolderIdentifier.algorithm,
            rawHolderIdentifier.keyReference ?: "0"
        )
    }

    private fun decodeRawHolderIdentifier(encodedRawHolderIdentifier: String): RawHolderIdentifier {
        return serializer.decodeFromString(encodedRawHolderIdentifier)
    }

    internal suspend fun insertAllIdentifiers(backupData: UnprotectedBackupData, keyStore: EncryptedKeyStore) {
        if (backupData !is Microsoft2024UnprotectedBackupData) {
            SdkLog.e("BackupData has wrong type ${backupData::class.simpleName}")
            throw BackupException("BackupData has wrong type ${backupData::class.simpleName}")
        }
        backupData.identifiers.forEach {
            try {
                // Check if the identifier is a Holder Identifier
                val rawHolderIdentifier = decodeRawHolderIdentifier(it)
                rawHolderIdentifier.keys.forEach { key -> importKey(key, keyStore) }
                val holderIdentifierData = parseRawHolderIdentifier(rawHolderIdentifier)
                val holderIdentifier = holderIdentifierDataRepository.convertToHolderIdentifier(holderIdentifierData)
                holderIdentifierDataRepository.insert(holderIdentifier)
            } catch (serializationException: SerializationException) {
                // Check if the identifier is a ION DID
                val oldIdentifiers = mutableListOf<Identifier>()
                var keySet = setOf<JWK>()
                val rawIdentity = decodeIdentifier(it)
                val pair = rawIdentifierConverter.parseRawIdentifier(rawIdentity)
                oldIdentifiers.add(pair.first)
                keySet = keySet.union(pair.second)
                keySet.forEach { key -> importKey(key, keyStore) }
                keyStore.storeKey(Constants.MAIN_IDENTIFIER_REFERENCE, JWK.parse(backupData.metaInf.seed))
                identityRepository.insert(pair.first)
            }
        }
    }

    private fun encodeIdentifier(rawIdentity: RawIdentity): String {
        return serializer.encodeToString(rawIdentity)
    }

    private fun decodeIdentifier(encodedRawIdentity: String): RawIdentity {
        return serializer.decodeFromString(encodedRawIdentity)
    }

    private fun importKey(
        jwk: JWK,
        keyStore: EncryptedKeyStore
    ) {
        if (!keyStore.containsKey(jwk.keyID)) {
            keyStore.storeKey(jwk.keyID, jwk)
        }
    }
}