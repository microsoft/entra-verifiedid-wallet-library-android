// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.identifier

import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.walletlibrary.did.sdk.crypto.CryptoOperations
import com.microsoft.walletlibrary.did.sdk.crypto.keyStore.EncryptedKeyStore
import org.junit.Test

class EncryptedSharedPreferencesIdentifierTest {

    private val keyStore = EncryptedKeyStore(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test
    fun sign() {
        val encryptedSharedPreferencesIdentifier = EncryptedSharedPreferencesIdentifier(
            id = "id",
            algorithm = "algorithm",
            method = "method",
            keyReference = "keyReference",
            cryptoOperations = CryptoOperations,
            keyStore = keyStore
        )
        val testDataString = "{\"iss\":\"joe\",\n" +
            " \"exp\":1300819380,\n" +
            " \"http://example.com/is_root\":true}"
        val testDataByteArray = testDataString.toByteArray()
        val signedData = encryptedSharedPreferencesIdentifier.sign(testDataByteArray)
        println(signedData.toString())

    }
}