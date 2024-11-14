// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NonceProcessorTest {

    @Test
    fun getNonce_passValidDid_ReturnsHashedNonce() {
        // Arrange
        val actualDid = "abcd"

        // Act
        val actualNonce = NonceProcessor.getNonce(actualDid)

        // Assert
        assertThat(actualNonce).isNotNull
        val actualDidHash = actualNonce.split(".")[1]
        assertThat(actualDidHash).isEqualTo("2AIvIGCtbv0perc9zFNVybIUBUsNF3ahNqZp0mp9OxT3OqDQ6_8Z7jMzaPAWS2QZqW2knj5IF1Pn6Wtxa9zLbw")
    }
}