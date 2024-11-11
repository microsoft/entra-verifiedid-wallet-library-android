// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.datasource.repository

import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.walletlibrary.datasource.db.dao.HolderIdentifierDataDao
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.walletlibrary.did.sdk.datasource.db.SdkDatabase
import com.microsoft.walletlibrary.identifier.EncryptedSharedPreferencesIdentifier
import com.microsoft.walletlibrary.identifier.HolderIdentifierCreator
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.nimbusds.jose.jwk.JWK
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HolderIdentifierDataRepositoryTest {
    private val keyStore =
        EncryptedKeyStore(InstrumentationRegistry.getInstrumentation().targetContext)
    private var holderIdentifierDataRepository: HolderIdentifierDataRepository
    private val holderIdentifierDataDao: HolderIdentifierDataDao = mockk()
    private val mockLibraryConfiguration: LibraryConfiguration = mockk()

    init {
        val sdkDatabase: SdkDatabase = mockk()
        every { sdkDatabase.holderIdentifierDataDao() } returns holderIdentifierDataDao
        every { mockLibraryConfiguration.database } returns sdkDatabase
        every { mockLibraryConfiguration.keyStore } returns keyStore
        holderIdentifierDataRepository = spyk(
            HolderIdentifierDataRepository(mockLibraryConfiguration),
            recordPrivateCalls = true
        )
    }

    @Test
    fun getMainHolderIdentifier_WhenNoHolderIdentifierExists_CreatesNewHolderIdentifier() {
        // Arrange
        coEvery { holderIdentifierDataDao.queryAllHolderIdentifiers() } returns emptyList()
        coEvery { holderIdentifierDataDao.insert(any()) } returns Unit

        // Act
        val actualHolderIdentifier = runBlocking {
            holderIdentifierDataRepository.getMainHolderIdentifier()
        }

        // Assert
        coVerify(exactly = 1) { holderIdentifierDataDao.insert(any()) }
        assertThat(actualHolderIdentifier).isInstanceOf(EncryptedSharedPreferencesIdentifier::class.java)
        assertThat(actualHolderIdentifier.algorithm).isEqualTo("ES256")
        assertThat(actualHolderIdentifier.method).isEqualTo("did:jwk")
    }

    @Test
    fun getMainHolderIdentifier_WhenHolderIdentifierExists_ReturnsExistingHolderIdentifier() {
        // Arrange
        val holderIdentifierData = EncryptedSharedPreferencesIdentifier(
            "id",
            "ES256",
            "did:jwk",
            "keyReference",
            keyStore,
            "keyId"
        ).convertToHolderIdentifierData()
        coEvery { holderIdentifierDataDao.queryAllHolderIdentifiers() } returns listOf(holderIdentifierData)
        val mockPrivateKeyJwk = mockk<JWK>()
        val jwkString =
            """{"kid": "keyId", "crv": "P-256","kty": "EC","x": "acbIQiuMs3i8_uszEjJ2tpTtRM4EU3yz91PH6CdH2V0","y": "_KcyLj9vWMptnmKtm46GqDz8wf74I5LKgrl2GzH3nSE"}"""
        val mockPublicKeyJwk = JWK.parse(jwkString)
        mockkConstructor(HolderIdentifierCreator::class)
        every {
            anyConstructed<HolderIdentifierCreator>()["fetchKey"](any<String>())
        } returns mockPrivateKeyJwk
        every { mockPrivateKeyJwk.toPublicJWK() } returns mockPublicKeyJwk

        // Act
        val actualHolderIdentifier = runBlocking {
            holderIdentifierDataRepository.getMainHolderIdentifier()
        }

        // Assert
        coVerify(exactly = 0) { holderIdentifierDataDao.insert(any()) }
        assertThat(actualHolderIdentifier).isInstanceOf(EncryptedSharedPreferencesIdentifier::class.java)
        assertThat(actualHolderIdentifier.algorithm).isEqualTo("ES256")
        assertThat(actualHolderIdentifier.method).isEqualTo("did:jwk")
    }
}