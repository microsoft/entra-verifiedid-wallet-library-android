// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2024

import com.microsoft.walletlibrary.VerifiedIdClient
import com.microsoft.walletlibrary.did.sdk.IdentifierService
import com.microsoft.walletlibrary.did.sdk.backup.UnprotectedBackup
import com.microsoft.walletlibrary.did.sdk.backup.content.BackupProcessor
import com.microsoft.walletlibrary.did.sdk.backup.content.UnprotectedBackupData
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.RawIdentifierConverter
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.VcMetadata
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.BackupException
import com.nimbusds.jose.jwk.JWK
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class Microsoft2024BackupProcessor @Inject constructor(
    private val identifierService: IdentifierService,
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val rawIdentifierConverter: RawIdentifierConverter
) : BackupProcessor {

    override suspend fun export(backup: UnprotectedBackup, verifiedIdClient: VerifiedIdClient): UnprotectedBackupData {
        if (backup !is Microsoft2024UnprotectedBackup) throw BackupException("Backup has wrong type ${backup::class.simpleName}")
        val vcMap = mutableMapOf<String, String>()
        val vcMetaMap = mutableMapOf<String, VcMetadata>()
        backup.verifiableCredentials.forEach { verifiableCredentialMetadataPair ->
            val encodedVerifiedId = verifiableCredentialMetadataPair.first
            val verifiedId = verifiedIdClient.decodeVerifiedId(encodedVerifiedId).getOrNull()
                ?: throw BackupException("Failed to decode VC")
            vcMap[verifiedId.id] = encodedVerifiedId
            vcMetaMap[verifiedId.id] = verifiableCredentialMetadataPair.second
        }

/*      This line creates master DID and its key if there isn't one already which is required for export.
        The created key is retrieved from keystore and used as seed in wallet metadata below.*/
        identifierService.getMasterIdentifier()
        backup.walletMetadata.seed = keyStore.getKey(Constants.MAIN_IDENTIFIER_REFERENCE).toJSONString()
        return Microsoft2024UnprotectedBackupData(
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            metaInf = backup.walletMetadata,
            identifiers = rawIdentifierConverter.getAllIdentifiers()
        )
    }

    override suspend fun import(backupData: UnprotectedBackupData, verifiedIdClient: VerifiedIdClient): UnprotectedBackup {
        if (backupData !is Microsoft2024UnprotectedBackupData) throw BackupException("BackupData has wrong type ${backupData::class.simpleName}")
        val identifiers = mutableListOf<Identifier>()
        var keySet = setOf<JWK>()

        backupData.identifiers.forEach { raw ->
            val pair = rawIdentifierConverter.parseRawIdentifier(raw)
            identifiers.add(pair.first)
            keySet = keySet.union(pair.second)
        }

        keySet.forEach { key -> importKey(key, keyStore) }
        identifiers.forEach { id -> identityRepository.insert(id) }

        keyStore.storeKey(Constants.MAIN_IDENTIFIER_REFERENCE, JWK.parse(backupData.metaInf.seed))
        return Microsoft2024UnprotectedBackup(
            walletMetadata = backupData.metaInf,
            verifiableCredentials = transformVcs(backupData)
        )
    }

    private fun transformVcs(backup: Microsoft2024UnprotectedBackupData): List<Pair<String, VcMetadata>> {
        val vcList = ArrayList<Pair<String, VcMetadata>>()
        backup.vcs.forEach { mapEntry ->
            val (jti, encodedVerifiedId) = mapEntry
            if (backup.vcsMetaInf[jti] == null) throw BackupException("Corrupt backup. MetaInf for $jti is missing.")
            vcList.add(Pair(encodedVerifiedId, backup.vcsMetaInf[jti]!!))
        }
        return vcList
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