package com.microsoft.walletlibrary.did.sdk.datasource.network.credentialOperations

import com.microsoft.walletlibrary.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.walletlibrary.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FetchPresentationRequestNetworkOperationTest {
    private val expectedNonce = "MqTyOoBtiLhoHAVmnkXLiw=="
    private val expectedClientId =
        "did:example:0123456789abcdefghi"
    private val expectedState = "djEeIymxLSfB"
    private val presentationDefinitions = listOf(
        "217b2662-23eb-4eec-a0f2-7706550943a9",
        "61375f4d-7423-43b8-82b1-9ef7c7406d18"
    )
    private val expectedPresentationRequestWithTwoVPTokens =
        """{
  "jti": "3941ff88-69d5-465b-be13-2fe9805efe21",
  "iat": 1643853738,
  "response_type": "id_token",
  "response_mode": "post",
  "scope": "openid",
  "nonce": "$expectedNonce",
  "client_id": "$expectedClientId",
  "redirect_uri": "https://4ab6-192-182-155-160.ngrok.io/v1.0/e1f66f2e-c050-4308-81b3-3d7ea7ef3b1b/verifiablecredentials/present",
  "state": "$expectedState",
  "exp": 1643854038,
  "registration": {
    "client_name": "December Demo",
    "subject_syntax_types_supported": [
      "did"
    ],
    "did_methods_supported": [
      "ion"
    ],
    "vp_formats": {
      "jwt_vp": {
        "alg": [
          "ES256K"
        ]
      }
    },
    "client_purpose": "DEBUGDEBUGDEBUG",
    "logo_uri": "https://foo.com/logo"
  },
  "claims": {
    "vp_token": [{
      "presentation_definition": {
        "id": "${presentationDefinitions[0]}",
        "input_descriptors": [
          {
            "id": "NameTagCredential",
            "name": "NameTagCredential",
            "purpose": "DEBUGDEBUGDEBUGDEBUGDEBUGDEBUG",
            "schema": [
              {
                "uri": "NameTagCredential"
              }
            ],
            "constraints": {
              "fields": [
                {
                  "path": [
                    "${'$'}.issuer",
                    "${'$'}.vc.issuer",
                    "${'$'}.iss"
                  ],
                  "filter": {
                    "type": "string",
                    "pattern": "did:ion:EiBP2wtU-Hcp2vDioFLjS0KKTeGFbeKhuQbWpHnczKsVIQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJzaWdfZGVjOTNjZTgiLCJwdWJsaWNLZXlKd2siOnsiY3J2Ijoic2VjcDI1NmsxIiwia3R5IjoiRUMiLCJ4IjoiS3dZcFNHWFFmdVctTlluM1RzMExQMVB4cC1uNjNuczlDNnBaWGkyMFp2ZyIsInkiOiJ0QnV3R3dOZjVkaG5jZjFXVXhiM0lwQTVyaml6ZmRYOUtjekl1b3VLVkNZIn0sInB1cnBvc2VzIjpbImF1dGhlbnRpY2F0aW9uIiwiYXNzZXJ0aW9uTWV0aG9kIl0sInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkifV0sInNlcnZpY2VzIjpbeyJpZCI6ImxpbmtlZGRvbWFpbnMiLCJzZXJ2aWNlRW5kcG9pbnQiOnsib3JpZ2lucyI6WyJodHRwczovL2RvbWFpbi5jb20vIl19LCJ0eXBlIjoiTGlua2VkRG9tYWlucyJ9LHsiaWQiOiJodWIiLCJzZXJ2aWNlRW5kcG9pbnQiOnsiaW5zdGFuY2VzIjpbImh0dHBzOi8vZGV2Lmh1Yi5tc2lkZW50aXR5LmNvbS92MS4wL2UxZjY2ZjJlLWMwNTAtNDMwOC04MWIzLTNkN2VhN2VmM2IxYiJdfSwidHlwZSI6IklkZW50aXR5SHViIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlEeGpZMzBTZkVDLWZkYkczWVZRNllHS3VOSldsNEJXX0JwRHFadDA4LXFtdyJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpRFpGSDd6SzF5LS02MTc2X3lTS2dzeVJPT3BpaXZnUHZmSXl1S2k4bHNZV2ciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaUJGX1Z2SFZ0eWkyeDNTdGtnRUVrV0g4dGNfRURZd1JhQlpxNl9pMllCS3NBIn19|did:ion:EiAv0eJ5cB0hGWVH5YbY-uw1K71EpOST6ztueEQzVCEc0A:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJzaWdfY2FiNjVhYTAiLCJwdWJsaWNLZXlKd2siOnsiY3J2Ijoic2VjcDI1NmsxIiwia3R5IjoiRUMiLCJ4IjoiOG15MHFKUGt6OVNRRTkyRTlmRFg4ZjJ4bTR2X29ZMXdNTEpWWlQ1SzhRdyIsInkiOiIxb0xsVG5rNzM2RTNHOUNNUTh3WjJQSlVBM0phVnY5VzFaVGVGSmJRWTFFIn0sInB1cnBvc2VzIjpbImF1dGhlbnRpY2F0aW9uIiwiYXNzZXJ0aW9uTWV0aG9kIl0sInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkifV0sInNlcnZpY2VzIjpbeyJpZCI6ImxpbmtlZGRvbWFpbnMiLCJzZXJ2aWNlRW5kcG9pbnQiOnsib3JpZ2lucyI6WyJodHRwczovL3N3ZWVwc3Rha2VzLmRpZC5taWNyb3NvZnQuY29tLyJdfSwidHlwZSI6IkxpbmtlZERvbWFpbnMifV19fV0sInVwZGF0ZUNvbW1pdG1lbnQiOiJFaUFwcmVTNy1Eczh5MDFnUzk2cE5iVnpoRmYxUlpvblZ3UkswbG9mZHdOZ2FBIn0sInN1ZmZpeERhdGEiOnsiZGVsdGFIYXNoIjoiRWlEMWRFdUVldERnMnhiVEs0UDZVTTNuWENKVnFMRE11M29IVWNMamtZMWFTdyIsInJlY292ZXJ5Q29tbWl0bWVudCI6IkVpREFkSzFWNkpja1BpY0RBcGFxV2IyZE95MFRNcmJKTmllNmlKVzk4Zk54bkEifX0"
                  }
                }
              ]
            }
          },
          {
            "id": "BusinessCardCredential",
            "name": "BusinessCardCredential",
            "purpose": "DEBUGDEBUGDEBUGDEBUGDEBUGDEBUG",
            "schema": [
              {
                "uri": "BusinessCardCredential"
              }
            ],
            "constraints": {
              "fields": [
                {
                  "path": [
                    "${'$'}.issuer",
                    "${'$'}.vc.issuer",
                    "${'$'}.iss"
                  ],
                  "filter": {
                    "type": "string",
                    "pattern": "did:ion:EiBP2wtU-Hcp2vDioFLjS0KKTeGFbeKhuQbWpHnczKsVIQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJzaWdfZGVjOTNjZTgiLCJwdWJsaWNLZXlKd2siOnsiY3J2Ijoic2VjcDI1NmsxIiwia3R5IjoiRUMiLCJ4IjoiS3dZcFNHWFFmdVctTlluM1RzMExQMVB4cC1uNjNuczlDNnBaWGkyMFp2ZyIsInkiOiJ0QnV3R3dOZjVkaG5jZjFXVXhiM0lwQTVyaml6ZmRYOUtjekl1b3VLVkNZIn0sInB1cnBvc2VzIjpbImF1dGhlbnRpY2F0aW9uIiwiYXNzZXJ0aW9uTWV0aG9kIl0sInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkifV0sInNlcnZpY2VzIjpbeyJpZCI6ImxpbmtlZGRvbWFpbnMiLCJzZXJ2aWNlRW5kcG9pbnQiOnsib3JpZ2lucyI6WyJodHRwczovL2RvbWFpbi5jb20vIl19LCJ0eXBlIjoiTGlua2VkRG9tYWlucyJ9LHsiaWQiOiJodWIiLCJzZXJ2aWNlRW5kcG9pbnQiOnsiaW5zdGFuY2VzIjpbImh0dHBzOi8vZGV2Lmh1Yi5tc2lkZW50aXR5LmNvbS92MS4wL2UxZjY2ZjJlLWMwNTAtNDMwOC04MWIzLTNkN2VhN2VmM2IxYiJdfSwidHlwZSI6IklkZW50aXR5SHViIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlEeGpZMzBTZkVDLWZkYkczWVZRNllHS3VOSldsNEJXX0JwRHFadDA4LXFtdyJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpRFpGSDd6SzF5LS02MTc2X3lTS2dzeVJPT3BpaXZnUHZmSXl1S2k4bHNZV2ciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaUJGX1Z2SFZ0eWkyeDNTdGtnRUVrV0g4dGNfRURZd1JhQlpxNl9pMllCS3NBIn19"
                  }
                }
              ]
            }
          }
        ]
      }
    },
    {
      "presentation_definition": {
        "id": "${presentationDefinitions[1]}",
        "input_descriptors": [
          {
            "id": "LivenessCredential",
            "name": "LivenessCredential",
            "purpose": "DEBUGDEBUGDEBUGDEBUGDEBUGDEBUG",
            "schema": [
              {
                "uri": "LivenessCredential"
              }
            ]
          }
        ]
      }
    }
    ]
  }
}"""

    @Test
    fun fetchPresentationRequestNetworkOperation_withTwoVPTokens_SucceedsToResolvePresentationRequestContent() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { presentationApis } returns mockk {
                coEvery { getRequest(any(), any()) } returns Result.success(IResponse(
                    status = 200,
                    headers = emptyMap(),
                    body = ("I'm a fake token").toByteArray(Charsets.UTF_8)
                ))
            }
        }
        val jwtValidator: JwtValidator = mockk {
            coEvery { this@mockk.verifySignature(any()) } returns true
            every { this@mockk.validateDidInHeaderAndPayload(any(), any()) } returns true
        }
        mockkObject(JwsToken.Companion)
        every { JwsToken.deserialize(any()) } answers {
            mockk {
                every { content() } returns expectedPresentationRequestWithTwoVPTokens
            }
        }
        val operation = FetchPresentationRequestNetworkOperation("", apiProvider, jwtValidator, defaultTestSerializer, emptyList())

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            assertThat(actual.isSuccess).isTrue
            val unwrapped = actual.getOrThrow()
            assertThat(unwrapped.first.nonce).isEqualTo(expectedNonce)
            assertThat(unwrapped.first.clientId).isEqualTo(expectedClientId)
            assertThat(unwrapped.first.claims.vpTokensInRequest.size).isEqualTo(2)
        }
    }
}