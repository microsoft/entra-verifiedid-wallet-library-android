// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.InputContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.walletlibrary.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.util.defaultTestSerializer
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.util.Date

class StringVerifiedIdSerializerTest {

    @Test
    fun serialize_takesVCVerifiedId_ReturnsRawVCString() {
        // Arrange
        val suppliedVcJti = "testJti"
        val suppliedVcRaw = "testVcRaw"
        val suppliedVcSubject = "subject"
        val suppliedVcIssuer = "Issuer"
        val suppliedIssuedTime = 12345678L
        val suppliedExpirationTime = 145678998L
        val suppliedVcContent = VerifiableCredentialContent(
            suppliedVcJti,
            VerifiableCredentialDescriptor(listOf("contexts"), listOf("credentialTypes"), mapOf("credSubKey" to "credSubValue")),
            suppliedVcSubject,
            suppliedVcIssuer,
            suppliedIssuedTime,
            suppliedExpirationTime
        )
        val expectedVerifiableCredential =
            com.microsoft.walletlibrary.did.sdk.credential.models.VerifiableCredential(
                suppliedVcJti,
                suppliedVcRaw,
                suppliedVcContent
            )
        val verifiedId: VerifiableCredential = mockk()
        every { verifiedId.raw } returns expectedVerifiableCredential
        val serializer = StringVerifiedIdSerializer
        val expectedRawVc =
            """{"jti":"testJti","raw":"testVcRaw","contents":{"jti":"testJti","vc":{"@context":["contexts"],"type":["credentialTypes"],"credentialSubject":{"credSubKey":"credSubValue"}},"sub":"subject","iss":"Issuer","iat":12345678,"exp":145678998}}"""

        // Act
        val serializedVerifiedId = serializer.serialize(verifiedId)

        // Assert
        assertThat(serializedVerifiedId).isEqualTo(expectedRawVc)
    }

    @Test
    fun serialize_takesUnKnownVerifiedId_ThrowsException() {
        // Arrange
        val verifiedId: MockVerifiedId = mockk()
        every { verifiedId.raw } returns MockVerifiedId()
        val serializer = StringVerifiedIdSerializer

        // Act and Assert
        assertThatThrownBy {
            serializer.serialize(verifiedId)
        }.isInstanceOf(VerifiedIdSerializer.VerifiedIdSerializationNotSupported::class.java)
    }

    @Test
    fun deserialize_takesRawVCAndNoContract_ReturnsVerifiedID() {
        // Arrange
        val suppliedRawVc =
            """{"jti":"testJti","raw":"testVcRaw","contents":{"jti":"testJti","vc":{"@context":["contexts"],"type":["credentialTypes"],"credentialSubject":{"credSubKey":"credSubValue"}},"sub":"subject","iss":"Issuer","iat":12345678,"exp":145678998}}"""
        val serializer = StringVerifiedIdSerializer

        // Act
        val verifiedId = serializer.deserialize(suppliedRawVc)

        // Assert
        assertThat(verifiedId).isInstanceOf(VerifiableCredential::class.java)
    }

    @Test
    fun deserialize_takesRawVCAndContract_ReturnsVerifiedID() {
        // Arrange
        val suppliedRawVc =
            """{"jti":"testJti","raw":"testVcRaw","contents":{"jti":"testJti","vc":{"@context":["contexts"],"type":["credentialTypes"],"credentialSubject":{"credSubKey":"credSubValue"}},"sub":"subject","iss":"Issuer","iat":12345678,"exp":145678998}}"""
        val suppliedContract = VerifiableCredentialContract(
            id = "",
            input = InputContract(
                credentialIssuer = "", // this information appears to be lost
                issuer = ""
            ),
            display = DisplayContract(
                card = CardDescriptor("", "", "", "", null, ""),
                consent = ConsentDescriptor("", ""),
                claims = emptyMap()
            )
        )
        val encodedContract = defaultTestSerializer.encodeToString(VerifiableCredentialContract.serializer(), suppliedContract)
        val serializer = StringVerifiedIdSerializer

        // Act
        val verifiedId = serializer.deserialize(suppliedRawVc, encodedContract)

        // Assert
        assertThat(verifiedId).isInstanceOf(VerifiableCredential::class.java)
    }
}

class MockVerifiedId : VerifiedId {
    val raw: Any
        get() = Any()
    override val id: String = "testId"

    override val issuedOn: Date = Date(1000 * 1000L)

    override val expiresOn: Date? = null

    override val style: VerifiedIdStyle? = null

    override val types: List<String> = emptyList()

    override fun getClaims(): ArrayList<VerifiedIdClaim> {
        return emptyList<VerifiedIdClaim>() as ArrayList<VerifiedIdClaim>
    }
}