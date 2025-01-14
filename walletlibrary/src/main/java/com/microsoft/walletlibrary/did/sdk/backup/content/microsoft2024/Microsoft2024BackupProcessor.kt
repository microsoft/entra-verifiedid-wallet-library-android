// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2024

import com.microsoft.walletlibrary.VerifiedIdClient
import com.microsoft.walletlibrary.did.sdk.IdentifierService
import com.microsoft.walletlibrary.did.sdk.backup.UnprotectedBackup
import com.microsoft.walletlibrary.did.sdk.backup.content.BackupProcessor
import com.microsoft.walletlibrary.did.sdk.backup.content.UnprotectedBackupData
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.RawIdentifierConverter
import com.microsoft.walletlibrary.did.sdk.backup.content.microsoft2020.VcMetadata
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier
import com.microsoft.walletlibrary.did.sdk.util.Constants
import com.microsoft.walletlibrary.did.sdk.util.controlflow.BackupException
import com.microsoft.walletlibrary.verifiedid.VCVerifiedIdSerializer
import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class Microsoft2024BackupProcessor @Inject constructor(
    private val identifierService: IdentifierService,
    private val identityRepository: IdentifierRepository,
    private val keyStore: EncryptedKeyStore,
    private val rawIdentifierConverter: RawIdentifierConverter,
    private val jsonSerializer: Json,
    private val verifiedIdClient: VerifiedIdClient
) : BackupProcessor {

    override suspend fun export(backup: UnprotectedBackup): UnprotectedBackupData {
        if (backup !is Microsoft2024UnProtectedBackup) throw BackupException("Backup has wrong type ${backup::class.simpleName}")
        val vcMap = mutableMapOf<String, String>()
        val vcMetaMap = mutableMapOf<String, VcMetadata>()
        backup.verifiableCredentials.forEach { verifiableCredentialMetadataPair ->
            val verifiedId = verifiedIdClient.decodeVerifiedId(verifiableCredentialMetadataPair.first).getOrNull()
                ?: throw BackupException("Failed to decode VC")
            vcMap[verifiedId.id] = VCVerifiedIdSerializer.serialize(verifiedId).raw
            vcMetaMap[verifiedId.id] = verifiableCredentialMetadataPair.second
        }

/*      This line creates master DID and its key if there isn't one already which is required for export.
        The created key is retrieved from keystore and used as seed in wallet metadata below.*/
        identifierService.getMasterIdentifier()
        backup.walletMetadata.seed = keyStore.getKey(Constants.MAIN_IDENTIFIER_REFERENCE).toJSONString()
        return Microsoft2024UnProtectedBackupData(
            vcs = vcMap,
            vcsMetaInf = vcMetaMap,
            metaInf = backup.walletMetadata,
            identifiers = rawIdentifierConverter.getAllIdentifiers()
        )
    }

    override suspend fun import(backupData: UnprotectedBackupData): UnprotectedBackup {
        if (backupData !is Microsoft2024UnProtectedBackupData) throw BackupException("BackupData has wrong type ${backupData::class.simpleName}")
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
        return Microsoft2024UnProtectedBackup(
            walletMetadata = backupData.metaInf,
            verifiableCredentials = transformVcs(backupData)
        )
    }

    private fun transformVcs(backup: Microsoft2024UnProtectedBackupData): List<Pair<String, VcMetadata>> {
        val vcList = ArrayList<Pair<String, VcMetadata>>()
        backup.vcs.forEach { mapEntry ->
            val (jti, rawVcToken) = mapEntry
            val jwsToken = JwsToken.deserialize(rawVcToken)
            val verifiableCredentialContent = jsonSerializer.decodeFromString(
                VerifiableCredentialContent.serializer(), jwsToken.content())
            val vc = VerifiableCredential(verifiableCredentialContent.jti, rawVcToken, verifiableCredentialContent)
            val verifiedId = VCVerifiedIdSerializer.deserialize(vc)
            val encodedVerifiedId = verifiedIdClient.encode(verifiedId).getOrNull()
                ?: throw BackupException("Failed to encode VC")
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