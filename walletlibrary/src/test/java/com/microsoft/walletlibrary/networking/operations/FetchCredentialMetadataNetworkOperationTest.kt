package com.microsoft.walletlibrary.networking.operations

import com.microsoft.walletlibrary.did.sdk.datasource.network.apis.HttpAgentApiProvider
import com.microsoft.walletlibrary.did.sdk.util.controlflow.ClientException
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata.CredentialMetadata
import com.microsoft.walletlibrary.util.defaultTestSerializer
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.IResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test

class FetchCredentialMetadataNetworkOperationTest {

    private val expectedCredentialMetadataString = """{
    "isEsts": true,
    "credential_configurations_supported": {
        "test-credential": {
            "format": "jwt_vc_json",
            "credential_definition": {
                "type": [
                    "VerifiableCredential",
                    "VerifiedEmployee"
                ],
                "credential_subject": {
                    "vc.credentialSubject.givenName": {
                        "display": [
                            {
                                "locale": "en-US",
                                "name": "Name"
                            }
                        ],
                        "value_type": "String"
                    },
                    "vc.credentialSubject.surname": {
                        "display": [
                            {
                                "locale": "en-US",
                                "name": "Surname"
                            }
                        ],
                        "value_type": "String"
                    },
                    "vc.credentialSubject.mail": {
                        "display": [
                            {
                                "locale": "en-US",
                                "name": "Email"
                            }
                        ],
                        "value_type": "String"
                    },
                    "vc.credentialSubject.jobTitle": {
                        "display": [
                            {
                                "locale": "en-US",
                                "name": "Job title"
                            }
                        ],
                        "value_type": "String"
                    },
                    "vc.credentialSubject.photo": {
                        "display": [
                            {
                                "locale": "en-US",
                                "name": "User picture"
                            }
                        ],
                        "value_type": "image/jpg;base64url"
                    },
                    "vc.credentialSubject.displayName": {
                        "display": [
                            {
                                "locale": "en-US",
                                "name": "Display name"
                            }
                        ],
                        "value_type": "String"
                    },
                    "vc.credentialSubject.preferredLanguage": {
                        "display": [
                            {
                                "locale": "en-US",
                                "name": "Preferred language"
                            }
                        ],
                        "value_type": "String"
                    },
                    "vc.credentialSubject.revocationId": {
                        "display": [
                            {
                                "locale": "en-US",
                                "name": "Revocation id"
                            }
                        ],
                        "value_type": "String"
                    }
                }
            },
            "cryptographic_binding_methods_supported": [
                "did:web"
            ],
            "cryptographic_suites_supported": [
                "ES256K"
            ],
            "display": [
                {
                    "name": "Verified Employee",
                    "background_color": "#000000",
                    "description": "This is a test verifiable credential.",
                    "locale": "en-US",
                    "logo": {
                        "uri": "https://testlogo.png",
                        "alt_text": "Default verified employee logo"
                    },
                    "text_color": "#FFFFFF"
                }
            ],
            "proof_types_supported": {
                "jwt": {
                    "proof_signing_alg_values_supported": [
                        "jwt"
                    ]
                }
            },
            "scope": "test-scope"
        }
    },
    "credential_endpoint": "https://test-credential-endpoint.com",
    "credential_issuer": "https://test-credential-issuer.com",
    "authorization_servers": [
        "https://test-authorization-server.com/token"
    ],
    "display": [
        {
            "name": "test display name"
        }
    ],
    "notification_endpoint": "https://test-notification-endpoint.com",
    "signed_metadata": "testSignedMetadata"
}""".trimIndent()

    private val expectedCredentialMetadata = defaultTestSerializer.decodeFromString(CredentialMetadata.serializer(), expectedCredentialMetadataString)

    @Test
    fun fetchCredentialMetadataNetworkOperationTest_FetchCredentialMetadata_ReturnsCredentialMetadata() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { getCredentialMetadata(any()) } returns Result.success(
                    IResponse(
                        status = 200,
                        headers = emptyMap(),
                        body = expectedCredentialMetadataString.toByteArray(Charsets.UTF_8)
                    )
                )
            }
        }
        val operation = FetchCredentialMetadataNetworkOperation("", apiProvider, defaultTestSerializer)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            Assertions.assertThat(actual.isSuccess).isTrue
            val unwrapped = actual.getOrThrow()
            Assertions.assertThat(unwrapped).isInstanceOf(CredentialMetadata::class.java)
            Assertions.assertThat(unwrapped).isEqualTo(expectedCredentialMetadata)
        }
    }

    @Test
    fun fetchCredentialMetadataNetworkOperationTest_FetchCredentialMetadataFailsWith400_ReturnsFailureWithClientException() {
        // Arrange
        val apiProvider: HttpAgentApiProvider = mockk {
            every { openId4VciApi } returns mockk {
                coEvery { getCredentialMetadata(any()) } returns Result.failure(
                    IHttpAgent.ClientException(
                        IResponse(
                            status = 400,
                            headers = emptyMap(),
                            body = "Bad request".toByteArray(Charsets.UTF_8)
                        )
                    )
                )
            }
        }
        val operation = FetchCredentialMetadataNetworkOperation("", apiProvider, defaultTestSerializer)

        runBlocking {
            // Act
            val actual = operation.fire()

            // Assert
            Assertions.assertThat(actual.isFailure).isTrue
            val unwrapped = actual.exceptionOrNull()
            Assertions.assertThat(unwrapped).isInstanceOf(ClientException::class.java)
            Assertions.assertThat((unwrapped as ClientException).errorCode?.toInt()).isEqualTo(400)
        }
    }
}