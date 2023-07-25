package com.microsoft.walletlibrary.mappings

import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.walletlibrary.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LinkedDomainMappingTest {
    private val expectedDomainUrl = "https://test.com"

    @Test
    fun linkedDomains_StatusVerified_ReturnsTrue() {
        // Arrange
        val verifiedLinkedDomainResult = LinkedDomainVerified(expectedDomainUrl)

        // Act
        val actualResult = verifiedLinkedDomainResult.toRootOfTrust()

        //Assert
        assertThat(actualResult.source).isEqualTo(expectedDomainUrl)
        assertThat(actualResult.verified).isEqualTo(true)
    }

    @Test
    fun linkedDomains_StatusUnVerified_ReturnsFalse() {
        // Arrange
        val unVerifiedLinkedDomainResult = LinkedDomainUnVerified(expectedDomainUrl)

        // Act
        val actualResult = unVerifiedLinkedDomainResult.toRootOfTrust()

        //Assert
        assertThat(actualResult.source).isEqualTo(expectedDomainUrl)
        assertThat(actualResult.verified).isEqualTo(false)
    }

    @Test
    fun linkedDomains_StatusMissing_ReturnsFalse() {
        // Arrange
        val missingLinkedDomainResult = LinkedDomainMissing

        // Act
        val actualResult = missingLinkedDomainResult.toRootOfTrust()

        //Assert
        assertThat(actualResult.source).isEqualTo("")
        assertThat(actualResult.verified).isEqualTo(false)
    }
}