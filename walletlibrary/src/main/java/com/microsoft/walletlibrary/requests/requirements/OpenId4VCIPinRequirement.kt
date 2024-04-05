package com.microsoft.walletlibrary.requests.requirements

import com.microsoft.walletlibrary.util.RequirementNotMetException
import com.microsoft.walletlibrary.util.VerifiedIdExceptions
import com.microsoft.walletlibrary.util.VerifiedIdResult

class OpenId4VCIPinRequirement(
    val length: Int? = null,
    val type: String? = null,
    var accessToken: String? = null,
    override val required: Boolean = true,
    internal var pin: String? = null
) : Requirement {

    override fun validate(): VerifiedIdResult<Unit> {
        if (accessToken == null)
            return RequirementNotMetException(
                "Access Token has not been set.",
                VerifiedIdExceptions.REQUIREMENT_NOT_MET_EXCEPTION.value
            ).toVerifiedIdResult()
        return VerifiedIdResult.success(Unit)
    }

    fun fulfill(pin: String) {
        this.pin = pin
    }

    internal fun fulfillAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }
}