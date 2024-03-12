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
        display?.forEach { displayDefinition ->
            displayDefinition.locale?.let { language ->
                ConfigurationCompat.getLocales(Resources.getSystem().configuration)
                    .getFirstMatch(
                        arrayOf(language)
                    )?.let {
                        return VerifiedIdManifestIssuerStyle(it.displayName)
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
        val authorizationServerHosts = authorization_servers.map { URL(it).host }
        credentialOffer.grants.forEach {
            if (!authorizationServerHosts.contains(URL(it.value.authorization_server).host))
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

    fun validateCredentialMetadataAndSignedMetadata(credentialMetadata: CredentialMetadata) {
        if (credentialMetadata.credential_issuer == null)
            throw OpenId4VciValidationException(
                "Credential metadata does not contain credential_issuer",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
        if (credentialMetadata.signed_metadata == null)
            throw OpenId4VciValidationException(
                "Credential metadata does not contain signed_metadata",
                VerifiedIdExceptions.MALFORMED_CREDENTIAL_METADATA_EXCEPTION.value
            )
    }
}