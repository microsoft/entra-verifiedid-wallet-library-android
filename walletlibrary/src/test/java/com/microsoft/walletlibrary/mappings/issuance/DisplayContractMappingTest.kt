package com.microsoft.walletlibrary.mappings.issuance

import com.microsoft.did.sdk.credential.service.models.contracts.display.*
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DisplayContractMappingTest {
    private lateinit var displayContract: DisplayContract
    private val expectedLocale = "test"

    private val cardDescriptor: CardDescriptor = mockk()
    private val expectedCardTitle = "test card title"
    private val expectedCardDescription = "test card description"
    private val expectedCardIssuer = "test issuer"
    private val expectedCardBackgroundColor = "#FFFFFF"
    private val expectedCardTextColor = "#000000"

    private val consentDescriptor: ConsentDescriptor = mockk()

    private val claimDescriptor1: ClaimDescriptor = mockk()
    private val claimDescriptor2: ClaimDescriptor = mockk()
    private val claimsMap = mutableMapOf<String, ClaimDescriptor>()
    private val expectedClaimDescriptorType = "claimType"
    private val expectedClaimDescriptorLabel = "claimLabel"
    private val expectedClaimName1 = "claimName1"
    private val expectedClaimName2 = "claimName2"

    private val logo: Logo = mockk()
    private val expectedLogoUri = "testLogoUri"
    private val expectedLogoDescription = "testLogoDescription"

    init {
        claimsMap[expectedClaimName1] = claimDescriptor1
        setupInput(claimsMap, true)
    }

    private fun setupInput(claimsMap: MutableMap<String, ClaimDescriptor>, logoPresent: Boolean) {
        displayContract =
            DisplayContract(locale = expectedLocale, card = cardDescriptor, consent = consentDescriptor, claims = claimsMap)
        setupCardDescriptor(logoPresent)
        setupClaimDescriptor(claimsMap)
    }

    private fun setupCardDescriptor(logoPresent: Boolean) {
        every { cardDescriptor.title } returns expectedCardTitle
        every { cardDescriptor.description } returns expectedCardDescription
        every { cardDescriptor.issuedBy } returns expectedCardIssuer
        every { cardDescriptor.backgroundColor } returns expectedCardBackgroundColor
        every { cardDescriptor.textColor } returns expectedCardTextColor
        if (!logoPresent)
            every { cardDescriptor.logo } returns null
        else
            setupLogo()
    }

    private fun setupClaimDescriptor(claimsMap: MutableMap<String, ClaimDescriptor>) {
        for (claimDescriptor in claimsMap.values) {
            every { claimDescriptor.type } returns expectedClaimDescriptorType
            every { claimDescriptor.label } returns expectedClaimDescriptorLabel
        }
    }

    private fun setupLogo() {
        every { cardDescriptor.logo } returns logo
        every { logo.uri } returns expectedLogoUri
        every { logo.description } returns expectedLogoDescription
    }

    @Test
    fun displayContractMapping_mapCardDescriptor_ReturnsStylingInVerifiedIdStyle() {
        // Act
        val actualVerifiedIdStyle = displayContract.toVerifiedIdStyle()

        // Assert
        assertThat(actualVerifiedIdStyle).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((actualVerifiedIdStyle as BasicVerifiedIdStyle).name).isEqualTo(expectedCardTitle)
        assertThat(actualVerifiedIdStyle.description).isEqualTo(expectedCardDescription)
        assertThat(actualVerifiedIdStyle.issuer).isEqualTo(expectedCardIssuer)
        assertThat(actualVerifiedIdStyle.backgroundColor).isEqualTo(expectedCardBackgroundColor)
        assertThat(actualVerifiedIdStyle.textColor).isEqualTo(expectedCardTextColor)
        assertThat(actualVerifiedIdStyle.logo).isNotNull
        assertThat(actualVerifiedIdStyle.logo?.uri).isEqualTo(expectedLogoUri)
        assertThat(actualVerifiedIdStyle.logo?.description).isEqualTo(expectedLogoDescription)
    }

    @Test
    fun displayContractMapping_NoLogoInCardDescriptor_ReturnsStylingWithNoLogoInVerifiedIdStyle() {
        // Arrange
        claimsMap[expectedClaimName1] = claimDescriptor1
        setupInput(claimsMap, false)

        // Act
        val actualVerifiedIdStyle = displayContract.toVerifiedIdStyle()

        // Assert
        assertThat(actualVerifiedIdStyle).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((actualVerifiedIdStyle as BasicVerifiedIdStyle).name).isEqualTo(expectedCardTitle)
        assertThat(actualVerifiedIdStyle.description).isEqualTo(expectedCardDescription)
        assertThat(actualVerifiedIdStyle.issuer).isEqualTo(expectedCardIssuer)
        assertThat(actualVerifiedIdStyle.backgroundColor).isEqualTo(expectedCardBackgroundColor)
        assertThat(actualVerifiedIdStyle.textColor).isEqualTo(expectedCardTextColor)
        assertThat(actualVerifiedIdStyle.logo).isNull()
    }

    @Test
    fun displayContractMapping_NoClaimsInCardDescriptor_ReturnsInVerifiedIdStyleWithNoClaims() {
        // Arrange
        setupInput(mutableMapOf(), false)

        // Act
        val actualVerifiedIdStyle = displayContract.toVerifiedIdStyle()

        // Assert
        assertThat(actualVerifiedIdStyle).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((actualVerifiedIdStyle as BasicVerifiedIdStyle).name).isEqualTo(expectedCardTitle)
        assertThat(actualVerifiedIdStyle.description).isEqualTo(expectedCardDescription)
        assertThat(actualVerifiedIdStyle.issuer).isEqualTo(expectedCardIssuer)
        assertThat(actualVerifiedIdStyle.backgroundColor).isEqualTo(expectedCardBackgroundColor)
        assertThat(actualVerifiedIdStyle.textColor).isEqualTo(expectedCardTextColor)
        assertThat(actualVerifiedIdStyle.logo).isNull()
    }

    @Test
    fun displayContractMapping_OneClaimInCardDescriptor_ReturnsInVerifiedIdStyleWithOneClaim() {
        // Arrange
        claimsMap[expectedClaimName1] = claimDescriptor1
        setupInput(claimsMap, false)

        // Act
        val actualVerifiedIdStyle = displayContract.toVerifiedIdStyle()

        // Assert
        assertThat(actualVerifiedIdStyle).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((actualVerifiedIdStyle as BasicVerifiedIdStyle).name).isEqualTo(expectedCardTitle)
        assertThat(actualVerifiedIdStyle.description).isEqualTo(expectedCardDescription)
        assertThat(actualVerifiedIdStyle.issuer).isEqualTo(expectedCardIssuer)
        assertThat(actualVerifiedIdStyle.backgroundColor).isEqualTo(expectedCardBackgroundColor)
        assertThat(actualVerifiedIdStyle.textColor).isEqualTo(expectedCardTextColor)
        assertThat(actualVerifiedIdStyle.logo).isNull()
    }

    @Test
    fun displayContractMapping_MultipleClaimsInCardDescriptor_ReturnsInVerifiedIdStyleWithMultipleClaims() {
        // Arrange
        claimsMap[expectedClaimName1] = claimDescriptor1
        claimsMap[expectedClaimName2] = claimDescriptor2
        setupInput(claimsMap, false)

        // Act
        val actualVerifiedIdStyle = displayContract.toVerifiedIdStyle()

        // Assert
        assertThat(actualVerifiedIdStyle).isInstanceOf(BasicVerifiedIdStyle::class.java)
        assertThat((actualVerifiedIdStyle as BasicVerifiedIdStyle).name).isEqualTo(expectedCardTitle)
        assertThat(actualVerifiedIdStyle.description).isEqualTo(expectedCardDescription)
        assertThat(actualVerifiedIdStyle.issuer).isEqualTo(expectedCardIssuer)
        assertThat(actualVerifiedIdStyle.backgroundColor).isEqualTo(expectedCardBackgroundColor)
        assertThat(actualVerifiedIdStyle.textColor).isEqualTo(expectedCardTextColor)
        assertThat(actualVerifiedIdStyle.logo).isNull()
    }
}