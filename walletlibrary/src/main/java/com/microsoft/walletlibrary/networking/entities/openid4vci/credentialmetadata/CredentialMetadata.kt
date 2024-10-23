package com.microsoft.walletlibrary.networking.entities.openid4vci.credentialmetadata

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import com.microsoft.walletlibrary.networking.entities.openid4vci.credentialoffer.CredentialOffer
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle
import com.microsoft.walletlibrary.util.OpenId4VciValidationException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import kotlinx.serialization.Serializable
import java.net.URL

/**
 * The metadata of the credential issuer.
 */
@Serializable
internal data class CredentialMetadata(
    // The end point of the credential issuer.
    val credential_issuer: String? = null,

    // The authorization servers property is a list of endpoints that can be used to get access token for this issuer.
    val authorization_servers: List<String>? = null,

    // The endpoint used to send the proofs to in order to be issued the Verified ID.
    val credential_endpoint: String? = null,

    // The callback endpoint to send the result of issuance.
    val notification_endpoint: String? = null,

    // Token to verify the issuer owns the DID and domain that the metadata comes from.
    val signed_metadata: String? = null,

    // A dictionary of Credential IDs to the corresponding contract.
    val credential_configurations_supported: Map<String, CredentialConfiguration>? = null,

    // Display information for the issuer.
    val display: List<LocalizedIssuerDisplayDefinition>? = null,
) {

    fun transformLocalizedIssuerDisplayDefinitionToRequesterStyle(): RequesterStyle {
        val preferredLocalesList =
            ConfigurationCompat.getLocales(Resources.getSystem().configuration)
        for (index in 0 until preferredLocalesList.size()) {
            val preferredLocale = preferredLocalesList[index]
            display?.forEach { displayDefinition ->
                displayDefinition.locale?.let { language ->
                    if (language == preferredLocale?.language) {
                        return VerifiedIdManifestIssuerStyle(displayDefinition.name ?: "")
                    }
                }
            }
        }
        return VerifiedIdManifestIssuerStyle(display?.first()?.name ?: "")
    }

    fun validateAuthorizationServers(credentialOffer: CredentialOffer) {
        if (authorization_servers.isNullOrEmpty()) {
            throw OpenId4VciValidationException(
                "Authorization servers property missing in credential metadata.",
                VerifiedIdExceptions.INVALID_PROPERTY_EXCEPTION.value
            )
        }
        val authorizationServerHosts = authorization_servers.map { getAuthorizationServerPath(it) }
        credentialOffer.grants.forEach {
            if (!authorizationServerHosts.contains(getAuthorizationServerPath(it.value.authorization_server)))
                throw OpenId4VciValidationException(
                    "Authorization server ${it.value.authorization_server} not found in Credential Metadata.",
                    VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
                )
        }
    }

    fun getSupportedCredentialConfigurations(credentialConfigurationIds: List<String>): List<CredentialConfiguration> {
        val supportedConfigIds = mutableListOf<CredentialConfiguration>()
        if (credential_configurations_supported == null)
            return supportedConfigIds
        credentialConfigurationIds.forEach { id ->
            credential_configurations_supported[id]?.let { supportedConfigIds.add(it) }
        }
        return supportedConfigIds
    }

    fun verifyIfCredentialIssuerExist() {
        if (credential_issuer == null)
            throw OpenId4VciValidationException(
                "Credential metadata does not contain credential_issuer.",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
    }

    fun verifyIfSignedMetadataExist() {
        if (signed_metadata == null)
            throw OpenId4VciValidationException(
                "Credential metadata does not contain signed_metadata.",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
    }

    private fun getAuthorizationServerPath(authorizationServer: String): String {
        val authorizationServerUrl = URL(authorizationServer)
        val authorizationServerPath = authorizationServerUrl.path.split("/")
        val authorizationServerTenant = if (authorizationServerPath.size > 1) authorizationServerPath[1] else ""
        return authorizationServerUrl.host + authorizationServerTenant
    }
}