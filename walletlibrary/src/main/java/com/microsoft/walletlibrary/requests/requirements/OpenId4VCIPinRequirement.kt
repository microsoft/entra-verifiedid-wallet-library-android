package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.requests.resolvers.OpenID4VCIPreAuthAccessTokenResolver
import com.microsoft.walletlibrary.util.LibraryConfiguration
import com.microsoft.walletlibrary.util.NetworkingException
import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.VerifiedIdException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult
import com.microsoft.walletlibrary.util.getResult

class OpenId4VCIPinRequirement(
    val length: Int? = null,
    val type: String? = null,
    val pinSet: Boolean,
    var accessToken: String? = null,
    override val required: Boolean = true,
    internal var pin: String? = null,
) : Requirement {
    internal var libraryConfiguration: LibraryConfiguration? = null
    internal var preAuthorizedCode: String? = null
    internal var accessTokenEndpoint: String? = null

    override fun validate(): VerifiedIdResult<Unit> {
        if (accessToken == null)
            return RequirementNotMetException(
                "Access Token has not been set.",
                VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value
            ).toVerifiedIdResult()
        return VerifiedIdResult.success(Unit)
    }

    suspend fun fulfill(pin: String): VerifiedIdResult<Unit> {
        this.pin = pin
        return getResult {
            libraryConfiguration?.let { libraryConfiguration ->
                accessTokenEndpoint?.let {
                    OpenID4VCIPreAuthAccessTokenResolver(libraryConfiguration).resolve(preAuthorizedCode, this, it)
                }
            }
        }
    }

    internal fun fulfillAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }

    suspend fun fulfillAccessToken(
        preAuthorizedCode: String,
        openId4VCIPinRequirement: OpenId4VCIPinRequirement,
        accessTokenEndpoint: String
    ) {
        libraryConfiguration?.let {
            OpenID4VCIPreAuthAccessTokenResolver(it).resolve(
                preAuthorizedCode,
                openId4VCIPinRequirement,
                accessTokenEndpoint
            )
        }
    }
}