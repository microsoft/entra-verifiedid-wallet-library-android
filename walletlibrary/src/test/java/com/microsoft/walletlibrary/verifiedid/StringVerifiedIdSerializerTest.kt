// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class StringVerifiedIdSerializerTest {

    @Test
    fun serialize_takesVCVerifiedId_ReturnsRawVCString() {
        // Arrange
        val mockVc: com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential = mockk()
        every { mockVc.raw } returns "mockVC"
        val verifiedId: VerifiableCredential = mockk()
        every { verifiedId.raw } returns mockVc
        val serializer = StringVerifiedIdSerializer

        // Act
        val serializedVerifiedId = serializer.serialize(verifiedId)

        // Assert
        assertThat(serializedVerifiedId).isEqualTo("mockVC")
    }
}