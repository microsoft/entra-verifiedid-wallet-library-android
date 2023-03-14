package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IssuanceRequestMappingTest {
    private lateinit var issuanceRequest: IssuanceRequest
    private val expectedEntityName = "testIssuer"

    init {
        setupInput()
    }

    private fun setupInput() {
        issuanceRequest = mockk()
        every { issuanceRequest.entityName } returns expectedEntityName
    }

    @Test
    fun issuanceRequestMapping_MapRequesterStyle_ReturnsSuccessfulRequesterStyle() {
        // Act
        val actualRequesterStyle = issuanceRequest.getRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle.requester).isEqualTo(expectedEntityName)
        assertThat(actualRequesterStyle.logo).isNull()
    }
}