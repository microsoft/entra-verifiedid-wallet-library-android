package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.requests.rawrequests.RequestType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RequestBodyLogFormatterTest {
    @Test
    fun format_PresentationRequest_PreservesStructureAndRedactsSensitiveValues() {
        val requestBody = mapOf(
            "claims" to mapOf(
                "vp_token" to mapOf(
                    "presentation_definition" to mapOf(
                        "input_descriptors" to listOf(
                            mapOf(
                                "id" to "descriptor-id",
                                "schema" to listOf(mapOf("uri" to "VerifiedIdentity")),
                                "constraints" to mapOf(
                                    "fields" to listOf(
                                        mapOf(
                                            "path" to listOf("$.vc.credentialSubject.photo"),
                                            "faceSDK" to true
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            "id_token_hint" to "secret-token",
            "state" to "secret-state"
        )

        val result = RequestBodyLogFormatter.format(RequestType.PRESENTATION, requestBody)

        assertThat(result).startsWith("Presentation request body received: ")
        assertThat(result).contains("VerifiedIdentity", "$.vc.credentialSubject.photo", "\"faceSDK\":true")
        assertThat(result).doesNotContain("descriptor-id", "secret-token", "secret-state")
        assertThat(result).contains("\"id\":\"[REDACTED]\"")
        assertThat(result).contains("\"id_token_hint\":\"[REDACTED]\"", "\"state\":\"[REDACTED]\"")
    }

    @Test
    fun format_IssuanceRequest_PreservesProtocolMetadataAndRedactsSensitiveValues() {
        val requestBody = mapOf(
            "response_type" to "id_token",
            "response_mode" to "form_post",
            "scope" to "openid",
            "nonce" to "secret-nonce",
            "state" to "secret-state",
            "registration" to mapOf(
                "client_name" to "Microsoft Verified ID",
                "subject_syntax_types_supported" to listOf("did:ion"),
                "vp_formats" to mapOf("jwt_vp" to mapOf("alg" to listOf("ES256K")))
            ),
            "credential_offer" to mapOf(
                "credential_configuration_ids" to listOf("employee-card"),
                "grants" to mapOf("pre-authorized_code" to "secret-code")
            )
        )

        val result = RequestBodyLogFormatter.format(RequestType.ISSUANCE, requestBody)

        assertThat(result).startsWith("Issuance request body received: ")
        assertThat(result).contains(
            "id_token",
            "form_post",
            "openid",
            "Microsoft Verified ID",
            "did:ion",
            "ES256K",
            "employee-card"
        )
        assertThat(result).doesNotContain("secret-nonce", "secret-state", "secret-code")
        assertThat(result).contains(
            "\"nonce\":\"[REDACTED]\"",
            "\"state\":\"[REDACTED]\"",
            "\"pre-authorized_code\":\"[REDACTED]\""
        )
    }
}