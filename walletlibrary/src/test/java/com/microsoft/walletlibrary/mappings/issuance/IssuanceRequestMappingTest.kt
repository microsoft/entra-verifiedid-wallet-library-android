package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IssuanceRequestMappingTest {
    private lateinit var issuanceRequest: IssuanceRequest
    private val expectedEntityName = "testIssuer"
    private val expectedConsentTitle = "test consent title"
    private val expectedConsentInstructions = "test consent instructions"

    init {
        setupInput()
    }

    private fun setupInput() {
        issuanceRequest = mockk()
        every { issuanceRequest.entityName } returns expectedEntityName
        every { issuanceRequest.contract.display.consent.title } returns expectedConsentTitle
        every { issuanceRequest.contract.display.consent.instructions } returns expectedConsentInstructions
    }

    @Test
    fun issuanceRequestMapping_MapRequesterStyle_ReturnsSuccessfulRequesterStyle() {
        // Act
        val actualRequesterStyle = issuanceRequest.getRequesterStyle()

        // Assert
        assertThat(actualRequesterStyle.name).isEqualTo(expectedEntityName)
        assertThat(actualRequesterStyle).isInstanceOf(VerifiedIdManifestIssuerStyle::class.java)
    }
}