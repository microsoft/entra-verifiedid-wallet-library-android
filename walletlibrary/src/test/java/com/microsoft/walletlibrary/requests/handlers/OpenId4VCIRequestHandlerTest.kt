package com.microsoft.walletlibrary.requests.handlers

import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test

class OpenId4VCIRequestHandlerTest {
    private val jsonSerializer = Json {
        serializersModule = SerializersModule {
            polymorphic(VerifiedId::class) {
                subclass(VerifiableCredential::class)
            }
            polymorphic(VerifiedIdStyle::class) {
                subclass(BasicVerifiedIdStyle::class)
            }
        }
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val openId4VCIRequestHandler = OpenId4VCIRequestHandler(mockk())
    private val expectedCredentialOffer = """
        {
            "credential_issuer": "metadata_url",
            "issuer_session": "request_state",
            "credential_configuration_ids": [
                "credential_id"
            ],
            "grants": {
                "authorization_code": {
                    "authorization_server": "authorization_server"
                }
            }
        }
    """.trimIndent()

    @Test
    fun mapToCredentialOffer_validMap_ReturnsCredentialOffer() {
        val credentialOffer = jsonSerializer.decodeFromString(CredentialOffer.serializer(), expectedCredentialOffer)
        assert(credentialOffer.credential_issuer == "metadata_url")
    }

    private fun jsonStringToMapWithKotlinx(json: String): Map<String, JsonElement> {
        val data = Json.parseToJsonElement(json)
        require(data is JsonObject) { "Only Json Objects can be converted to a Map!" }
        return data
    }
}